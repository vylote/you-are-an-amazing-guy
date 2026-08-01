import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Chuyen doc file anh va co gian ve dung kich thuoc o vuong (size x size).
 * Neu khong doc duoc file (thieu file, sai duong dan...), tra ve 1 icon
 * placeholder (hinh tron mau + chu) de chuong trinh van chay duoc khi demo.
 */
public class ImageLoader {

    /** Doc anh tu duong dan, co gian vua khop hinh vuong size x size, giu ti le (letterbox). */
    public static ImageIcon loadScaled(String path, int size) {
        try {
            BufferedImage original = ImageIO.read(new File(path));
            if (original == null) {
                throw new IOException("Khong doc duoc anh: " + path);
            }
            return new ImageIcon(scaleToFit(original, size));
        } catch (IOException e) {
            System.err.println("[ImageLoader] " + e.getMessage() + " -> dung anh placeholder.");
            return placeholder(size, new Color(120, 120, 120), "?");
        }
    }

    /** Co gian anh goc de vua khop trong o size x size, giu nguyen ty le, phan du to nen trong suot. */
    private static BufferedImage scaleToFit(BufferedImage src, int size) {
        double scale = Math.min((double) size / src.getWidth(), (double) size / src.getHeight());
        int w = Math.max(1, (int) Math.round(src.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(src.getHeight() * scale));

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = (size - w) / 2;
        int y = (size - h) / 2;
        g2.drawImage(src, x, y, w, h, null);
        g2.dispose();
        return result;
    }

    /** Icon du phong: hinh tron mau + 1 ky tu, dung khi khong co file anh that. */
    public static ImageIcon placeholder(int size, Color color, String label) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, size / 3));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (size - fm.stringWidth(label)) / 2;
        int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(label, tx, ty);
        g2.dispose();
        return new ImageIcon(img);
    }
}