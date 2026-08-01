import javax.swing.*;
import java.awt.*;

/**
 * 1 anh co the animate. Chi luu du lieu/trang thai rieng cua no
 * (thuoc cot nao, duong dan file anh, kich thuoc goc/kich thuoc zoom),
 * khong chua logic dieu khien khi nao chay/zoom -> logic do nam trong TierAnimator.
 */
public class DraggableImage extends JLabel {

    private static final double ZOOM_SCALE = 1.3; // phong to 130% khi dang zoom

    private final int columnIndex; // 0 = cot trai, 1 = cot phai... (chi de tinh toa do X)
    private final String imagePath;

    private int baseSize;      // kich thuoc "binh thuong" = kich thuoc 1 o vuong
    private boolean zoomed = false;

    public DraggableImage(String imagePath, int columnIndex, int size, Color fallbackColor, String fallbackLabel) {
        this.imagePath = imagePath;
        this.columnIndex = columnIndex;
        this.baseSize = size;
        setSize(size, size);
        setIcon(ImageLoader.loadScaled(imagePath, size));
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getImagePath() {
        return imagePath;
    }

    /** Doi kich thuoc "binh thuong" (goc) khi geometry cua bang thay doi (vd: cua so resize). */
    public void resizeTo(int size) {
        this.baseSize = size;
        applyCurrentSize();
    }

    /** Bat/tat hieu ung phong to. Giu nguyen tam anh (khong bi lech vi tri khi phong to/thu nho). */
    public void setZoomed(boolean zoomed) {
        if (this.zoomed == zoomed) return;
        this.zoomed = zoomed;
        applyCurrentSize();
    }

    public boolean isZoomed() {
        return zoomed;
    }

    /** Ap dung kich thuoc thuc te (co gian theo zoom hay khong), giu nguyen tam so voi truoc do. */
    private void applyCurrentSize() {
        int newSize = zoomed ? (int) Math.round(baseSize * ZOOM_SCALE) : baseSize;
        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;
        setSize(newSize, newSize);
        setIcon(ImageLoader.loadScaled(imagePath, newSize));
        setLocation(centerX - newSize / 2, centerY - newSize / 2);
    }

    /** Danh dau dang duoc "cam" hay khong. Hien khong doi giao dien gi (khong vien). */
    public void setHeld(boolean held) {
        // Khong con vien bao hieu nua. Neu can hieu ung khac (mo dan, do bong...),
        // co the them logic o day sau.
    }
}