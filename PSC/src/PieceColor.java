public enum PieceColor {
    White, Black;
    PieceColor opposite () {
        return (this == White) ? Black : White;
    }
}
