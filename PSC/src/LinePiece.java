import java.util.ArrayList;

abstract class LinePiece extends Piece {
    final TilePosition[] bishopDirections =
            {p(1,1), p(1,-1), p(-1,1), p(-1,-1)};
    final TilePosition[] rookDirections =
            {p(0,1), p(0,-1), p(-1,0), p(1,0)};

    LinePiece(Board board, TilePosition pos, PieceColor pc) {
        super(board, pos, pc);
    }

    abstract TilePosition[] getDirections();

    ArrayList<TilePosition> computeAttacks() {
        ArrayList<TilePosition> result = new ArrayList<TilePosition>();
        for (TilePosition direction: getDirections()) {
            result.addAll(attacksInOneDirection(direction));
        }
        return result;
    }

    final private ArrayList<TilePosition> attacksInOneDirection(TilePosition direction) {
        ArrayList<TilePosition> result = new ArrayList<TilePosition>();
        TilePosition nextPos = new TilePosition(getTilePosition().x, getTilePosition().y);
        while (true) {
            nextPos = nextPos.add(direction);
            if (!nextPos.isLegal()) break;
            if (!isFree(nextPos)) {
                if (pieceIsOfOppositeColor(nextPos)) result.add(nextPos);
                break;
            }
            else result.add(nextPos);
        }
        return result;
    }


}
