import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dung giao dien tinh cua bang tier (giu nguyen cau truc GridLayout + BorderLayout
 * nhu ban goc). Khong chua logic animation - viec do giao het cho TierAnimator.
 */
public class TierBoard extends JFrame {

    private static final Tier[] TIERS = {
        new Tier("Acceptance", new Color(0, 220, 59)),
        new Tier("Hope", new Color(161, 216, 52)),
        new Tier("Struggle", new Color(206, 218, 47)),
        new Tier("Doubt", new Color(230, 138, 0)),
        new Tier("Lost", new Color(255, 83, 64))
    };

    // Duong dan anh demo - doi lai duong dan that cua ban; neu khong ton tai,
    // ImageLoader se tu dong dung anh placeholder de chuong trinh van chay duoc.
    private static final String IMAGE_A_PATH = "images/imgA.png";
    private static final String IMAGE_B_PATH = "images/imgB.png";

    public TierBoard() {
        super("You are an amazing guy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(true); // BoardGeometry tu tinh lai toa do khi resize nen co the bat resize thoai mai

        JPanel root = new JPanel();
        root.setLayout(new GridLayout(TIERS.length, 1));
        root.setBackground(new Color(20, 20, 18));

        List<JPanel> contentPanels = new ArrayList<>();
        for (Tier tier : TIERS) {
            JPanel content = new JPanel(); // se duoc BoardGeometry dung lam moc toa do
            JPanel row = buildRow(tier.getName(), tier.getColor(), content);
            root.add(row);
            contentPanels.add(content);
        }

        JScrollPane scroll = new JScrollPane(root);
        scroll.setBorder(null);
        add(scroll);

        // Tao 2 anh: cot 0 va cot 1, ngay sau label
        DraggableImage imgA = new DraggableImage(IMAGE_A_PATH, 0, 60, Color.CYAN, "A");
        DraggableImage imgB = new DraggableImage(IMAGE_B_PATH, 1, 60, Color.MAGENTA, "B");
        getLayeredPane().add(imgA, JLayeredPane.PALETTE_LAYER);
        getLayeredPane().add(imgB, JLayeredPane.PALETTE_LAYER);

        // Nhac nen, lap lai lien tuc (doi duong dan cho khop file that cua ban)
        MusicPlayer music = new MusicPlayer();
        music.playLoop("audio/bgm.wav");

        // Cho UI hien thi xong roi moi tinh geometry va bat dau animation
        BoardGeometry geometry = new BoardGeometry(this, contentPanels);
        AnimationConfig timing = AnimationConfig.loadFromFile("config/timing.txt", 800);
        SwingUtilities.invokeLater(() -> {
            geometry.refresh();
            imgA.resizeTo(geometry.cellSize());
            imgB.resizeTo(geometry.cellSize());
            TierAnimator animator = new TierAnimator(geometry, imgA, imgB, timing);
            animator.start();
        });
    }

    /** Dung 1 hang tier: nhan mau ben trai + vung noi dung (content) ben phai duoc truyen vao san. */
    private JPanel buildRow(String name, Color color, JPanel content) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(new MatteBorder(1, 0, 1, 0, Color.BLACK));

        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setPreferredSize(new Dimension(150, 110));
        label.setBorder(new MatteBorder(0, 0, 0, 1, Color.BLACK));
        row.add(label, BorderLayout.WEST);

        content.setBackground(new Color(24, 22, 20));
        row.add(content, BorderLayout.CENTER);

        return row;
    }
}