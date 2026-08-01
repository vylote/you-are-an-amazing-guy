import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/**
 * Chi lo viec tinh toan hinh hoc cua bang: toa do tung hang tren layeredPane,
 * kich thuoc 1 o vuong. Khong chua logic chuot hay animation.
 */
public class BoardGeometry {

    private final JFrame frame;
    private final List<JPanel> rowContentPanels;
    private Rectangle[] rowBounds;
    private int cellSize = 60;

    public BoardGeometry(JFrame frame, List<JPanel> rowContentPanels) {
        this.frame = frame;
        this.rowContentPanels = rowContentPanels;
        // Tu dong tinh lai moi khi cua so resize (bao gom lan dau hien thi)
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh();
            }
        });
    }

    /** Doc lai toa do thuc te cua tung hang. Goi lai sau khi UI da hien thi hoac resize. */
    public void refresh() {
        if (rowContentPanels.isEmpty()) return;
        JLayeredPane layeredPane = frame.getLayeredPane();
        rowBounds = new Rectangle[rowContentPanels.size()];
        for (int i = 0; i < rowContentPanels.size(); i++) {
            JPanel content = rowContentPanels.get(i);
            if (content.getWidth() == 0 || content.getHeight() == 0) continue;
            Point origin = SwingUtilities.convertPoint(content, new Point(0, 0), layeredPane);
            rowBounds[i] = new Rectangle(origin.x, origin.y, content.getWidth(), content.getHeight());
        }
        Rectangle sample = rowBounds[0];
        if (sample != null) {
            cellSize = sample.height; // o vuong, canh = chieu cao hang
        }
    }

    public int rowCount() {
        return rowBounds == null ? 0 : rowBounds.length;
    }

    public int cellSize() {
        return cellSize;
    }

    /** Tam diem cua o [rowIndex, columnIndex]; cac cot xep vuong lien tiep ngay sau label. */
    public Point centerOf(int rowIndex, int columnIndex) {
        if (rowBounds == null || rowBounds[rowIndex] == null) return new Point(0, 0);
        Rectangle r = rowBounds[rowIndex];
        int cx = r.x + cellSize * columnIndex + cellSize / 2;
        int cy = r.y + r.height / 2;
        return new Point(cx, cy);
    }

    /** Tim hang chua toa do y cho truoc tren layeredPane; tra -1 neu ngoai bang. */
    public int rowIndexAt(int y) {
        if (rowBounds == null) return -1;
        for (int i = 0; i < rowBounds.length; i++) {
            Rectangle r = rowBounds[i];
            if (r != null && y >= r.y && y < r.y + r.height) return i;
        }
        return -1;
    }
}