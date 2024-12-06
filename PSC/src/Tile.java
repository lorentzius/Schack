import java.awt.*;

class Tile {
    private final static int SIZE = Board.TILESIZE;
    private final static int LABEL_OFFSET = SIZE/12;
    private final static Color lightContrColor = new Color(255, 230, 204);
    private final static Color darkContrColor = new Color(153, 79, 0);
    private final static Color lightColor = new Color(166, 166, 166);
    private final static Color darkColor = new Color(102, 102, 102);
    private final static Color markColor = Color.BLUE;
    private final static Color alertColor = Color.RED;
    private final static int markWidth = 6;

    private final TilePosition pos;
    private final String name;
    private final boolean isDark;

    private Color color;
    private int controlValue;
    private boolean isVisible;
    private boolean markedForLastMove = false;
    private boolean showMyControls = true;
    private Piece inhabitant;


    Tile(TilePosition pos, String name) {
        this.pos = pos;
        this.name = name;
        this.isDark = (pos.x + pos.y) % 2 == 1;
    }

    String getName() {return name;}
    boolean isControlled() {return controlValue > 0;}
    boolean isControlledByOpponent(){return controlValue < 0;}
    void addControl (int increment) { controlValue += increment;}
    void setZeroControl() { controlValue = 0;}
    void makeVisible() {isVisible = true;}
    void makeInvisible() {isVisible = false;}
    void markForLastMove() {markedForLastMove = true;}
    void eraseMarkForLastMove() {markedForLastMove = false;}
    void toggleShowControl() {showMyControls = ! showMyControls;}
    Piece getInhabitant() {return inhabitant;}
    void setInhabitant(Piece piece) {inhabitant = piece;}
    boolean isFree() {
        return inhabitant == null;
    }
    boolean hasMovablePiece() {
        return !isFree() && !isControlledByOpponent() && inhabitant.isMine();
    }

    void paint(Graphics g) {
        if (!isVisible) return;
        if (isDark) color = darkColor;
        else color = lightColor;
        if ((isControlled() && showMyControls) ||
                (isControlledByOpponent() && !showMyControls)) {
            if (isDark) color = darkContrColor;
            else color = lightContrColor;
        }
        if (inhabitant != null && !inhabitant.isMobile())
            color = alertColor;

        if (markedForLastMove) {
            g.setColor(markColor);
            g.fillRect(pos.x * SIZE, pos.y * SIZE, SIZE, SIZE);
            g.setColor(color);
            g.fillRect(pos.x * SIZE + markWidth, pos.y * SIZE + markWidth,
                    SIZE - 2 * markWidth, SIZE - 2 * markWidth);
        } else {
            g.setColor(color);
            g.fillRect(pos.x * SIZE, pos.y * SIZE, SIZE, SIZE);
        }

        if (!isFree()) inhabitant.paint(g);
        paintLabel(g);
    }

    private void paintLabel(Graphics g) {
        if (isDark) g.setColor(color.WHITE);
        else g.setColor(color.BLACK);
        g.setFont(new Font("TimesRoman", Font.BOLD, 12));
        if (isBottom()) {
            g.drawString(String.valueOf(name.charAt(0)), pos.x * SIZE + SIZE / 2 - LABEL_OFFSET, (pos.y + 1) * SIZE-LABEL_OFFSET/2);
        }
        if (isLeft()) {
            g.drawString(String.valueOf(name.charAt(1)),  0, pos.y * SIZE + SIZE/2 - LABEL_OFFSET/2);
        }
    }

    private boolean isBottom() {return pos.y == 7;}
    private boolean isLeft() {return pos.x == 0;}
}
