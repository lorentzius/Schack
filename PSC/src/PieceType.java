public enum PieceType {
    BISHOP('B'),
    KING('K'),
    KNIGHT('N'),
    PAWN('P'),
    QUEEN('Q'),
    ROOK('R');

    final char designator;
    PieceType(char c) {
        this.designator = c;
    }
}
