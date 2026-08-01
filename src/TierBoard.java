import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Cua so chinh: dung 1 BoardCanvas duy nhat (tu ve toan bo bang bang tay),
 * dieu khien boi Camera + TierAnimator de tao hieu ung "zoom camera" tap trung
 * vao anh dang di chuyen, thay vi hien toan bo bang co dinh nhu truoc.
 */
public class TierBoard extends JFrame {

    private static final Tier[] TIERS = {
        new Tier("Acceptance", new Color(0, 220, 59)),
        new Tier("Hope", new Color(161, 216, 52)),
        new Tier("Struggle", new Color(206, 218, 47)),
        new Tier("Doubt", new Color(230, 138, 0)),
        new Tier("Lost", new Color(255, 83, 64))
    };

    // Duong dan anh - doi lai duong dan that; neu khong ton tai, ImageLoader
    // se tu dung anh placeholder de chuong trinh van chay duoc.
    private static final String IMAGE_A_PATH = "images/imgA.png";
    private static final String IMAGE_B_PATH = "images/imgB.png";

    public TierBoard() {
        super("You are an amazing guy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        List<Tier> tiers = Arrays.asList(TIERS);
        Camera camera = new Camera(0, 0); // gia tri tam thoi, TierAnimator.start() se ghi de ngay
        BoardCanvas canvas = new BoardCanvas(tiers, IMAGE_A_PATH, IMAGE_B_PATH, camera);
        setContentPane(canvas);

        // Nhac nen, lap lai lien tuc (doi duong dan cho khop file that cua ban)
        MusicPlayer music = new MusicPlayer();
        music.playLoop("audio/bgm.wav");

        AnimationConfig timing = AnimationConfig.loadFromFile("config/timing.txt", 800);
        TierAnimator animator = new TierAnimator(canvas, camera, timing, tiers.size());

        // Cho canvas hien thi that su (co kich thuoc that) roi moi bat dau animation,
        // vi hinh hoc (rowHeight/imageColWidth) phu thuoc kich thuoc canvas thuc te.
        SwingUtilities.invokeLater(animator::start);
    }
}