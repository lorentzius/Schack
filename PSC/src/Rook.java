class Rook extends LinePiece {

    Rook(Board board, TilePosition pos, PieceColor pc){
        super(board, pos, pc);
    }

    TilePosition[] getDirections() {
        return rookDirections;
    }
}
