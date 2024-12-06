import java.util.ArrayList;
class Knight extends KingOrKnight {

    Knight(Board board, TilePosition pos, PieceColor pc) {
        super(board, pos, pc);
    }

    ArrayList<TilePosition> computeAttacks() {
        TilePosition[] displacements = {p(1, 2), p(-1, 2), p(1, -2), p(-1, -2),
                p(2, 1), p(-2, 1), p(2, -1), p(-2, -1)};
        return super.computeAttacks(displacements);
    }
}
