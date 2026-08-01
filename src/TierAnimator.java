import javax.swing.*;
import java.awt.*;

/**
 * Chay chuoi animation tu dong (khong can chuot):
 * 1) Anh A hien ra o hang 0, di chuyen dan xuong tung hang toi hang cuoi.
 * 2) Khi A toi hang cuoi, anh B moi hien ra o hang 0, cung di chuyen dan xuong hang cuoi.
 * 3) Khi ca 2 da o hang cuoi, ca 2 cung di chuyen dan len tren, ve lai hang 0.
 *
 * Do tre truoc MOI buoc duoc tra tu AnimationConfig (file cau hinh ben ngoai),
 * nen co the chinh nhip truot khac nhau cho tung buoc de khop nhac, khong bi
 * han che boi 1 con so co dinh nhu ban truoc.
 */
public class TierAnimator {

    private enum Phase { A_DOWN, B_DOWN, BOTH_UP, DONE }

    private final BoardGeometry geometry;
    private final DraggableImage imgA;
    private final DraggableImage imgB;
    private final AnimationConfig config;

    private Timer pendingTimer; // timer 1 lan cho buoc ke tiep, luon duoc thay moi
    private Phase phase;
    private int currentRow;
    private int stepIndex; // dem so buoc da chay, dung de tra do tre tuong ung trong config

    public TierAnimator(BoardGeometry geometry, DraggableImage imgA, DraggableImage imgB, AnimationConfig config) {
        this.geometry = geometry;
        this.imgA = imgA;
        this.imgB = imgB;
        this.config = config;
    }

    /** Bat dau chuoi animation tu dau: A hien, B an, dat ca 2 ve hang 0. */
    public void start() {
        if (geometry.rowCount() == 0) return; // chua co geometry, khong the bat dau

        stop(); // huy timer dang cho (neu co) truoc khi bat dau lai

        phase = Phase.A_DOWN;
        currentRow = 0;
        stepIndex = 0;

        imgA.setVisible(true);
        imgB.setVisible(false);
        place(imgA, currentRow);

        scheduleNextStep();
    }

    /** Dung animation, huy bo buoc dang cho (neu co). */
    public void stop() {
        if (pendingTimer != null) {
            pendingTimer.stop();
            pendingTimer = null;
        }
    }

    /** Dat lich cho buoc ke tiep, do tre lay tu config theo dung thu tu buoc. */
    private void scheduleNextStep() {
        int delay = config.delayAt(stepIndex);
        pendingTimer = new Timer(delay, e -> {
            stepIndex++;
            tick();
            if (phase != Phase.DONE) {
                scheduleNextStep();
            }
        });
        pendingTimer.setRepeats(false);
        pendingTimer.start();
    }

    /** Thuc hien dung 1 buoc di chuyen (hoac chuyen pha), khong tu lap lai. */
    private void tick() {
        int lastRow = geometry.rowCount() - 1;
        switch (phase) {
            case A_DOWN:
                if (currentRow < lastRow) {
                    currentRow++;
                    place(imgA, currentRow);
                } else {
                    // A da toi hang cuoi -> cho B xuat hien va bat dau di xuong tu hang 0
                    currentRow = 0;
                    imgB.setVisible(true);
                    place(imgB, currentRow);
                    phase = Phase.B_DOWN;
                }
                break;

            case B_DOWN:
                if (currentRow < lastRow) {
                    currentRow++;
                    place(imgB, currentRow);
                } else {
                    // Ca 2 dang o hang cuoi -> chuyen sang giai doan cung di len
                    phase = Phase.BOTH_UP;
                }
                break;

            case BOTH_UP:
                if (currentRow > 0) {
                    currentRow--;
                    place(imgA, currentRow);
                    place(imgB, currentRow);
                } else {
                    phase = Phase.DONE;
                }
                break;

            case DONE:
                break;
        }
    }

    private void place(DraggableImage img, int rowIndex) {
        Point c = geometry.centerOf(rowIndex, img.getColumnIndex());
        img.setLocation(c.x - img.getWidth() / 2, c.y - img.getHeight() / 2);
    }
}