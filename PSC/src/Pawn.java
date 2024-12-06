import javax.swing.*;
import java.util.ArrayList;

class Pawn extends Piece {

    private Board board;
    private final int initialRow;
    private boolean justMovedTwoSquares = false;

    Pawn(Board board, TilePosition pos, PieceColor pc) {
        super(board, pos, pc);
        this.board = board;
        initialRow = isMine() ? 6 : 1;
    }

    @Override
    void moveTo(TilePosition destination) {
        if (Math.abs(this.getTilePosition().y - destination.y) > 1) {
            justMovedTwoSquares = true;
        }
        if (isEnPassant(destination)) removePawnForEnPassant(destination);
        super.moveTo(destination);
    }

    @Override
    PieceType getPromotionType(TilePosition target) {
        PieceType promotionType = null;
        if (target.isFinalRank())
            promotionType = promotionChoice();
        return promotionType;
    }

    @Override
    boolean canGoTo(TilePosition destination) {
        if (destination.y == getTilePosition().y) return false;
        if (destination.x != getTilePosition().x && isFree(destination)) {
            TilePosition enPassantVictim = p(destination.x, getTilePosition().y);
            return super.canGoTo(destination) && isControlled(enPassantVictim);
        }
        else return super.canGoTo(destination);
    }

    @Override
    ArrayList<TilePosition> computeAttacks() {
        final int row = getTilePosition().y;
        final int file = getTilePosition().x;
        final int rowInFront = isMine() ? row - 1 : row + 1;
        final int twoRowsInFront = isMine() ? row - 2 : row + 2;
        final ArrayList<TilePosition> result = new ArrayList();

        final TilePosition frontPos = new TilePosition(file, rowInFront);
        final TilePosition leftFrontPos = new TilePosition(file - 1, rowInFront);
        final TilePosition rightFrontPos = new TilePosition(file + 1, rowInFront);
        final TilePosition leftSidePos = new TilePosition(file - 1, row);
        final TilePosition rightSidePos = new TilePosition(file + 1, row);
        final TilePosition twoInFrontPos = new TilePosition(file, twoRowsInFront);

        if (isFree(frontPos)) result.add(frontPos);
        if (isLegitimateTarget(leftFrontPos) && !isFree(leftFrontPos)) result.add(leftFrontPos);
        if (isLegitimateTarget(rightFrontPos) && !isFree(rightFrontPos)) result.add(rightFrontPos);
        if (otherPawnJustMovedTwoSquares(leftSidePos)) {
            result.add(leftFrontPos);
            result.add(leftSidePos);
        }
        if (otherPawnJustMovedTwoSquares(rightSidePos)) {
            result.add(rightFrontPos);
            result.add(rightSidePos);
        }
        if (row == initialRow && isFree(twoInFrontPos) && isFree(frontPos)) result.add(twoInFrontPos);
        return result;
    }

    boolean isEnPassant(TilePosition destination) {
        return destination.x != getTilePosition().x && isFree(destination);
    }

    void resetJustMovedTwoSquares() {
        justMovedTwoSquares = false;
    }

    private void removePawnForEnPassant (TilePosition destination) {
        Tile tile = board.tile(new TilePosition(destination.x, getTilePosition().y));
        tile.getInhabitant().remove();
    }

    private PieceType promotionChoice() {
        final PieceType [] choices = {PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN};
        final int numberOfChoices = choices.length;
        final ImageIcon[] imageIcons = new ImageIcon[numberOfChoices];
        for (int possibleChoice=0; possibleChoice<numberOfChoices; possibleChoice++) {
            imageIcons [possibleChoice] = new ImageIcon(Piece.getPieceImage(getColor(), choices[possibleChoice].designator));
        }
        int defaultChoice = 3;
        int selectedValue = JOptionPane.showOptionDialog(board.getBoardGraphics(),
                null, "Promotion", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, new ImageIcon(),
                imageIcons, imageIcons[defaultChoice]);
        if (selectedValue < 0) selectedValue = defaultChoice;
        return choices[selectedValue];
    }

    private boolean otherPawnJustMovedTwoSquares(TilePosition pos) {
        if (!pos.isLegal()) return false;
        Piece thePiece = board.inhabitant(pos);
        return thePiece instanceof Pawn && ((Pawn) thePiece).justMovedTwoSquares &&
                pieceIsOfOppositeColor(pos);
    }

}


