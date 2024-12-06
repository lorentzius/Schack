import static java.lang.System.arraycopy;

class Queen extends LinePiece {

    final private TilePosition[] queenDirections = new TilePosition[rookDirections.length + bishopDirections.length];

    Queen(Board board, TilePosition pos, PieceColor pc) {
        super(board, pos, pc);
        arraycopy(rookDirections,0,queenDirections,0, rookDirections.length);
        arraycopy(bishopDirections,0,queenDirections,rookDirections.length, bishopDirections.length);
    }

    TilePosition[] getDirections() {return queenDirections;}

}
