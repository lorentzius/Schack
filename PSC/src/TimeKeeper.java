import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class TimeKeeper implements ActionListener, KeyListener {
    private Player thePlayer;
    private int clock;
    private int increment;
    private int clockStep = 100;
    private Timer timer = new Timer(clockStep, this);
    private boolean timerIsRunning, timerIsPaused;

    private final String CHEAT = "cheaton";
    private String lastTyped = "";


    TimeKeeper(Player thePlayer, int clock, int increment) {
        this.thePlayer = thePlayer;
        this.clock = clock;
        this.increment = increment;
    }

    void incrementClock() {
        clock += increment;
    }

    public void cheat() {
        this.clock /= 2;
    }

    void start() {
        timer.start();
        timerIsRunning = true;
    }

    void stop() {
        timer.stop();
        timerIsRunning = false;
    }

    void pause() {
        timer.stop();
        timerIsPaused = timerIsRunning;
    }

    void restart() {
        if (timerIsPaused)
            timer.start();
    }

    String timeToString(int time) {
        int minutes = time / 60000;
        int seconds = (time % 60000) / 1000;
        int secondsFirstDigit = seconds / 10;
        int secondsSecondDigit = seconds % 10;
        String result = "";
        if (minutes > 0)
            result += minutes;
        result += ":" + secondsFirstDigit;
        result += secondsSecondDigit;
        return result;
    }

    String timeToString() {
        return timeToString(clock);
    }

    public void actionPerformed(ActionEvent e) {
        clock -= clockStep;
        if (clock % 1000 < clockStep) {
            thePlayer.showTime(true);
            thePlayer.sendTime(timeToString());
        }
        if (clock < 0) {
            timer.stop();
            thePlayer.loseOnTime();
        }
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        char currentChar = e.getKeyChar();

        if (currentChar == 'c') {
            lastTyped = "";
        }
        lastTyped += currentChar;
        if (lastTyped.length() >=7 ) {
            if (lastTyped.equals(CHEAT)) {
                thePlayer.sendCheat();
            }
            lastTyped = "";
        }

    }

        public void keyReleased(KeyEvent e) {
        }
    }


    //long currentTime = System.currentTimeMillis();
    //long lastTypedTime = currentTime;
      //  if ((currentTime-lastTypedTime) >= 5000) {
          //      lastTyped="";
            //    }

