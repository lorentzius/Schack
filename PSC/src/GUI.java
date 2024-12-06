import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Semaphore;
import java.io.*;
import javax.sound.sampled.*;

public class GUI {

    private final Player thePlayer;
    private final TextArea recording;
    private final JFrame theFrame = new JFrame("Parrow's Synchronous Chess " + Start.VERSION);
    private final JPanel topPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();
    private final JButton offerDrawButton = new JButton("Offer Draw");
    private final JButton resignButton = new JButton("Resign");
    private final JButton acceptButton = new JButton("Accept");
    private final JButton rejectButton = new JButton("Reject");
    private final JButton goButton = new JButton("Go again");
    private Clip alert;
    private boolean playAlertSound = false; // on some platforms playing sound does not work

    private Semaphore swingLock = new Semaphore(1);

    private void waitForSwingLock() {
        swingLock.acquireUninterruptibly();
    }

    private void releaseSwingLock() {
        swingLock.release();
    }

    GUI(Player thePlayer) {
        this.thePlayer = thePlayer;
        theFrame.setLayout(new BorderLayout());
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.add(thePlayer.getBoard().getBoardGraphics(), BorderLayout.CENTER);
        recording = new TextArea(10, 20);
        recording.setEditable(false);
        recording.setFont(new Font("Courier", Font.PLAIN, 16));
        recording.setBackground(new Color(230, 204, 179));
        theFrame.add(recording, BorderLayout.EAST);

        System.out.println("ADDING KEY LISTENER");
        theFrame.setFocusable(true);

        thePlayer.getBoard().getBoardGraphics().addKeyListener(thePlayer.theTimeKeeper); // Lägg till key listener på
// board graphics, funkar ej
// annars typ
        getAlertSound();
        setButtonListeners();
        theFrame.setVisible(true);
        showTitle();
    }

    private void getAlertSound() {
        try {
            InputStream alertInputStream = Main.class.getResourceAsStream("/alert.wav");
            BufferedInputStream bufferedAlertInputStream = new BufferedInputStream(alertInputStream);
            AudioInputStream audioAlert = AudioSystem.getAudioInputStream(bufferedAlertInputStream);
            alert = AudioSystem.getClip();
            alert.open(audioAlert);
            playAlertSound = true;
        } catch (Exception e) {
            System.out.println("No alert sound :(");
            playAlertSound = false;
            alert = null;
            System.out.println(e);
        }
        ;
    }

    private void setButtonListeners() {
        resignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thePlayer.resign();
            }
        });
        offerDrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thePlayer.offerDraw();
            }
        });
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thePlayer.acceptDraw();
            }
        });
        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                thePlayer.rejectDraw();
            }
        });
        goButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        thePlayer.goAgain();
                    }
                });
    }

    void close() {
        theFrame.dispose();
    }

    void playClick() {
        alert.setFramePosition(0);
        alert.start();
    }

    void record(String text) {
        recording.append(text);
        if (playAlertSound)
            playClick();
    }

    boolean confirmResign() {
        int reply = JOptionPane.showConfirmDialog(
                theFrame,
                "Are you sure you want to resign?",
                "Resignation?",
                JOptionPane.YES_NO_OPTION);
        return reply == JOptionPane.YES_OPTION;
    }

    void showDisagreement(String reason) {
        JOptionPane.showMessageDialog(theFrame, "Failed: " + reason);
    }

    void showTitle() {
        waitForSwingLock();
        topPanel.removeAll();
        topPanel.setBackground(Color.black);
        theFrame.add(topPanel, BorderLayout.NORTH);
        JLabel title = new JLabel("PSC: Parrow's Synchronous Chess");
        title.setForeground(Color.white);
        title.setFont(new Font("TimesRoman", Font.BOLD, 26));
        topPanel.add(title);
        topPanel.add(resignButton);
        topPanel.add(offerDrawButton);
        finalizeShow();
    }

    void showFinal(String verdict) {
        waitForSwingLock();
        topPanel.removeAll();
        JLabel theText = new JLabel(verdict);
        theText.setFont(new Font("TimesRoman", Font.BOLD, 32));
        topPanel.setBackground(Color.red);
        theText.setForeground(Color.white);
        topPanel.add(theText);
        thePlayer.stopGettingMoves();
        topPanel.add(goButton);
        finalizeShow();
    }

    private void showOffer(String reason) {
        waitForSwingLock();
        topPanel.removeAll();
        JLabel theText = new JLabel(reason);
        theText.setFont(new Font("TimesRoman", Font.BOLD, 26));
        topPanel.setBackground(Color.blue);
        theText.setForeground(Color.white);
        topPanel.add(theText);
    }

    void showDrawOffer() {
        showOffer("Draw?");
        topPanel.add(acceptButton);
        topPanel.add(rejectButton);
        finalizeShow();
    }

    void showYouOfferDraw() {
        showOffer("You Offer Draw");
        finalizeShow();
    }

    void showTime(Boolean isRunning, String time, String opponentTime) {
        waitForSwingLock();
        bottomPanel.removeAll();
        Color backgroundColor = isRunning ? Color.RED : Color.WHITE;
        bottomPanel.setBackground(backgroundColor);
        theFrame.add(bottomPanel, BorderLayout.SOUTH);
        JLabel myTime = new JLabel("Time left " + time);
        bottomPanel.setForeground(Color.black);
        myTime.setFont(new Font("TimesRoman", Font.BOLD, 36));
        JLabel oppTime = new JLabel(" Time left for opponent " + opponentTime);
        oppTime.setFont(new Font("TimesRoman", Font.BOLD, 24));
        bottomPanel.add(myTime);
        if (!thePlayer.getFogOfWar())
            bottomPanel.add(oppTime);
        finalizeShow();
    }

    public void repaint() {
        theFrame.repaint();
    }

    private void finalizeShow() {
        theFrame.pack();
        theFrame.repaint();
        releaseSwingLock();
    }
}
