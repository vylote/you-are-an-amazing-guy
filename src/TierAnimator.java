import javax.swing.*;

/**
 * Chay chuoi animation, KHONG zoom ra/vao lien tuc moi buoc nua (do gay roi mat).
 * Thay vao do, zoom chi doi trang thai dung 2 lan moi "luot chay":
 * - Buoc DAU TIEN cua 1 luot (vd A tu hang 0 -> 1): zoom VAO dong thoi voi luc truot.
 * - Cac buoc GIUA cua luot do: chi truot, zoom giu nguyen (khong doi).
 * - Sau buoc CUOI cua luot (anh da toi hang cuoi): zoom RA rieng 1 lan (khong co gi truot),
 *   dua camera ve giua bang, roi lai bat dau luot tiep theo (B xuat hien, hoac ca 2 cung len).
 *
 * Trong luc WAIT (cho do tre tu AnimationConfig), camera hoan toan khong doi -
 * giu nguyen bat ke dang zoom vao hay da zoom ra.
 */
public class TierAnimator {

    private enum Phase { A_DOWN, B_DOWN, BOTH_UP, DONE }

    private static final double ZOOM_ROWS_VISIBLE = 3.0; // do zoom sau (so nho hon = zoom sau hon)

    // ==== 2 THONG SO QUYET DINH TOC DO - CHINH O DAY ====
    private static final int MOVE_DURATION_MS = 260;       // thoi gian 1 buoc truot (ke ca buoc co zoom-in dong thoi)
    private static final int ZOOM_TRANSITION_MS = 260;      // thoi gian rieng cho 1 lan zoom-ra (khong co gi truot)
    // ============================================

    private static final int FRAME_INTERVAL_MS = 16; // ~60fps

    private final BoardCanvas canvas;
    private final Camera camera;
    private final AnimationConfig config;
    private final int totalRows;

    private Timer waitTimer;
    private Timer animTimer;

    private Phase phase;
    private int currentRow;
    private int stepIndex;

    // Trang thai camera hien tai - dung lam diem "tu" (from) cho lan bien doi tiep theo
    private double currentZoom = 1.0;
    private double currentFocusX;
    private double currentFocusY;

    public TierAnimator(BoardCanvas canvas, Camera camera, AnimationConfig config, int totalRows) {
        this.canvas = canvas;
        this.camera = camera;
        this.config = config;
        this.totalRows = totalRows;
    }

    public void start() {
        stopAllTimers();
        phase = Phase.A_DOWN;
        currentRow = 0;
        stepIndex = 0;

        canvas.setVisibleA(true);
        canvas.setVisibleB(false);
        canvas.setRowA(currentRow);

        currentFocusX = canvas.getWidth() / 2.0;
        currentFocusY = canvas.getHeight() / 2.0;
        currentZoom = 1.0;
        applyCamera();

        scheduleWait();
    }

    public void stop() {
        stopAllTimers();
    }

    private void stopAllTimers() {
        if (waitTimer != null) {
            waitTimer.stop();
            waitTimer = null;
        }
        if (animTimer != null) {
            animTimer.stop();
            animTimer = null;
        }
    }

    private void applyCamera() {
        camera.setFocus(currentFocusX, currentFocusY);
        camera.setZoom(currentZoom);
        canvas.repaint();
    }

    /** Chi cho het do tre. KHONG dong gi den camera - giu nguyen trang thai zoom dang co. */
    private void scheduleWait() {
        int delay = config.delayAt(stepIndex);
        waitTimer = new Timer(delay, e -> performStep());
        waitTimer.setRepeats(false);
        waitTimer.start();
    }

    private void performStep() {
        int lastRow = totalRows - 1;
        switch (phase) {
            case A_DOWN:
                if (currentRow < lastRow) {
                    boolean firstMoveOfRun = (currentRow == 0);
                    animateMove(true, false, currentRow, currentRow + 1, firstMoveOfRun, canvas.colCenterX(0));
                } else {
                    currentRow = 0;
                    canvas.setVisibleB(true);
                    canvas.setRowB(currentRow);
                    phase = Phase.B_DOWN;
                    animateZoomOut();
                }
                break;

            case B_DOWN:
                if (currentRow < lastRow) {
                    boolean firstMoveOfRun = (currentRow == 0);
                    animateMove(false, true, currentRow, currentRow + 1, firstMoveOfRun, canvas.colCenterX(1));
                } else {
                    phase = Phase.BOTH_UP;
                    animateZoomOut();
                }
                break;

            case BOTH_UP:
                if (currentRow > 0) {
                    boolean firstMoveOfRun = (currentRow == lastRow);
                    animateMove(true, true, currentRow, currentRow - 1, firstMoveOfRun, canvas.imagesMidpointX());
                } else {
                    phase = Phase.DONE;
                    animateZoomOut();
                }
                break;

            case DONE:
                break;
        }
    }

    private void advanceStep() {
        stepIndex++;
        if (phase != Phase.DONE) {
            scheduleWait();
        }
        // Neu la DONE thi khong lam gi them - animateZoomOut() da dua camera ve toan canh roi.
    }

    /**
     * Truot 1 hang. Neu firstMoveOfRun = true, zoom VAO (tu currentZoom -> targetZoom)
     * DONG THOI voi luc truot. Neu false, zoom giu nguyen currentZoom suot qua trinh truot
     * (khong doi - day chinh la "khong zoom out/in lien tuc" nhu yeu cau).
     */
    private void animateMove(boolean moveA, boolean moveB, int fromRow, int toRow, boolean firstMoveOfRun, double focusX) {
        long startTime = System.currentTimeMillis();
        double targetZoom = totalRows / ZOOM_ROWS_VISIBLE;
        double zoomFrom = currentZoom;
        double zoomTo = firstMoveOfRun ? targetZoom : currentZoom;

        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(FRAME_INTERVAL_MS, null);
        animTimer.addActionListener(e -> {
            double t = Math.min(1.0, (System.currentTimeMillis() - startTime) / (double) MOVE_DURATION_MS);
            double eased = easeInOutCubic(t);
            double rowNow = fromRow + (toRow - fromRow) * eased;

            if (moveA) canvas.setRowA(rowNow);
            if (moveB) canvas.setRowB(rowNow);

            currentZoom = zoomFrom + (zoomTo - zoomFrom) * eased;
            currentFocusX = focusX;
            currentFocusY = canvas.rowCenterY(rowNow);
            applyCamera();

            if (t >= 1.0) {
                animTimer.stop();
                if (moveA) canvas.setRowA(toRow);
                if (moveB) canvas.setRowB(toRow);
                currentRow = toRow;
                currentZoom = zoomTo;
                advanceStep();
            }
        });
        animTimer.start();
    }

    /** Zoom rieng le (khong co gi truot): tu currentZoom -> 1.0, focus tra ve giua bang. */
    private void animateZoomOut() {
        long startTime = System.currentTimeMillis();
        double zoomFrom = currentZoom;
        double focusFromX = currentFocusX;
        double focusFromY = currentFocusY;
        double focusToX = canvas.getWidth() / 2.0;
        double focusToY = canvas.getHeight() / 2.0;

        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(FRAME_INTERVAL_MS, null);
        animTimer.addActionListener(e -> {
            double t = Math.min(1.0, (System.currentTimeMillis() - startTime) / (double) ZOOM_TRANSITION_MS);
            double eased = easeInOutCubic(t);

            currentZoom = zoomFrom + (1.0 - zoomFrom) * eased;
            currentFocusX = focusFromX + (focusToX - focusFromX) * eased;
            currentFocusY = focusFromY + (focusToY - focusFromY) * eased;
            applyCamera();

            if (t >= 1.0) {
                animTimer.stop();
                currentZoom = 1.0;
                currentFocusX = focusToX;
                currentFocusY = focusToY;
                advanceStep();
            }
        });
        animTimer.start();
    }

    private static double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }
}