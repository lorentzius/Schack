import java.io.*;

class Player {
    private final Board theBoard;
    private final Communicator theCommunicator;
    private final GUI theGUI;
    private final Start theStart;
    private final int STARTINGTIME = 180000; // in milliseconds
    private final int INCREMENT = 2000;
    public final TimeKeeper theTimeKeeper;
    private final PieceColor myColor;
    private boolean fogOfWar;
    private boolean drawIsBeingConsidered = false;
    private boolean drawHasBeenOffered = false;
    private String opponentTime;

    Player(boolean decideColor, boolean fogOfWar, ObjectInputStream input, ObjectOutputStream output, Start theStart) {
        this.theStart = theStart;
        this.fogOfWar = fogOfWar;
        theCommunicator = new Communicator(input, output, this);
        theTimeKeeper = new TimeKeeper(this, STARTINGTIME, INCREMENT);
        opponentTime = theTimeKeeper.timeToString(STARTINGTIME);
        myColor = getColor(decideColor);
        theBoard = new Board(myColor, this);
        theGUI = new GUI(this);
        checkInitials();
        theCommunicator.startToListenForMoves();
        showTime(false);
        Move.resetMoveNumber();
    }

    Board getBoard() {
        return theBoard;
    }

    Board.BoardGraphics getBoardGraphics() {
        return theBoard.getBoardGraphics();
    }

    boolean getFogOfWar() {
        return fogOfWar;
    }

    private PieceColor getColor(Boolean determineColor) {
        PieceColor myColor, opponentColor;
        myColor = (Math.random() > 0.5) ? PieceColor.White : PieceColor.Black;
        theCommunicator.sendColor(myColor);
        opponentColor = theCommunicator.getColor();
        if (!determineColor)
            myColor = opponentColor.opposite();
        return myColor;
    }

    public void sendCheat() {
        theCommunicator.sendCheat();
    }

    private void checkInitials() {
        InitialMessage message = new InitialMessage(fogOfWar, Start.VERSION);
        theCommunicator.sendInitialMessage(message);
        InitialMessage opponentsMessage = theCommunicator.getInitialMessage();
        if (!message.checkFogOfWar(opponentsMessage)) {
            theGUI.showDisagreement("Disagreeing on Fog of War");
            goFromBeginning();
        }
        if (!message.checkVersion(opponentsMessage)) {
            theGUI.showDisagreement("Incompatible versions. You have " + Start.VERSION +
                    " and opponent has " + opponentsMessage.getVersion());
            goFromBeginning();
        }
    }

    private void sendMoveToOpponent(Message message) {
        theTimeKeeper.incrementClock();
        if (drawIsBeingConsidered) {
            rejectDraw();
        }
        showTime(false);
        sendTime(theTimeKeeper.timeToString());
        theCommunicator.sendMove(message);
    }

    void sendMoveToOpponent(TilePosition source, TilePosition target, PieceType promotionType) {
        sendMoveToOpponent(new Message(source, target, promotionType));
    }

    void getMessageFromOpponent(Message message) {
        if (message.isCheat) {
            theTimeKeeper.cheat();
            theGUI.repaint();
            showTime(true);
            sendTime(theTimeKeeper.timeToString());
            return;
        }
        if (message.acceptDraw || (drawHasBeenOffered && message.offerDraw))
            drawByAgreement();
        else if (message.rejectDraw)
            myDrawOfferHasBeenRejected(message);
        else if (message.offerDraw)
            getDrawOffer();
        else if (message.resign)
            opponentResigned();
        else if (message.loseOnTime)
            showWinOnTime();
        else if (message.time != null)
            updateOpponentTime(message.time);
        else
            sendMoveToBoard(message);
    }

    private void sendMoveToBoard(Message message) {
        Move theMove = new Move(message.source, message.target, message.promotionType, theBoard);
        theBoard.incomingMoveFromOpponent(theMove);
    }

    void sendTime(String time) {
        theCommunicator.sendMove(new Message(time));
    }

    private void updateOpponentTime(String time) {
        opponentTime = time;
        showTime(false);
    }

    void showTime(boolean isRunning) {
        theGUI.showTime(isRunning, theTimeKeeper.timeToString(), opponentTime);
    }

    void startClock() {
        theTimeKeeper.start();
        showTime(true);
    }

    void stopClock() {
        theTimeKeeper.stop();
        showTime(false);
    }

    void stopGettingMoves() {
        theBoard.stopGettingMoves();
        theTimeKeeper.stop();
    }

    void record(String move) {
        theGUI.record(move);
    }

    void resign() {
        if (theGUI.confirmResign()) {
            Message message = new Message();
            message.resign();
            sendMoveToOpponent(message);
            showIResign();
        }
    }

    void loseOnTime() {
        Message message = new Message();
        message.loseOnTime();
        sendMoveToOpponent(message);
        showILoseOnTime();
    }

    void offerDraw() {
        drawHasBeenOffered = true;
        showIOfferDraw();
        theBoard.stopGettingMoves();
        recordDrawOffer(true);
        Message message = new Message();
        message.offerDraw();
        sendMoveToOpponent(message);
        theTimeKeeper.pause();
    }

    private void recordDrawOffer(boolean myOffer) {
        if ((myOffer && myColor == PieceColor.White)
                || (!myOffer && myColor == PieceColor.Black))
            theGUI.record("W offers draw\n");
        else
            theGUI.record("B offers draw\n");
    }

    void rejectDraw() {
        drawIsBeingConsidered = false;
        Message rejectMessage = new Message();
        rejectMessage.rejectDraw();
        theCommunicator.sendMove(rejectMessage);
        theGUI.showTitle();
    }

    void commError(String reason) {
        showOutOfSynch(reason);
    }

    private void opponentResigned() {
        showOpponentResigned();
    }

    private void drawByAgreement() {
        showDrawByAgreement();
    }

    private void getDrawOffer() {
        drawIsBeingConsidered = true;
        recordDrawOffer(false);
        showDrawOffer();
    }

    private void myDrawOfferHasBeenRejected(Message message) {
        theGUI.showTitle();
        drawHasBeenOffered = false;
        if (message.source != null) {
            sendMoveToBoard(message);
        }
        theBoard.reactivate();
        theTimeKeeper.restart();
    }

    void acceptDraw() {
        Message message = new Message();
        message.acceptDraw();
        sendMoveToOpponent(message);
        drawIsBeingConsidered = false;
        showDrawByAgreement();
    }

    private String outcome(boolean iWin) {
        if (iWin && myColor == PieceColor.White)
            return " 1 - 0\n";
        if (!iWin && myColor == PieceColor.Black)
            return " 1 - 0\n";
        return " 0 - 1\n";
    }

    void showWin() {
        theGUI.record(outcome(true));
        theGUI.showFinal("YOU WIN!");
    }

    void showDraw(String reason) {
        theGUI.record(" 1/2 - 1/2\n");
        theGUI.showFinal("DRAW! " + reason);
    }

    void showWinOnTime() {
        theGUI.record(outcome(true));
        theBoard.makeAllVisible();
        theGUI.showFinal("OPPONENT LOST ON TIME");
    }

    void showLose() {
        theGUI.record(outcome(false));
        theGUI.showFinal("YOU LOSE!");
    }

    void showILoseOnTime() {
        theGUI.record(outcome(false));
        theBoard.makeAllVisible();
        theGUI.showFinal("YOU LOSE ON TIME");
    }

    private void showIOfferDraw() {
        theGUI.showYouOfferDraw();
    }

    private void showDrawOffer() {
        theGUI.showDrawOffer();
    }

    private void showDrawByAgreement() {
        theGUI.record(" 1/2 - 1/2\n");
        theBoard.makeAllVisible();
        theGUI.showFinal("Drawn by agreement");
    }

    private void showIResign() {
        theGUI.record(outcome(false));
        theBoard.makeAllVisible();
        theGUI.showFinal("You have resigned");
    }

    private void showOpponentResigned() {
        theGUI.record(outcome(true));
        theBoard.makeAllVisible();
        theGUI.showFinal("Opponent has resigned");
    }

    private void showOutOfSynch(String s) {
        theGUI.record("Out of Synch " + s + "\n");
        theGUI.showFinal("Out of Synch" + s);
    }

    void goAgain() {
        theCommunicator.stopListening();
        theGUI.close();
        theStart.reStart();
    }

    void goFromBeginning() {
        theCommunicator.stopListening();
        theGUI.close();
        theStart.reStartFromBeginning();
    }
}