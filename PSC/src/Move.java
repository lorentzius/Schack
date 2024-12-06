class Move {
    private final TilePosition source, target;
    private final Board board;
    private final Piece thePiece;
    private final PieceType promoteTo;
    private static int moveNumber;
    private final String separator;
    private final boolean isEnPassant;
    private final boolean isCastleShort;
    private final boolean isCastleLong;

    Move(TilePosition source, TilePosition target, PieceType promoteTo, Board board)     {
        this.source = source;
        this.target = target;
        this.board = board;
        this.promoteTo = promoteTo;
        thePiece = board.inhabitant(source);
        isEnPassant = thePiece instanceof Pawn && ((Pawn) thePiece).isEnPassant(target);
        isCastleShort = thePiece instanceof King && ((King)thePiece).isCastleShort(target);
        isCastleLong = thePiece instanceof King && ((King)thePiece).isCastleLong(target);
        if (!board.tile(target).isFree() || isEnPassant) separator = "x";
        else separator = "-";
        board.updateLameMoves(thePiece, target);
    }

    static void resetMoveNumber() {moveNumber = 1;}

    void execute() {
        resetAllJustMovedTwoSquares();
        thePiece.moveTo(target);
        if (isPromotion()) {promote();}
        resetMoveMarks();
    }

    String twoMovesToString(Move theMove) {
        String result;
        String myMove = this.toString();
        String otherMove = theMove.toString();
        if (board.getFogOfWar()) otherMove = "-------";
        if (thePiece.getColor() == PieceColor.Black) {
            result = otherMove + " "+myMove+"\n";
        }
        else result = myMove+" "+otherMove+"\n";
        result = moveNumberToString()+". "+ result;
        moveNumber ++;
        return result;
    }

    public String toString() {
        if (isCastleShort) {return "0-0   ";}
        if (isCastleLong) {return "0-0-0 ";}

        String result = "";
        if (!(thePiece instanceof Pawn)) result += thePiece.getDesignator();
        result += board.tile(source).getName();
        result += separator;
        result += board.tile(target).getName();
        if (isPromotion()) result += promoteTo.designator;
        else if (thePiece instanceof Pawn) result += " ";
        return result;
    }

    private boolean isPromotion() {
        return promoteTo != null;
    }

    private void promote() {
        Piece.pieceFactory(board, target, promoteTo, thePiece.getColor());
    }

    private void resetMoveMarks() {
        for (Tile tile : board.getAllTiles()) {
            tile.eraseMarkForLastMove();
        }
        board.tile(source).markForLastMove();
        board.tile(target).markForLastMove();
    }

    private void resetAllJustMovedTwoSquares() {
        for (Tile tile : board.getAllTiles()) {
            Piece inhabitant = tile.getInhabitant();
            if (inhabitant instanceof Pawn && thePiece.getColor() == inhabitant.getColor()) {
                ((Pawn)inhabitant).resetJustMovedTwoSquares();
            }
        }
    }

    private String moveNumberToString() {
        if (moveNumber < 10) return "  "+moveNumber;
        if (moveNumber < 100) return " "+moveNumber;
        return "" + moveNumber;
    }
}
