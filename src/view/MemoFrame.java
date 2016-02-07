package view;

import com.sun.mail.util.MailConnectException;
import controller.CustomOutputStream;
import model.UserInfo;

import javax.mail.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

public class MemoFrame extends JFrame implements ActionListener {

    private JPanel      infoPanel = new JPanel();
    private JPanel      functionalPanel = new JPanel();
    private JTextArea   infoArea = new JTextArea();
    private JScrollPane scrollPane = new JScrollPane(infoArea);
    private JTextField  commandField = new JTextField(15);
    private JButton     performBtn = new JButton("Perform");
    private Properties  props = new Properties();
    private Session     session;
    private UserInfo    userInfo;
    private Message[]   messages;
    private Store       store;
    private Folder      inbox;

    public MemoFrame() {
        super("Pop3 Client. Memo frame");
        this.getSwag();
        Toolkit   toolkit   = Toolkit.getDefaultToolkit();
        Dimension dimension = new Dimension(toolkit.getScreenSize());

        userInfo = new UserInfo("pop.gmail.com", "uran230@gmail.com", "Xm881xRm3", "pop3");

        infoArea.setEditable(false);
        performBtn.addActionListener(this);

        functionalPanel.setLayout(new FlowLayout());
        functionalPanel.add(commandField);
        functionalPanel.add(performBtn);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(scrollPane, BorderLayout.CENTER);
        infoPanel.add(functionalPanel, BorderLayout.SOUTH);

        this.setLocation((dimension.width / 8) * 3, dimension.height / 5);
        this.setSize(dimension.width / 4, dimension.height / 2);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getConnection();
        this.add(infoPanel);
        this.setVisible(true);
    }

    private void getAuthification() {
        infoArea.append("");
    }

    private void getConnection() {
        props.put("mail.host", "pop.gmail.com");
        props.put("mail.store.protocol", "pop3s");
        props.put("mail.pop3s.auth", "true");
        props.put("mail.pop3s.port", "995");

        try {
            session = Session.getInstance(props);
            store = session.getStore();
            store.connect(userInfo.getLogin(), userInfo.getPassword());

            inbox = store.getFolder("INBOX");
            if (inbox == null) {
                System.out.println("No INBOX");
                System.exit(1);
            }
            inbox.open(Folder.READ_WRITE);

            messages = inbox.getMessages();
            getList();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getSwag() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Nimbus is not available");
        }
    }

    public void getList() {
        infoArea.append("LIST\n");
        infoArea.append("+OK " + userInfo.getLogin() + " has " + messages.length + " messages\n");
    }

    public void closeConnection() {
        infoArea.append("CLOSE");
        try {
            inbox.close(true);
            store.close();
            infoArea.append("+OK connection closed\n");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void getStatistic() {
        infoArea.append("STAT\n");
        infoArea.append("+OK " + messages.length + " (" + getInboxSize() + ") octets\n");
    }


    public void deleteMsg(int number) {
        infoArea.append("DELE\n");
        try {
            messages[number].setFlag(Flags.Flag.DELETED, true);
            infoArea.append("+OK message " + number + " is marked as DELETE\n");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void retrieveMsg(int number) {
        infoArea.append("RETR\n");
        try {
            infoArea.append("Message " + number + "\n");
            infoArea.append("From : " + messages[number].getFrom()[0] + "\n");
            infoArea.append("Subject : " + messages[number].getSubject() + "\n");
            infoArea.append("Sent Date : " + messages[number].getSentDate() + "\n");
            Object content = messages[number].getContent();
            if (content instanceof String) {
                String body = (String) content;
                infoArea.append("Text : " + body + "\n");
            }
            infoArea.append("\n");
        } catch (MessagingException ex) {
            infoArea.append("-ERR something wrong with retrieving messages");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void testConnection() {
        infoArea.append("NOOP\n");
        try {
            Boolean isConnected = session.getTransport().isConnected();
            if (isConnected) {
                infoArea.append("+OK connection is stable\n");
            } else {
                infoArea.append("-ERR connection is lost\n");
            }
        } catch (NoSuchProviderException e) {
            infoArea.append("-ERR something wrong with connection\n");
            e.printStackTrace();
        }
    }

    public void showHelp() {
        infoArea.append("HELP\n");
        infoArea.append("USER, PASS, LIST, STAT, DELE, RETR, NOOP, QUIT\n");
    }

    public int getInboxSize() {
        int inboxSize = 0;
        for (Message message : messages) {
            try {
                inboxSize += message.getSize();
            } catch (MessagingException e) {
                infoArea.append("-ERR inbox size calculation problem\n");
                e.printStackTrace();
            }
        }
        return inboxSize;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == performBtn) {
            switch (commandField.getText().toUpperCase()) {
                case "LIST":
                    getList();
                    break;
                case "STAT":
                    getStatistic();
                    break;
                case "DELE": // TODO crydev: Delete message functionality
                    deleteMsg(1);
                    break;
                case "RETR": // TODO crydev: Retrieve message functionality
                    retrieveMsg(1);
                    break;
                case "NOOP":
                    testConnection();
                    break;
                case "HELP":
                    showHelp();
                    break;
                case "QUIT":
                    closeConnection();
                    break;
                default:
                    infoArea.append("-ERR no such command\n");
            }
        }
    }
}
