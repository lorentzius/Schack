import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

class Board {

    static final int TILESIZE = BoardGraphics.TILESIZE;
    final private PieceColor orientation;
    final private Tile[][] tileTable = new Tile[8][8];
    final private ArrayList<Tile> allTiles = new ArrayList();
    final private Player thePlayer;
    final private ComputeControl theComputeControl = new ComputeControl();
    final private BoardGraphics boardGraphics = new BoardGraphics();
    final private Semaphore lock = new Semaphore(1); // This might not be needed...
    private King myKing, opponentKing;
    private Move opponentMove, myMove;
    private int numberOfLameMoves = 0;
    private boolean gettingMoves = false;
    final private int maxNumberOfLameMoves = 50;

    Board(PieceColor myColor, Player thePlayer) {
        this.thePlayer = thePlayer;
        this.orientation = myColor;
        setUpTiles();
        createStartPosition();
// createAlternateStartPosition(); // for debugging purposes
// createAlternateStartPositionTestPromotions();
// createCastleTestStartPosition();
        theComputeControl.computeControls();
        boardGraphics.repaint();
        startGettingMoves();
    }

    private void setUpTiles() {
        for (int fileNumber = 0; fileNumber < 8; fileNumber++) {
            for (int rowNumber = 0; rowNumber < 8; rowNumber++) {
                Tile tile = new Tile(new TilePosition(fileNumber, rowNumber), tileName(fileNumber, rowNumber));
                tileTable[fileNumber][rowNumber] = tile;
                allTiles.add(tile);
            }
        }
    }

    private String tileName(int x, int y) {
        final int file = orientationAsWhite() ? x : 7 - x;
        final int row = orientationAsWhite() ? y : 7 - y;
        final String rows = "87654321";
        final String files = "abcdefgh";
        return String.valueOf(files.charAt(file)) + rows.charAt(row);
    }

    private void createStartPosition() {
        PieceColor opponentColor = orientation.opposite();
        PieceType[] initialRow = { PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK };
        int kingFile = 4;
        if (!orientationAsWhite()) {
            initialRow[3] = PieceType.KING;
            initialRow[4] = PieceType.QUEEN;
            kingFile = 3;
        }
        int file = 0;
        for (PieceType type : initialRow) {
            Piece.pieceFactory(this, new TilePosition(file, 0), type, opponentColor);
            Piece.pieceFactory(this, new TilePosition(file, 1), PieceType.PAWN, opponentColor);
            Piece.pieceFactory(this, new TilePosition(file, 6), PieceType.PAWN, orientation);
            Piece.pieceFactory(this, new TilePosition(file, 7), type, orientation);
            file++;
        }
        myKing = (King) inhabitant(new TilePosition(kingFile, 7));
        opponentKing = (King) inhabitant(new TilePosition(kingFile, 0));
    }

    boolean orientationAsWhite() {
        return orientation == PieceColor.White;
    }

    Tile tile(TilePosition pos) {
        return tileTable[pos.x][pos.y];
    }

    Piece inhabitant(TilePosition pos) {
        return tile(pos).getInhabitant();
    }

    boolean isControlled(TilePosition pos) {
        return tile(pos).isControlled();
    }

    boolean isControlledByOpponent(TilePosition pos) {
        return tile(pos).isControlledByOpponent();
    }

    boolean isFree(TilePosition pos) {
        return tile(pos).isFree();
    }

    ArrayList<Tile> getAllTiles() {
        return allTiles;
    }

    boolean getFogOfWar() {
        return thePlayer.getFogOfWar();
    }

    BoardGraphics getBoardGraphics() {
        return boardGraphics;
    }

    void startGettingMoves() {
        if (!gettingMoves) {
            boardGraphics.startListening();
            gettingMoves = true;
        }
    }

    void stopGettingMoves() {
        boardGraphics.stopListening();
        gettingMoves = false;
    }

    void reactivate() {
        if (!iHaveMoved())
            startGettingMoves();
    }

    void updateLameMoves(Piece thePiece, TilePosition target) {
        if (isLameMove(thePiece, target))
            numberOfLameMoves++;
        else
            numberOfLameMoves = 0;
    }

    void makeAllVisible() {
        for (Tile tile : allTiles) {
            tile.makeVisible();
        }
        boardGraphics.repaint();
    }

    void incomingMoveFromOpponent(Move theMove) {
        if (iHaveMoved()) {
            waitForLock();
            theMove.execute();
            record(myMove, theMove);
            myMove = null;
            theComputeControl.computeControls();
            boardGraphics.repaint();
            startGettingMoves();
            releaseLock();
        } else {
            opponentMove = theMove;
            thePlayer.startClock();
        }
    }

    private void makeMyMove(TilePosition target, Piece thePiece) {
        if (thePiece.canGoTo(target)) {
            PieceType promoteTo = thePiece.getPromotionType(target);
            TilePosition source = thePiece.getTilePosition();
            Move theMove = new Move(source, target, promoteTo, this);
            thePlayer.sendMoveToOpponent(source.transpose(), target.transpose(), promoteTo);
            theMove.execute();
            if (opponentHasMoved()) {
                opponentMove.execute();
                record(theMove, opponentMove);
                opponentMove = null;
                thePlayer.stopClock();
                theComputeControl.computeControls();
            } else {
                stopGettingMoves();
                myMove = theMove;
            }
        }
        boardGraphics.repaint();
    }

    private boolean opponentHasMoved() {
        return opponentMove != null;
    }

    private boolean iHaveMoved() {
        return myMove != null;
    }

    private void waitForLock() {
        lock.acquireUninterruptibly();
    }

    private void releaseLock() {
        lock.release();
    }

    private boolean isLameMove(Piece thePiece, TilePosition target) {
        return !(thePiece instanceof Pawn) && isFree(target);
    }

    private void record(Move move1, Move move2) {
        thePlayer.record(move1.twoMovesToString(move2));
    }

///////////////////////////////////////////////////////////////////////////////////////////////////

    class BoardGraphics extends JPanel {
        static final int TILESIZE = 60;
        private MyMouseListener theMouseListener = new MyMouseListener();

        BoardGraphics() {
            setPreferredSize(new Dimension(TILESIZE * 8, TILESIZE * 8));
            setUpControlShowToggle();
        }

        private void startListening() {
            addMouseListener(theMouseListener);
            addMouseMotionListener(theMouseListener);

        }

        private void stopListening() {
            removeMouseListener(theMouseListener);
            removeMouseMotionListener(theMouseListener);
        }

        private void setUpControlShowToggle() {
            addKeyListener(
                    new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            if (e.getKeyCode() == KeyEvent.VK_SPACE && !thePlayer.getFogOfWar()) {
                                for (Tile tile : allTiles) {
                                    tile.toggleShowControl();
                                }
                                repaint();
                            }
                        }
                    });
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Tile tile : allTiles) {
                tile.paint(g);
            }
            if (theMouseListener.isDragging())
                theMouseListener.currentlyDraggedPiece.paint(g);
            if (iHaveMoved()) {
                g.setColor(new Color(100, 100, 100, 100));
                g.fillRect(0, 0, TILESIZE * 8, TILESIZE * 8);
            }
            requestFocus();
            setFocusable(true);

        }

        private class MyMouseListener implements MouseListener, MouseMotionListener {

            private Piece currentlyDraggedPiece = null;

            private boolean isDragging() {
                return currentlyDraggedPiece != null;
            }

            private void removeCurrentlyDraggedPiece() {
                if (isDragging()) {
                    currentlyDraggedPiece.makeNotDragged();
                    currentlyDraggedPiece = null;
                }
            }

            public void mouseMoved(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent mouseEvent) {
                waitForLock();
                Tile tile = getTile(mouseEvent);
                if (tile.hasMovablePiece()) {
                    currentlyDraggedPiece = tile.getInhabitant();
                    currentlyDraggedPiece.makeDragged();
                    placeCurrentlyDraggedPiece(mouseEvent);
                }
                releaseLock();
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                waitForLock();
                if (isDragging()) {
                    TilePosition target = getDraggedPosition(mouseEvent).tilePosition();
                    makeMyMove(target, currentlyDraggedPiece);
                    removeCurrentlyDraggedPiece();
                }
                releaseLock();
            }

            public void mouseDragged(MouseEvent mouseEvent) {
                waitForLock();
                placeCurrentlyDraggedPiece(mouseEvent);
                releaseLock();
            }

            private void placeCurrentlyDraggedPiece(MouseEvent mouseEvent) {
                if (isDragging()) {
                    currentlyDraggedPiece.setDraggedPosition(
                            new DraggedPosition(mouseEvent.getX() - TILESIZE / 2,
                                    mouseEvent.getY() - TILESIZE / 2));
                    repaint();
                }
            }

            private DraggedPosition getDraggedPosition(MouseEvent mouseEvent) {
                return new DraggedPosition(mouseEvent.getX(), mouseEvent.getY());
            }

            private Tile getTile(MouseEvent mouseEvent) {
                TilePosition pos = getDraggedPosition(mouseEvent).tilePosition();
                return tile(pos);
            }
        }
    }

    private class ComputeControl {

        private boolean myKingIsAttacked, opponentKingIsAttacked;

        private void attackFrom(Tile tile) {
            if (!tile.isFree()) {
                Piece attackingPiece = tile.getInhabitant();
                ArrayList<TilePosition> attackedPositions = attackingPiece.computeAttacks();
                attackedPositions.add(attackingPiece.getTilePosition()); // attacking the square where it sits
                int increment = attackingPiece.isMine() ? 1 : -1;
                for (TilePosition attackedPos : attackedPositions) {
                    tile(attackedPos).addControl(increment);
                    Piece attackedPiece = tile(attackedPos).getInhabitant();
                    if (attackingPiece != myKing && attackedPiece == myKing)
                        myKingIsAttacked = true;
                    if (attackingPiece != opponentKing && attackedPiece == opponentKing)
                        opponentKingIsAttacked = true;
                    if (attackingPiece.isMine())
                        tile(attackedPos).makeVisible();
                }
            }
        }

        private void computeControls() {
            myKingIsAttacked = false;
            opponentKingIsAttacked = false;
            for (Tile tile : allTiles) {
                tile.setZeroControl();
                if (getFogOfWar())
                    tile.makeInvisible();
                else
                    tile.makeVisible();
            }
            for (Tile tile : allTiles) {
                attackFrom(tile);
            }
            appraise();
        }

        private boolean stalemated(boolean checkMyPieces) {
            for (Tile tile : allTiles) {
                if (!tile.isFree()) {
                    Piece inhabitant = tile.getInhabitant();
                    if (inhabitant.isMine() == checkMyPieces) {
                        if (inhabitant.canMoveAtAll()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private boolean passedLimitOfLameMoves() {
            return numberOfLameMoves / 2 >= maxNumberOfLameMoves;
        }

        private void appraise() {
            boolean myKingLost = !myKing.isMobile();
            boolean opponentKingLost = !opponentKing.isMobile();
            boolean iAmStalemated = stalemated(true);
            boolean opponentIsStalemated = stalemated(false);
            boolean iWin = opponentKingLost || (opponentIsStalemated && opponentKingIsAttacked);
            boolean iLose = myKingLost || (iAmStalemated && myKingIsAttacked);
            boolean gameEnds = iWin || iLose || iAmStalemated || opponentIsStalemated || passedLimitOfLameMoves();
            if (gameEnds) {
                makeAllVisible();
                boardGraphics.repaint();
                if (iWin && iLose)
                    thePlayer.showDraw("Both lose");
                else if (iLose)
                    thePlayer.showLose();
                else if (iWin)
                    thePlayer.showWin();
                else if (iAmStalemated || opponentIsStalemated)
                    thePlayer.showDraw("Stalemate");
                else if (passedLimitOfLameMoves())
                    thePlayer.showDraw(maxNumberOfLameMoves + " move rule");
            }
        }
    }

////////////// for debugging purposes

    private void createCastleTestStartPosition() {
        int kingFile = orientationAsWhite() ? 4 : 3;
        PieceColor opponentColor = orientation.opposite();

        Piece.pieceFactory(this, new TilePosition(0, 0), PieceType.ROOK, opponentColor);
        Piece.pieceFactory(this, new TilePosition(7, 0), PieceType.ROOK, opponentColor);
        Piece.pieceFactory(this, new TilePosition(0, 7), PieceType.ROOK, orientation);
        Piece.pieceFactory(this, new TilePosition(7, 7), PieceType.ROOK, orientation);
        Piece.pieceFactory(this, new TilePosition(kingFile, 0), PieceType.KING, opponentColor);
        Piece.pieceFactory(this, new TilePosition(kingFile, 7), PieceType.KING, orientation);
        Piece.pieceFactory(this, new TilePosition(1, 0), PieceType.KNIGHT, opponentColor);
        Piece.pieceFactory(this, new TilePosition(6, 0), PieceType.KNIGHT, opponentColor);
        Piece.pieceFactory(this, new TilePosition(1, 7), PieceType.KNIGHT, orientation);
        Piece.pieceFactory(this, new TilePosition(6, 7), PieceType.KNIGHT, orientation);

        myKing = (King) inhabitant(new TilePosition(kingFile, 7));
        opponentKing = (King) inhabitant(new TilePosition(kingFile, 0));
    }

    private void createAlternateStartPosition() {
        int kingFile = 7;
        if (!orientationAsWhite()) {
            kingFile = 0;
        }
        PieceColor opponentColor = orientation.opposite();
        Piece.pieceFactory(this, new TilePosition(kingFile, 0), PieceType.KING, opponentColor);
        Piece.pieceFactory(this, new TilePosition(kingFile, 7), PieceType.KING, orientation);
        myKing = (King) inhabitant(new TilePosition(kingFile, 7));
        myKing.setHasMoved();
        opponentKing = (King) inhabitant(new TilePosition(kingFile, 0));
        opponentKing.setHasMoved();
        Piece.pieceFactory(this, new TilePosition(kingFile, 2), PieceType.KNIGHT, opponentColor);
        Piece.pieceFactory(this, new TilePosition(kingFile, 3), PieceType.KNIGHT, opponentColor);
        Piece.pieceFactory(this, new TilePosition(kingFile, 4), PieceType.KNIGHT, orientation);
        Piece.pieceFactory(this, new TilePosition(kingFile, 5), PieceType.KNIGHT, orientation);
        Piece.pieceFactory(this, new TilePosition(3, 1), PieceType.PAWN, opponentColor);
        Piece.pieceFactory(this, new TilePosition(2, 1), PieceType.PAWN, opponentColor);
        Piece.pieceFactory(this, new TilePosition(4, 6), PieceType.PAWN, orientation);
        Piece.pieceFactory(this, new TilePosition(5, 6), PieceType.PAWN, orientation);
    }

    private void createAlternateStartPositionTestPromotions() {
        int kingFile = 4;
        int pawnFile = 1;
        if (!orientationAsWhite()) {
            kingFile = 3;
            pawnFile = 6;
        }
        PieceColor opponentColor = orientation.opposite();
        opponentKing = (King) Piece.pieceFactory(this, new TilePosition(kingFile, 0), PieceType.KING, opponentColor);
        myKing = (King) Piece.pieceFactory(this, new TilePosition(kingFile, 7), PieceType.KING, orientation);
        Piece.pieceFactory(this, new TilePosition(pawnFile, 6), PieceType.PAWN, opponentColor);
        Piece.pieceFactory(this, new TilePosition(pawnFile, 1), PieceType.PAWN, orientation);
    }

    private void createTestPos() {
        PieceColor opponentColor = orientation.opposite();
        TilePosition WKingPos = new TilePosition(2, 6);
        TilePosition BKingPos = new TilePosition(0, 7);
        TilePosition WQPos = new TilePosition(1, 4);
        if (!orientationAsWhite()) {
            WKingPos = WKingPos.transpose();
            WQPos = WQPos.transpose();
            BKingPos = BKingPos.transpose();
            myKing = (King) Piece.pieceFactory(this, BKingPos, PieceType.KING, PieceColor.Black);
            opponentKing = (King) Piece.pieceFactory(this, WKingPos, PieceType.KING, PieceColor.White);
            Piece.pieceFactory(this, WQPos, PieceType.QUEEN, PieceColor.White);
        } else {
            myKing = (King) Piece.pieceFactory(this, WKingPos, PieceType.KING, PieceColor.White);
            opponentKing = (King) Piece.pieceFactory(this, BKingPos, PieceType.KING, PieceColor.Black);
            Piece.pieceFactory(this, WQPos, PieceType.QUEEN, PieceColor.White);
        }
    }

///////////////
}