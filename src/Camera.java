import java.awt.Graphics2D;

/**
 * "Camera" don gian cho canvas 2D: giu 1 tieu diem (focusX, focusY) trong toa do
 * logic cua bang (world coordinates) va 1 he so zoom. ApplyTo() tra ve 1 Graphics2D
 * moi da duoc translate+scale sao cho tieu diem luon nam giua vung hien thi (viewport).
 */
public class Camera {

    private double focusX;
    private double focusY;
    private double zoom = 1.0;

    public Camera(double focusX, double focusY) {
        this.focusX = focusX;
        this.focusY = focusY;
    }

    public void setFocus(double x, double y) {
        this.focusX = x;
        this.focusY = y;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getZoom() {
        return zoom;
    }

    /**
     * Tao 1 Graphics2D moi (nho dispose() sau khi ve xong) da duoc bien doi theo camera:
     * moi thu ve bang toa do world binh thuong se tu dong hien dung vi tri/kich thuoc
     * da phong to/thu nho va can giua tai tieu diem.
     */
    public Graphics2D applyTo(Graphics2D base, int viewportWidth, int viewportHeight) {
        Graphics2D g2 = (Graphics2D) base.create();
        g2.translate(viewportWidth / 2.0, viewportHeight / 2.0);
        g2.scale(zoom, zoom);
        g2.translate(-focusX, -focusY);
        return g2;
    }
}