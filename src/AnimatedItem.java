import java.awt.Point;

/**
 * Goi chung 1 DraggableImage va 1 ZoomHighlight di kem.
 * Dam bao khi anh di chuyen hoac bat/tat zoom, khung highlight luon bam theo dung vi tri,
 * ma khong can TierAnimator phai tu tay dong bo 2 component rieng le.
 */
public class AnimatedItem {

    private static final int HIGHLIGHT_PADDING = 14; // khoang cach giua canh anh va vien khung

    private final DraggableImage image;
    private final ZoomHighlight highlight;

    public AnimatedItem(DraggableImage image, ZoomHighlight highlight) {
        this.image = image;
        this.highlight = highlight;
    }

    public DraggableImage getImage() {
        return image;
    }

    public ZoomHighlight getHighlight() {
        return highlight;
    }

    /** Dat tam anh vao toa do cho truoc (lay tu BoardGeometry.centerOf), khung highlight tu bam theo. */
    public void placeCenterAt(Point center) {
        int size = image.getWidth();
        image.setLocation(center.x - size / 2, center.y - size / 2);
        syncHighlightToImage();
    }

    /** An/hien ca anh lan khung highlight (dung khi B chua xuat hien). */
    public void setVisible(boolean visible) {
        image.setVisible(visible);
        if (!visible) highlight.setVisible(false);
    }

    /** Bat/tat khung highlight quanh anh (khong dong gi den kich thuoc/anh that). */
    public void setZoomed(boolean zoomed) {
        highlight.setVisible(zoomed && image.isVisible());
        syncHighlightToImage();
    }

    private void syncHighlightToImage() {
        int imgSize = image.getWidth();
        int highlightSize = imgSize + HIGHLIGHT_PADDING * 2;
        int centerX = image.getX() + imgSize / 2;
        int centerY = image.getY() + imgSize / 2;
        highlight.setBounds(centerX - highlightSize / 2, centerY - highlightSize / 2, highlightSize, highlightSize);
    }
}