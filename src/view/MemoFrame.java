package view;

import model.UserInfo;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemoFrame extends JFrame implements ActionListener {

    private JPanel         infoPanel          = new JPanel();
    private JPanel         functionalPanel    = new JPanel();
    private JTextArea      infoArea           = new JTextArea();
    private JScrollPane    scrollPane         = new JScrollPane(infoArea);
    private JTextField     commandField       = new JTextField(15);
    private JButton        performBtn         = new JButton("Perform");
    private Properties     props              = new Properties();
    private UserInfo       userInfo;
    private Boolean        isConnected;
    private Message[]      messages;
    private Store          store;
    private Folder         inbox;

    public MemoFrame() {
        super("Pop3 Client. Memo frame");
        Toolkit   toolkit   = Toolkit.getDefaultToolkit();
        Dimension dimension = new Dimension(toolkit.getScreenSize());

        userInfo = new UserInfo("pop.gmail.com", "", "", "pop3");

        infoArea.setEditable(false);
        performBtn.addActionListener(this);

        functionalPanel.setLayout(new FlowLayout());
        functionalPanel.add(commandField);
        functionalPanel.add(performBtn);
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(scrollPane, BorderLayout.CENTER);
        infoPanel.add(functionalPanel, BorderLayout.SOUTH);

        this.getSwag();
        this.setLocation((dimension.width / 8) * 3, dimension.height / 5);
        this.setSize(dimension.width / 4, dimension.height / 2);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.add(infoPanel);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
    }

    private void getConnection() {
        props.put("mail.host", "pop.gmail.com");
        props.put("mail.store.protocol", "pop3s");
        props.put("mail.pop3s.auth", "true");
        props.put("mail.pop3s.port", "995");

        try {
            Session session = Session.getInstance(props);
            store = session.getStore();
            store.connect(userInfo.getLogin(), userInfo.getPassword());
            inbox = store.getFolder("INBOX");

            if (inbox == null) {
                System.out.println("No INBOX");
                System.exit(1);
            }
            inbox.open(Folder.READ_WRITE);
            messages = inbox.getMessages();
            isConnected = true;

            infoArea.append("+OK " + userInfo.getLogin() + "'s maildrop has " + messages.length
                    + " messages (" + getInboxSize() + ") octets\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            infoArea.append("-ERR cannot get inbox messages\n");
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
    
    private void getAuthorisation(String login) {
        infoArea.append("USER\n");
        userInfo.setLogin(login);
        Boolean isValid = crunchyEmailValidator(login);
        if (isValid) {
            infoArea.append("+OK " + login + " is a real hoopy frood\n");
        } else {
            infoArea.append("-ERR check email address\n");
        }
    }

    private void getPassword(String password) {
        infoArea.append("PASS\n");
        userInfo.setPassword(password);
        getConnection();
    }

    public void getList() {
        infoArea.append("LIST\n");
        infoArea.append("+OK " + userInfo.getLogin() + " has " + messages.length + " messages\n");
    }

    public void closeConnection() {
        infoArea.append("QUIT\n");
        try {
            inbox.close(true);
            store.close();
            isConnected = false;
            infoArea.append("+OK connection closed\n");
        } catch (MessagingException e) {
            infoArea.append("-ERR something gone wrong\n");
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
            String textFromMessage = null;
            try {
                textFromMessage = this.getTextFromMessage(messages[number]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            infoArea.append("Message " + number + "\n");
            infoArea.append("From : " + messages[number].getFrom()[0] + "\n");
            infoArea.append("Subject : " + messages[number].getSubject() + "\n");
            infoArea.append("Sent Date : " + messages[number].getSentDate() + "\n");
            infoArea.append("Text : " + textFromMessage + "\n");
            infoArea.append("--- END OF MESSAGE ---\n");
        } catch (MessagingException ex) {
            infoArea.append("-ERR something wrong with retrieving messages\n");
            ex.printStackTrace();
        }
    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")){
            return message.getContent().toString();
        }else if (message.isMimeType("multipart/*")) {
            String result = "";
            MimeMultipart mimeMultipart = (MimeMultipart)message.getContent();
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i ++){
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")){
                    result = result + "\n" + bodyPart.getContent();
                    break;
                } else if (bodyPart.isMimeType("text/html")){
                    String html = (String) bodyPart.getContent();
                    result = result + "\n" + Jsoup.parse(html).text();
                }
            }
            return result;
        }
        return "";
    }

    public void testConnection() {
        infoArea.append("NOOP\n");
        if (store.isConnected()) {
            infoArea.append("+OK connection is active\n");
        } else {
            infoArea.append("-ERR connection lost\n");
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

    private boolean crunchyEmailValidator(String email) {
        boolean isValid = false;
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            isValid = true;
        } catch (AddressException e) {
            System.out.println("You are in catch block -- Exception Occurred for: " + email);
        }
        return isValid;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == performBtn) {
            String  commandPart = commandField.getText();
            String  variablePart = null;
            Integer numberOfMessage = 1;

            Pattern pattern = Pattern.compile("\\s");
            Matcher matcher = pattern.matcher(commandPart);
            Boolean found = matcher.find();

            if (found) {
                String[] parts = commandPart.split("\\s");
                commandPart = parts[0];
                variablePart = parts[1];
                try {
                    numberOfMessage = Integer.parseInt(variablePart);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }

            switch (commandPart.toUpperCase()) {
                case "USER":
                    getAuthorisation(variablePart);
                    break;
                case "PASS":
                    getPassword(variablePart);
                    break;
                case "LIST":
                    getList();
                    break;
                case "STAT":
                    getStatistic();
                    break;
                case "DELE":
                    deleteMsg(numberOfMessage);
                    break;
                case "RETR":
                    retrieveMsg(numberOfMessage);
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
                    infoArea.append("-ERR \"" + commandPart + "\" no such command\n");
            }
        }
    }
}
