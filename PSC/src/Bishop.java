class Bishop extends LinePiece {

    Bishop(Board board, TilePosition pos, PieceColor pc) {
        super(board, pos, pc);
    }

    TilePosition[] getDirections() {return bishopDirections;}

}
