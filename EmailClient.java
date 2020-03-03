import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Main class and GUI. Puts together the separate components functionally and
 * visually.
 * 
 * @author Ivar Lund ivlu1468 ivarnilslund@gmail.com
 * 
 */
@SuppressWarnings("serial")
public class EmailClient extends JFrame {

	private Properties properties = new Properties();
	private Session session;
	private EmailFetcher fetchEmails;
	private Message[] inbox;
	private JPanel center;

	/**
	 * Class constructor. Sets up JFrame container for MailSender.
	 */
	public EmailClient() {
		super("Gmail Client | Server: smtp.gmail.com");
		setLayout(new BorderLayout());

		center = new JPanel();
		center.setLayout(new GridLayout(0, 1, 5, 0));

		JScrollPane pane = new JScrollPane(center);

		JButton inbox = new JButton("Inbox");
		JButton composeBtn = new JButton("Compose+");

		inbox.addActionListener(new InboxListener());
		composeBtn.addActionListener(new ComposeListener());

		Component horizontalStrut = Box.createHorizontalStrut(20);
		Component verticalStrut = Box.createVerticalStrut(20);

		add(pane, BorderLayout.CENTER);
		add(verticalStrut, BorderLayout.NORTH);
		add(horizontalStrut, BorderLayout.EAST);
		add(composeBtn, BorderLayout.SOUTH);
		add(inbox, BorderLayout.WEST);

		setSize(720, 540);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		login();
	}

	/**
	 * Sets up properties for mail transceiving. Gets server, username and password
	 * Strings from JTextFields
	 */
	private void setProperties(String username) {
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("username", username);
		properties.put("mail.pop3s.host", "pop.gmail.com");
		properties.put("mail.pop3s.port", "995");
		properties.put("mail.pop3s.starttls.enable", "true");
	}

	/**
	 * Sets up the session used for sending email. Creates an Authenticator object
	 * from JTextField's
	 */
	private void setMailSession(String username, String password) {
		session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
	}

	/**
	 * Login method. Puts Login class in an OptionDialog and tests credentials
	 * towards gmail server.
	 */
	private void login() {
		Login login = new Login();
		Object[] options = { "Login", "Exit" };
		int answer = JOptionPane.showOptionDialog(EmailClient.this, login, "Login", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if (answer != JOptionPane.OK_OPTION) {
			System.exit(0);
		} else {
			String username = login.getUsername();
			String password = login.getPassword();
			setProperties(username);
			setMailSession(username, password);
			fetchEmails = new EmailFetcher(session);
			try {
				Transport checkCredentials;
				checkCredentials = session.getTransport("smtp");
				checkCredentials.connect("smtp.gmail.com", username, password);
				checkCredentials.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Wrong username or password", "Authentication error",
						JOptionPane.ERROR_MESSAGE);
				login();
			}
		}
	}

	/**
	 * Message creation method. Puts EmailTransmitter class in a OptionDialog and
	 * sends message when confirmed. Provides messageDialogs to inform user if
	 * sending was successful or not.
	 */
	private void composeMessage() {
		EmailTransmitter et = new EmailTransmitter(properties, session);

		Object[] option = { "Send", "Cancel" };
		int answer = JOptionPane.showOptionDialog(EmailClient.this, et, "Compose new mail",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, option, option[0]);

		if (answer != JOptionPane.OK_OPTION) {
			return;
		} else {
			try {
				et.sendMsg();
				JOptionPane.showMessageDialog(this, "Mail sent!");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error: could not send mail", "Error", JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	/**
	 * Show inbox method. Called when inbox button is pressed and updates inbox GUI.
	 * Creates a button for each mail in inbox to open it.
	 * 
	 * @throws Exception problems fetching emails.
	 */
	private void showInbox() throws Exception {
		center.removeAll();
		for (int i = inbox.length - 1; i > 0; i--) {
			Message message = inbox[i];
			String btnText = "<html>" + "From: " + message.getFrom()[0] + "<br>" + "Subject: " + message.getSubject()
					+ "</html>";
			JButton btn = new JButton(btnText);
			btn.setBackground(Color.WHITE);
			btn.addActionListener(new MailListener(message));
			center.add(btn);
		}
		getContentPane().revalidate();
	}

	/**
	 * Initiates OpenEmail class.
	 * 
	 * @param message the message to be opened.	
	 */
	private void openMail(Message message) {
		new OpenEmail(message, properties, session);
	}

	/**
	 * ActionListener for email buttons.
	 * 
	 * @author Ivar Lund
	 */
	private class MailListener implements ActionListener {
		Message message;

		/**
		 * Constructor for MailListener.
		 * 
		 * @param message the message to be opened.
		 */
		public MailListener(Message message) {
			this.message = message;
		}

		/**
		 * Method call for ActionListener
		 */
		public void actionPerformed(ActionEvent e) {
				openMail(message);
		}
	}
	
	/**
	 * ActionListener for compose button.
	 * 
	 * @author Ivar Lund
	 */
	private class ComposeListener implements ActionListener {
		/**
		 * Method call for ActionListener.
		 */
		public void actionPerformed(ActionEvent e) {
			composeMessage();
		}
	}

	/**
	 * ActionListener for inbox button.
	 * 
	 * @author Ivar Lund
	 */
	private class InboxListener implements ActionListener {
		/**
		 * Method call for ActionListener.
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				inbox = fetchEmails.fetch();
				showInbox();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	// Main method
	public static void main(String[] args) {
		new EmailClient();
	}

}
