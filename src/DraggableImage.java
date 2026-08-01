import javax.swing.*;
import java.awt.*;

/**
 * 1 anh dung de animate. Kich thuoc luon co dinh theo o vuong (khong tu phong to nua -
 * hieu ung "zoom" gio nam o ZoomHighlight/AnimatedItem, khong phai o ban than anh).
 * Chi luu du lieu rieng cua no (thuoc cot nao, duong dan file anh).
 */
public class DraggableImage extends JLabel {

    private final int columnIndex; // 0 = cot trai, 1 = cot phai... (chi de tinh toa do X)
    private final String imagePath;

    public DraggableImage(String imagePath, int columnIndex, int size, Color fallbackColor, String fallbackLabel) {
        this.imagePath = imagePath;
        this.columnIndex = columnIndex;
        setSize(size, size);
        setIcon(ImageLoader.loadScaled(imagePath, size));
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getImagePath() {
        return imagePath;
    }

    /** Doi kich thuoc anh khi geometry cua bang thay doi (vd: cua so resize). */
    public void resizeTo(int size) {
        setSize(size, size);
        setIcon(ImageLoader.loadScaled(imagePath, size));
    }
}