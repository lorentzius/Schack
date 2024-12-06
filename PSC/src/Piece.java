import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

abstract class Piece {
    private final Board board;
    private BufferedImage appearance;
    private final int SIZE;
    private final PieceColor color;
    private char designator;

    private Tile tile;
    private TilePosition position;
    private DraggedPosition posWhenDragged;
    private boolean isCurrentlyDragged = false;
    private boolean hasMoved = false;

    Piece (Board board, TilePosition pos, PieceColor color) {
        SIZE = board.TILESIZE;
        this.board = board;
        this.position = pos;
        this.color = color;
        tile = board.tile(pos);
        tile.setInhabitant(this);
    }

    abstract ArrayList<TilePosition> computeAttacks();

    final static BufferedImage getPieceImage (PieceColor color, char designator) {
         String name;
         BufferedImage image = null;
         name = (color == PieceColor.White) ? "W" : "B";
         name += designator;
         InputStream is = Main.class.getResourceAsStream("Pieces/"+name+".png");
           try {
             image = ImageIO.read(is);
         }
         catch (IOException e) {
             System.out.println("Failed loading piece image");
         }
         return image;
    }
    final private void getImage() {
        appearance = getPieceImage(color, designator);
    }

    final static Piece pieceFactory(Board board, TilePosition pos, PieceType type, PieceColor pc) {
           Piece newPiece = null;
           switch (type) {
                case KING: newPiece = new King(board,pos,pc); break;
                case PAWN: newPiece = new Pawn(board,pos,pc); break;
                case KNIGHT: newPiece = new Knight(board,pos,pc); break;
                case BISHOP: newPiece = new Bishop(board,pos,pc); break;
                case ROOK: newPiece = new Rook(board,pos,pc); break;
                case QUEEN: newPiece = new Queen(board,pos,pc); break;
           }
           newPiece.designator = type.designator;
           newPiece.getImage();
           return newPiece;
    }

    final PieceColor getColor () {return color;}
    final char getDesignator() {return designator;}
    final void setDraggedPosition(DraggedPosition pos) {posWhenDragged = pos;}
    final TilePosition getTilePosition() {return position;}
    final boolean hasMoved() {return hasMoved;}
    final void setHasMoved() {hasMoved = true;}
    final void makeDragged() {isCurrentlyDragged=true;}
    final void makeNotDragged() {isCurrentlyDragged=false;}
    final void remove() {tile.setInhabitant(null);}

    final boolean isMine() {
        return (board.orientationAsWhite() ? PieceColor.White : PieceColor.Black) == color;
    }

    final boolean isControlled(TilePosition pos) {
        return isMine() ? board.isControlled(pos) : board.isControlledByOpponent(pos);
    }

    final boolean isControlledByOpponent(TilePosition pos) {
        return !isMine() ? board.isControlled(pos) : board.isControlledByOpponent(pos);
    }

    final boolean isMobile() {return !isControlledByOpponent(position);}

    void moveTo(TilePosition destination) {  //overrides by King and Pawn
        this.position = destination;
        tile.setInhabitant(null);
        tile = board.tile(destination);
        tile.setInhabitant(this);
        hasMoved = true;
    }

    PieceType getPromotionType(TilePosition target) {return null;} // override by Pawn

    boolean canGoTo(TilePosition destination) { //overrides by King and Pawn
        return computeAttacks().contains(destination) && isControlled(destination);
    }

    final boolean canMoveAtAll() {
        if (!isMobile()) return false;
        for (TilePosition pos : computeAttacks()) {
            if (canGoTo(pos)) return true;
        }
        return false;
    }

    final void paint(Graphics g) {
        if (!isCurrentlyDragged) {
            g.drawImage(appearance, position.x * SIZE, position.y * SIZE, null);
        }
        else {
            g.drawImage(appearance, posWhenDragged.x, posWhenDragged.y, null);
        }
    }

    // just abbreviations used in subclasses
    final TilePosition p(int x, int y) {return new TilePosition(x,y);}

    final boolean isFree(TilePosition pos) {return board.isFree(pos);}

    final boolean pieceIsOfOppositeColor(TilePosition pos) {return board.inhabitant(pos).isMine() != this.isMine();}

    final boolean isLegitimateTarget(TilePosition pos) {return pos.isLegal() && (isFree(pos) || pieceIsOfOppositeColor(pos));}

}


