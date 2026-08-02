import javax.swing.*;

/**
 * Chay chuoi animation, camera bam theo tung "luot chay".
 *
 * Cach tinh zoom + tieu diem X (quan trong, sua loi "mat anh" va "hut label"):
 * 1) Xac dinh khoang cach can thay (0 -> mep phai cua anh xa nhat can hien, vd A hoac B).
 * 2) GIOI HAN zoom sao cho khoang cach do CHAC CHAN lot vua khung hinh (khong sau hon).
 * 3) Neo tieu diem X = canvasWidth / (2 * zoom) - dam bao mep trai khung nhin luon dung
 *    tai x=0 (het label, khong con khoang den) VA vi zoom da bi gioi han o buoc 2,
 *    anh can thay chac chan nam trong khung hinh (khong con bi day ra ngoai / "mat anh").
 *
 * Trinh tu: A truot xuong -> zoom ra, B xuat hien -> B truot xuong (2 buoc dau zoom
 * vua phai de van thay A, tu buoc 3 zoom sau nhu binh thuong) -> B toi hang cuoi CHI
 * pan ngang (khong zoom ra) -> ca 2 cung truot len (zoom giu nguyen) -> ket thuc, zoom ra.
 */
public class TierAnimator {

    private enum Phase { A_DOWN, B_DOWN, BOTH_UP, DONE }

    private static final double ZOOM_ROWS_VISIBLE = 3.0;       // do zoom sau binh thuong (theo chieu doc)
    private static final double MODERATE_PADDING_ROWS = 1.5;    // dem khi B moi xuat hien, de van thay A
    private static final double HORIZONTAL_MARGIN = 20;         // le sau mep phai anh xa nhat can thay

    // ==== 2 THONG SO QUYET DINH TOC DO - CHINH O DAY ====
    private static final int MOVE_DURATION_MS = 260;
    private static final int ZOOM_TRANSITION_MS = 260;
    // ============================================

    private static final int FRAME_INTERVAL_MS = 16;

    private final BoardCanvas canvas;
    private final Camera camera;
    private final AnimationConfig config;
    private final int totalRows;

    private Timer waitTimer;
    private Timer animTimer;

    private Phase phase;
    private int currentRow;
    private int stepIndex;

    private static final double SHAKE_AMPLITUDE = 50;       // bien do rung toi da (world units)
    private static final double SHAKE_FREQUENCY_HZ = 2;    // TOC DO RUNG - so nhip rung/giay, giam so nay de rung cham hon

    private double currentZoom = 1.0;
    private double currentFocusX;
    private double currentFocusY;
    private double shakeOffsetX = 0; // offset rung cong THEM vao tieu diem khi ap dung camera, khong lam ban currentFocusX/Y
    private double shakeOffsetY = 0;

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
        if (waitTimer != null) { waitTimer.stop(); waitTimer = null; }
        if (animTimer != null) { animTimer.stop(); animTimer = null; }
    }

    private void applyCamera() {
        camera.setFocus(currentFocusX + shakeOffsetX, currentFocusY + shakeOffsetY);
        camera.setZoom(currentZoom);
        canvas.repaint();
    }

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
                    int toRow = currentRow + 1;
                    double desiredVertical = (currentRow == 0) ? fullZoom() : currentZoom;
                    // Chi can thay den het cot A (cot 0) trong pha nay
                    double zoomTarget = capZoomToShowColumn(desiredVertical, 0);
                    animateMove(true, false, currentRow, toRow, zoomTarget,
                            focusXForZoom(zoomTarget), canvas.rowCenterY(toRow), false);
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
                    int toRow = currentRow + 1;
                    // Luon tinh zoom de bao gom CA hang cua A (dang dung yen o lastRow) VA hang
                    // cua B - khong con ranh gioi "2 buoc dau / tu buoc 3" nua, nen A khong bao
                    // gio bi "ra khoi khung hinh" du B da di duoc bao xa.
                    double desiredVertical = zoomToInclude(lastRow, toRow, MODERATE_PADDING_ROWS);
                    double focusY = canvas.rowCenterY(midRow(lastRow, toRow));
                    // Phai thay den het cot B (cot 1) - vi cot 1 nam ben phai cot 0 nen A cung tu dong lot vao
                    double zoomTarget = capZoomToShowColumn(desiredVertical, 1);
                    animateMove(false, true, currentRow, toRow, zoomTarget, focusXForZoom(zoomTarget), focusY, false);
                } else {
                    phase = Phase.BOTH_UP;
                    // KHONG zoom ra - chi pan ngang sang tieu diem "thay ca 2 cot", zoom giu nguyen
                    animatePan(focusXForZoom(currentZoom), currentFocusY);
                }
                break;

            case BOTH_UP:
                if (currentRow > 0) {
                    int toRow = currentRow - 1;
                    // Zoom giu nguyen (khong doi) suot ca luot nay; BAT camera shake luc dang truot len
                    animateMove(true, true, currentRow, toRow, currentZoom,
                            focusXForZoom(currentZoom), canvas.rowCenterY(toRow), true);
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
    }

    private double fullZoom() {
        return totalRows / ZOOM_ROWS_VISIBLE;
    }

    /** Zoom vua du de thay ca hang rowX va rowY (co dem), nhung khong sau hon zoom binh thuong. */
    private double zoomToInclude(int rowX, int rowY, double paddingRows) {
        double span = Math.abs(rowX - rowY) + paddingRows;
        return Math.min(totalRows / span, fullZoom());
    }

    private double midRow(int rowX, int rowY) {
        return (rowX + rowY) / 2.0;
    }

    /**
     * Gioi han zoom mong muon sao cho khoang [0, mep phai cot columnIndex] chac chan
     * lot vua khung hinh - day la buoc quan trong dam bao vua khong hut label, vua
     * khong day anh ra ngoai khung hinh.
     */
    private double capZoomToShowColumn(double desiredZoom, int columnIndex) {
        double requiredWidth = canvas.rightEdgeOfColumn(columnIndex) + HORIZONTAL_MARGIN;
        double maxZoomByWidth = canvas.getWidth() / requiredWidth;
        return Math.min(desiredZoom, maxZoomByWidth);
    }

    /** Neo mep trai khung nhin dung tai x=0 (het label) voi zoom da cho truoc. */
    private double focusXForZoom(double zoom) {
        return canvas.getWidth() / (2.0 * zoom);
    }

    private void animateMove(boolean moveA, boolean moveB, int fromRow, int toRow,
                              double zoomTarget, double focusXTarget, double focusYTarget, boolean shake) {
        long startTime = System.currentTimeMillis();
        double zoomFrom = currentZoom;
        double focusXFrom = currentFocusX;
        double focusYFrom = currentFocusY;

        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(FRAME_INTERVAL_MS, null);
        animTimer.addActionListener(e -> {
            double t = Math.min(1.0, (System.currentTimeMillis() - startTime) / (double) MOVE_DURATION_MS);
            double eased = easeInOutCubic(t);
            double rowNow = fromRow + (toRow - fromRow) * eased;

            if (moveA) canvas.setRowA(rowNow);
            if (moveB) canvas.setRowB(rowNow);

            currentZoom = zoomFrom + (zoomTarget - zoomFrom) * eased;
            currentFocusX = focusXFrom + (focusXTarget - focusXFrom) * eased;
            currentFocusY = focusYFrom + (focusYTarget - focusYFrom) * eased;

            if (shake) {
                // Rung theo hinh sin voi tan so co dinh (SHAKE_FREQUENCY_HZ) - muot va co "toc do"
                // ro rang de chinh, thay vi doi huong ngau nhien moi frame (kho chinh nhanh/cham).
                double elapsedMs = System.currentTimeMillis() - startTime;
                double decay = 1.0 - t; // rung manh nhat luc vua bat dau truot, tat dan ve 0 khi sap dung
                double phase = elapsedMs / 1000.0 * SHAKE_FREQUENCY_HZ * 2 * Math.PI;
                shakeOffsetX = SHAKE_AMPLITUDE * decay * Math.sin(phase);
                shakeOffsetY = SHAKE_AMPLITUDE * decay * Math.sin(phase * 1.3 + 1.0); // lech pha/tan so nhe de khong rung theo duong thang cheo
            } else {
                shakeOffsetX = 0;
                shakeOffsetY = 0;
            }

            applyCamera();

            if (t >= 1.0) {
                animTimer.stop();
                if (moveA) canvas.setRowA(toRow);
                if (moveB) canvas.setRowB(toRow);
                currentRow = toRow;
                currentZoom = zoomTarget;
                currentFocusX = focusXTarget;
                currentFocusY = focusYTarget;
                shakeOffsetX = 0;
                shakeOffsetY = 0;
                advanceStep();
            }
        });
        animTimer.start();
    }

    private void animatePan(double focusXTarget, double focusYTarget) {
        long startTime = System.currentTimeMillis();
        double zoomFixed = currentZoom;
        double focusXFrom = currentFocusX;
        double focusYFrom = currentFocusY;

        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(FRAME_INTERVAL_MS, null);
        animTimer.addActionListener(e -> {
            double t = Math.min(1.0, (System.currentTimeMillis() - startTime) / (double) ZOOM_TRANSITION_MS);
            double eased = easeInOutCubic(t);

            currentFocusX = focusXFrom + (focusXTarget - focusXFrom) * eased;
            currentFocusY = focusYFrom + (focusYTarget - focusYFrom) * eased;
            currentZoom = zoomFixed;
            applyCamera();

            if (t >= 1.0) {
                animTimer.stop();
                currentFocusX = focusXTarget;
                currentFocusY = focusYTarget;
                advanceStep();
            }
        });
        animTimer.start();
    }

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