import java.awt.Color;

/** Du lieu 1 tier: ten hien thi + mau nen. Thuan tuy la model, khong chua logic ve/keo tha. */
public class Tier {
    private final String name;
    private final Color color;

    public Tier(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}