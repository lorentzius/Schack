import java.io.Serializable;

class TilePosition implements Serializable {   //Position in 0..7 x 0..7
    int x,y;
    TilePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    TilePosition add(TilePosition pos) {
        return new TilePosition(this.x+pos.x, this.y+pos.y);
    }

    TilePosition transpose() {
        return new TilePosition(7-x, 7-y);
    }

    boolean isLegal() {
        return x>=0 && y>=0 && x<=7 && y<=7;
    }

    boolean isFinalRank() {
        return y==0 || y==7;
    }

    @Override
    public boolean equals(Object obj) {
        TilePosition pos = (TilePosition) obj;
        return this.x == pos.x && this.y == pos.y;
    }

    public String toString() {
        return "("+x+","+y+")";
    }
}


class DraggedPosition {  // position in pixels
    int x;
    int y;
    DraggedPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    TilePosition tilePosition () {
       return new TilePosition(x / Board.TILESIZE , y / Board.TILESIZE);
    }
}
