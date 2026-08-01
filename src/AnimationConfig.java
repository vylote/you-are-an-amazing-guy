import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Doc file cau hinh do tre (ms) cho tung buoc animation.
 * Dinh dang file: moi dong 1 so nguyen (ms). Dong trong hoac bat dau bang '#' se bi bo qua.
 * Muon them/bot buoc animation -> chi can them/xoa dong trong file nay, khong dong code.
 *
 * Vi du noi dung file:
 *   # do tre truoc moi lan anh truot 1 hang (ms)
 *   600
 *   600
 *   450
 *   450
 *   900   <- buoc nay co the la luc doi pha (A xuong het -> B xuat hien)
 */
public class AnimationConfig {

    private final List<Integer> delaysMs;

    private AnimationConfig(List<Integer> delaysMs) {
        this.delaysMs = delaysMs;
    }

    /** Doc file cau hinh; neu loi/khong ton tai, dung 1 gia tri mac dinh duy nhat cho tat ca cac buoc. */
    public static AnimationConfig loadFromFile(String path, int fallbackDelayMs) {
        List<Integer> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                list.add(Integer.parseInt(line));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[AnimationConfig] Khong doc duoc file cau hinh (" + path + "): "
                    + e.getMessage() + " -> dung do tre mac dinh " + fallbackDelayMs + "ms cho tat ca cac buoc.");
        }
        if (list.isEmpty()) {
            list.add(fallbackDelayMs);
        }
        return new AnimationConfig(list);
    }

    /**
     * Do tre (ms) truoc khi thuc hien buoc thu stepIndex (0-based).
     * Neu stepIndex vuot qua so dong trong file, tu dong lap lai gia tri cuoi cung
     * -> khong can file phai co du dong cho moi buoc, van an toan khi them/bot hang tier.
     */
    public int delayAt(int stepIndex) {
        int i = Math.min(stepIndex, delaysMs.size() - 1);
        return delaysMs.get(i);
    }

    public int stepCount() {
        return delaysMs.size();
    }
}