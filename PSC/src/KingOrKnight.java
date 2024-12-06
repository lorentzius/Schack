import java.util.ArrayList;

abstract class KingOrKnight extends Piece {

    KingOrKnight(Board board, TilePosition pos, PieceColor pc) {
        super(board, pos, pc);
    }

    ArrayList<TilePosition> computeAttacks(TilePosition[] displacements) {
        ArrayList<TilePosition> result = new ArrayList();
        for (TilePosition displacement : displacements) {
            TilePosition candidate = displacement.add(getTilePosition());
            if (isLegitimateTarget(candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

}
