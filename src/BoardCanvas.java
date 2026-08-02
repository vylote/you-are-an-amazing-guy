import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Ve toan bo bang tier tren 1 canvas duy nhat. Rieng CHIEU CAO hang van lap day canvas
 * (rowHeight = getHeight()/soHang), nhung 2 O ANH KHONG con keo gian lap day phan con lai
 * nua - kich thuoc co dinh theo ty le rowHeight, dat sat nhau (co GAP nho) ngay sau label,
 * de khi zoom camera vao 1 anh thi anh kia (neu cung dang hien) van nam gan do, khong bi
 * "xa" ra qua muc. Phan con du ben phai tu nhien la nen den (khong ve gi them).
 */
public class BoardCanvas extends JPanel {

    public static final int LABEL_COL_WIDTH = 150;
    private static final int IMG_GAP = 12;            // khoang cach giua 2 o anh (de "sat nhau")
    private static final int LEFT_MARGIN_AFTER_LABEL = 20; // khoang trong giua label va o anh dau tien
    private static final double CELL_SIZE_RATIO = 0.8; // be rong o anh = rowHeight * ty le nay (chieu cao anh = full rowHeight, khong con le tren/duoi)

    private final List<Tier> tiers;
    private final Image imageA;
    private final Image imageB;
    private final Camera camera;

    private double rowA = 0;
    private double rowB = 0;
    private boolean visibleA = true;
    private boolean visibleB = false;

    public BoardCanvas(List<Tier> tiers, String imagePathA, String imagePathB, Camera camera) {
        this.tiers = tiers;
        this.imageA = ImageLoader.loadScaled(imagePathA, 256).getImage();
        this.imageB = ImageLoader.loadScaled(imagePathB, 256).getImage();
        this.camera = camera;
        setBackground(new Color(20, 20, 18));
    }

    /** Chieu cao 1 hang = chieu cao canvas / so hang (luon lap day chieu cao). */
    public double rowHeight() {
        int rows = tiers.size();
        return rows > 0 ? getHeight() / (double) rows : 100;
    }

    /** Canh 1 o anh - co dinh theo ty le rowHeight, KHONG keo gian theo be rong canvas. */
    private double cellSize() {
        return rowHeight() * CELL_SIZE_RATIO;
    }

    public double rowCenterY(double rowIndex) {
        return rowIndex * rowHeight() + rowHeight() / 2.0;
    }

    /** Tam X cua o anh thu columnIndex (0 hoac 1) - 2 o dat sat nhau (cach nhau IMG_GAP). */
    public double colCenterX(int columnIndex) {
        double size = cellSize();
        double startX = LABEL_COL_WIDTH + LEFT_MARGIN_AFTER_LABEL;
        return startX + size / 2.0 + columnIndex * (size + IMG_GAP);
    }

    /** Tam X giua 2 o anh - dung lam tieu diem camera khi ca 2 anh cung dang di chuyen. */
    public double imagesMidpointX() {
        return (colCenterX(0) + colCenterX(1)) / 2.0;
    }

    /** Mep phai cua o anh thu columnIndex - dung de tinh zoom/focus dam bao thay het anh nay. */
    public double rightEdgeOfColumn(int columnIndex) {
        return colCenterX(columnIndex) + cellSize() / 2.0;
    }

    public void setRowA(double row) {
        this.rowA = row;
    }

    public void setRowB(double row) {
        this.rowB = row;
    }

    public void setVisibleA(boolean v) {
        this.visibleA = v;
    }

    public void setVisibleB(boolean v) {
        this.visibleB = v;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D raw = (Graphics2D) g;
        raw.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Graphics2D g2 = camera.applyTo(raw, getWidth(), getHeight());
        try {
            drawRows(g2);
            if (visibleA) drawImage(g2, imageA, rowA, 0);
            if (visibleB) drawImage(g2, imageB, rowB, 1);
        } finally {
            g2.dispose();
        }
    }

    private void drawRows(Graphics2D g2) {
        double rh = rowHeight();
        int width = getWidth();

        for (int i = 0; i < tiers.size(); i++) {
            Tier tier = tiers.get(i);
            double y = i * rh;

            g2.setColor(tier.getColor());
            g2.fill(new Rectangle2D.Double(0, y, LABEL_COL_WIDTH, rh));
            g2.setColor(new Color(24, 22, 20));
            g2.fill(new Rectangle2D.Double(LABEL_COL_WIDTH, y, width - LABEL_COL_WIDTH, rh));

            g2.setColor(Color.BLACK);
            g2.draw(new Line2D.Double(0, y, width, y));

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(tier.getName());
            g2.drawString(tier.getName(), (int) ((LABEL_COL_WIDTH - textW) / 2.0),
                    (int) (y + rh / 2 + fm.getAscent() / 2 - 4));
        }
        double totalHeight = tiers.size() * rh;
        g2.setColor(Color.BLACK);
        g2.draw(new Line2D.Double(LABEL_COL_WIDTH, 0, LABEL_COL_WIDTH, totalHeight));
        g2.draw(new Rectangle2D.Double(0, 0, width - 1, totalHeight - 1));
    }

    private void drawImage(Graphics2D g2, Image img, double row, int col) {
        double cx = colCenterX(col);
        double cy = rowCenterY(row);
        double w = cellSize();
        double h = rowHeight();
        g2.drawImage(img, (int) Math.round(cx - w / 2), (int) Math.round(cy - h / 2),
                (int) Math.round(w), (int) Math.round(h), null);
    }
}