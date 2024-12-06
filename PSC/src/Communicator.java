import java.io.*;

public class Communicator {

    private ObjectInputStream inputFromOpponent;
    private ObjectOutputStream outputToOpponent;
    private final Player thePlayer;
    private MoveListener myMoveListener = new MoveListener();

    Communicator(ObjectInputStream is, ObjectOutputStream os, Player thePlayer) {
        this.thePlayer = thePlayer;
        this.inputFromOpponent = is;
        this.outputToOpponent = os;
    }

    InitialMessage getInitialMessage() {
        try {
            InitialMessage message = (InitialMessage) inputFromOpponent.readObject();
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            thePlayer.commError(" failed initial message");
        }
        return null;
    }

    void sendInitialMessage(InitialMessage message) {
        try {
            outputToOpponent.writeObject(message);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            thePlayer.commError(" sending initial message");
        }
    }

    PieceColor getColor() {
        try {
            PieceColor color = (PieceColor) inputFromOpponent.readObject();
            return color;
        } catch (Exception e) {
            e.printStackTrace();
            thePlayer.commError(" setting up colors");
        }
        return null;
    }

    void sendColor(PieceColor color) {
        try {
            outputToOpponent.writeObject(color);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            thePlayer.commError(" sending color");
        }
    }

    void sendCheat() {
        try {
            Message msg = new Message();
            msg.cheat();
            outputToOpponent.writeObject(msg);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            thePlayer.commError(" sending cheat");
        }
    }

    void sendMove(Message message) {
        try {
            outputToOpponent.writeObject(message);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            thePlayer.commError(" sending move");
        }
    }

    void startToListenForMoves() {
        myMoveListener.start();
    }

    void stopListening() {
        myMoveListener.stopListening();
    }

    private class MoveListener extends Thread {

        boolean running = true;

        public void run() {
            Message message = null;
            Object inputObject;
            while (running) {
                try {

                    try {
                        inputObject = inputFromOpponent.readObject();
                        message = (Message) inputObject;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        running = false;
                    } catch (EOFException e) {
                        running = false;
                    }
                    if (running && message != null) {
                        thePlayer.getMessageFromOpponent(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    running = false;
                }
            }
        }

        void stopListening() {
            running = false;
        }
    }
}