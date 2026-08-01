import javax.swing.*;
import java.awt.*;

/**
 * Khung hinh vuong (chi ve duong vien, ben trong trong suot) dat lon hon anh
 * (co padding) de lam noi bat anh dang "active". Tu an/hien qua setVisible().
 */
public class ZoomHighlight extends JComponent {

    private static final Color BORDER_COLOR = new Color(255, 215, 0); // vang gold
    private static final int STROKE_WIDTH = 4;
    private static final int ARC = 12; // be cong goc; dat 0 neu muon vuong that 100%

    public ZoomHighlight() {
        setOpaque(false);
        setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(STROKE_WIDTH));
        int half = STROKE_WIDTH / 2;
        g2.drawRoundRect(half, half, getWidth() - STROKE_WIDTH, getHeight() - STROKE_WIDTH, ARC, ARC);
        g2.dispose();
    }
}