import java.util.ArrayList;

class King extends KingOrKnight {
    private int castleShortDirection;
    private Board board;

    King(Board board, TilePosition pos, PieceColor pc) {
        super(board, pos, pc);
        castleShortDirection = board.orientationAsWhite() ? 1 : -1;
        this.board = board;
    }

    @Override
    ArrayList<TilePosition> computeAttacks() {
        TilePosition[] displacements = {p(1, 1), p(1, 0), p(1, -1),
                p(0, 1), p(0, -1),
                p(-1, 1), p(-1, 0), p(-1, -1)};
        ArrayList<TilePosition> attacks = super.computeAttacks(displacements);
        for (int displacement = -2; displacement < 5; displacement +=4) {
            TilePosition possibleDest = possibleCastleDestination(displacement);
            if (possibleDest != null) attacks.add(possibleDest);
        }
        return attacks;
    }

    @Override
    boolean canGoTo(TilePosition destination) {
        if (super.canGoTo(destination) && isCastle(destination))
            return findRookForCastle(destination).isMobile()
                    && isControlled(findRookDestination(destination))
                    && isControlled(destination);
        else return super.canGoTo(destination);
    }

    @Override
    void moveTo(TilePosition destination) {
        if (isCastle(destination)) moveRookForCastle(destination);
        super.moveTo(destination);
    }

    boolean isCastleShort(TilePosition destination) {
        return getTilePosition().x - destination.x == -2*castleShortDirection;
    }

    boolean isCastleLong(TilePosition destination) {
        return getTilePosition().x - destination.x == 2*castleShortDirection;
    }

    private int castleDirection(TilePosition castleDestination) {
        return getTilePosition().x < castleDestination.x ? 1 : -1;
    }

    private boolean isCastle(TilePosition destination) {
        return isCastleShort(destination) || isCastleLong(destination);
    }

    private Piece findRookForCastle(TilePosition destination) {
        int distanceToRook = isCastleShort(destination) ? 3 * castleShortDirection : -4 * castleShortDirection;
        return board.inhabitant(p(getTilePosition().x + distanceToRook, destination.y));
    }

    private TilePosition findRookDestination(TilePosition destination) {
        return p(getTilePosition().x + castleDirection(destination), destination.y);
    }

    private TilePosition findNextToRook(TilePosition destination) {
        int length = isCastleShort(destination) ? 2 * castleShortDirection : -3 * castleShortDirection;
        return p(getTilePosition().x + length, destination.y);
    }

    private TilePosition possibleCastleDestination(int kingDisplacement) {
        if (this.hasMoved()) return null;
        int xDestination = getTilePosition().x + kingDisplacement;
        TilePosition destination = p(xDestination, getTilePosition().y);
        Piece theRook = findRookForCastle(destination);
        if (theRook != null
                && !theRook.hasMoved()
                && isFree(destination)
                && isFree(findRookDestination(destination))
                && isFree(findNextToRook(destination))) {
            return destination;
        }
        else return null;
    }

    private void moveRookForCastle(TilePosition destination) {
        Piece theRook = findRookForCastle(destination);
        theRook.moveTo(findRookDestination(destination));
    }
}
