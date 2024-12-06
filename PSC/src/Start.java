import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

class Start {
    final static String VERSION = "1.2.1";
    private final JTextField IPField = new JTextField(20);
    private final JTextField portField = new JTextField(5);
    private JFrame theFrame ;
    private JPanel startPanel;
    private ServerSocket serverSocket;
    private InputStream is;
    private ObjectInputStream inputFromOpponent;
    private OutputStream os;
    private ObjectOutputStream outputToOpponent;
    private boolean isServer = false;
    boolean fogOfWar = false;

    private int port;
    private String IP;

    Start(String defaultIP, int defaultPort) {
        this.IP = defaultIP;
        this.port = defaultPort;
        setUpFirstFrame();
    }

    void reStart() {
        if (isServer) {
            waitForConnection();
        } else {
            new Start(IP, port);
        }
    }

    void reStartFromBeginning() {
        new Start(IP, port);
    }

    private void setUpFirstFrame() {
        theFrame = new JFrame("PSC: Parrow's Synchronous Chess " + VERSION);
        startPanel = new JPanel();
        startPanel.setPreferredSize(new Dimension(700,120));
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addFields();
        addButtons();
        theFrame.add(startPanel);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    private void addFields() {
        IPField.setEditable(true);
        IPField.setText(IP);
        portField.setText(String.valueOf(port));
        portField.setEditable(true);
        startPanel.add(new JLabel("EITHER Connect to IP:"));
        startPanel.add(IPField);
        startPanel.add(new JLabel("Port number:"));
        startPanel.add(portField);
        startPanel.add(new JLabel("OR accept incoming connection" ));
        }

    private void addButtons() {
        JButton ruleButton = new JButton("Show rules");
        ruleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRules();
            }
        });

        JButton goButton = new JButton("Connect");
        goButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                go();
            }
        });

        JButton acceptButton = new JButton("Accept");
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                waitForConnection();
            }
        });

        JCheckBox fogOfWarBox = new JCheckBox("Fog of War");
        fogOfWarBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                fogOfWar = !fogOfWar;
            }
        });

        startPanel.add(ruleButton);
        startPanel.add(goButton);
        startPanel.add(acceptButton);
        startPanel.add(fogOfWarBox);
    }

    private void go() {
        IP = IPField.getText();
        port = Integer.parseInt(portField.getText());
        try {
            startClient(IP, port);
        }
        catch (IOException e) {
            failedConnection();
        }
    }

    private void failedConnection() {
        JOptionPane.showMessageDialog(theFrame, "Connection failed");
    }

    private void failedServer() {
        JOptionPane.showMessageDialog(theFrame, "ServerSocket failed");
    }

    private void startClient(String IP, int port) throws IOException {
        Socket opponentSocket = new Socket(IP,port);
        theFrame.dispose();
        getStreams(opponentSocket);
        new Player(true, fogOfWar, inputFromOpponent, outputToOpponent, this);
        }

    private void getStreams(Socket socket) {
        try {
         is = socket.getInputStream();
         os = socket.getOutputStream();
         outputToOpponent = new ObjectOutputStream(os);
         inputFromOpponent = new ObjectInputStream(is);
         } catch (IOException e) {
            e.printStackTrace();
            System.out.println("CANNOT GET STREAMS");
         }
    }

    private void showRules() {
        JFrame ruleFrame = new JFrame("PSC rules");
        InputStream is =  getClass().getClassLoader().getResourceAsStream("PSC.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String text = reader.lines().collect(Collectors.joining("\n"));
        JTextArea ruleArea = new JTextArea(text);
        ruleArea.setEditable(false);
        ruleArea.setFont(new Font("TimesRoman", Font.PLAIN, 24));
        ruleArea.setBackground(new Color(230,204,179));
        JScrollPane ruleScrollArea = new JScrollPane(ruleArea);
        ruleScrollArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        ruleFrame.add(ruleScrollArea);
        ruleFrame.pack();
        ruleFrame.setVisible(true);
    }

    private void waitForConnection() {
        port = Integer.parseInt(portField.getText());
        isServer = true;
        theFrame.remove(startPanel);
        theFrame.setDefaultCloseOperation(WindowConstants. DO_NOTHING_ON_CLOSE);
        JPanel waitPanel = new JPanel();
        waitPanel.setPreferredSize(new Dimension(600,100));
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quit();
            }
        });
        String myIP;
        try {
            myIP = InetAddress.getLocalHost().getHostAddress();
        }
        catch (IOException e) {
            myIP = "unknown";
        }
        waitPanel.add(new JLabel ("Waiting for connection on IP "
                +myIP+" on port "+port));
        waitPanel.add(quitButton);
        theFrame.add(waitPanel);
        theFrame.pack();
        theFrame.setVisible(true);

        new StartServer().start();
    }

    private class StartServer extends Thread {
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();
                serverSocket.close();
                theFrame.dispose();
                getStreams(socket);
                new Player(false, fogOfWar, inputFromOpponent, outputToOpponent, Start.this);
            } catch (IOException e) {
                closeServer();
            }
        }
    }

    private void closeServer () {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            else failedServer();
        }
        catch (IOException e) {
            System.out.println("Error in closing");
        }
        System.exit(0);
    }

    private void quit() {
        //System.out.println("Quitting!");
        closeServer();
    }
}
