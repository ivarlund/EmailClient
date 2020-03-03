import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Class that opens the emails fetched from the logged in accounts inbox.
 * 
 * @author Ivar Lund ivlu1468 ivarnilslund@gmail.com
 *
 */
@SuppressWarnings("serial")
public class OpenEmail extends JFrame {

	private JLabel from = new JLabel("FROM: ");
	private JLabel subject = new JLabel("SUBJECT: ");
	private JLabel eAddress;
	private JLabel eSubject;
	private JTextArea display;
	private JPanel bottom;
	private String replyContent;
	private String replyTo;
	private Properties properties;
	private Session session;
	/**
	 * Class constructor. Takes a message object representing the email to be
	 * opened.
	 * 
	 * @param message the email to be opened.
	 */
	public OpenEmail(Message message, Properties properties, Session session) {
		this.properties = properties;
		this.session = session;
		try {
			eAddress = new JLabel("" + message.getFrom()[0]);
			eSubject = new JLabel(message.getSubject());
			setReplyTo("" + message.getFrom()[0]);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel top = new JPanel();
		JPanel left = new JPanel();
		JPanel right = new JPanel();
		bottom = new JPanel();

		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		bottom.setLayout(new BorderLayout());

		display = new JTextArea(15, 30);
		display.setEditable(false);
		JScrollPane pane = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JButton reply = new JButton("Reply");
		reply.addActionListener(new replyListener());
		
		Component horizontalStrut = Box.createHorizontalStrut(20);

		left.add(this.from);
		left.add(this.subject);
		right.add(eAddress);
		right.add(eSubject);
		top.add(left);
		top.add(right);
		top.add(horizontalStrut);
		top.add(reply);
		
		add(top);
		add(pane);
		add(bottom);

		setSize(520, 260);
		setLocation(100, 100);
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		getMailContent(message);

	}

	/**
	 * Gets the content of the email. If its a string it updates display, if not it
	 * calls getMailText.
	 * 
	 * @param message message object of the email.
	 */
	private void getMailContent(Message message) {
		try {
			Object content = message.getContent();
			if (content instanceof String) {
				String body = (String) content;
				updateDisplay(body);
				setReplyContent(body);
			} else if (content instanceof MimeMultipart) {
				MimeMultipart multi = (MimeMultipart) content;
				getMailText(multi);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Loops through the different parts of the email and checks if it contains
	 * attachments by checking for files and disposition. Updates display with the
	 * text bodyPart of the email. Creates a button for downloading attachment if
	 * the email contains an attachment.
	 * 
	 * @param mime Provided from getMailContent
	 * @throws Exception problems with processing the email.
	 */
	private void getMailText(MimeMultipart mime) throws Exception {
		Multipart multi = (Multipart) mime;
		for (int i = 0; i < multi.getCount(); i++) {
			Part part = multi.getBodyPart(i);

			String dis = part.getDisposition();

			if (part.isMimeType("text/plain")) {
				if (dis == null) {
					updateDisplay("" + part.getContent());
					setReplyContent("" + part.getContent());
				}
			}
			if ((dis != null) && (dis.equalsIgnoreCase(Part.ATTACHMENT) || dis.equalsIgnoreCase(Part.INLINE))) {
				JButton btn = new JButton("Download attachment");
				btn.addActionListener(new downloadListener((MimeBodyPart) part));
				bottom.add(btn, BorderLayout.CENTER);
			}
			if (part.getFileName() != null) {
				JButton btn = new JButton("Download attachment");
				btn.addActionListener(new downloadListener((MimeBodyPart) part));
				bottom.add(btn, BorderLayout.CENTER);
			} else if (part instanceof MimeMultipart) {
				getMailText((MimeMultipart) part);
			}
		}
	}

	/**
	 * Saves the attachment from the bodyPart.
	 * 
	 * @param body	the attachment part of the email body.
	 * @throws Exception	problems with downloading attachment.
	 */
	private void download(MimeBodyPart body) throws Exception {
		body.saveFile(new File(body.getFileName()));
	}

	/**
	 * Updates display.
	 * 
	 * @param str	the text to update with.
	 */
	private void updateDisplay(String str) {
		display.append(str);
	}
	/**
	 * Sets reply content.
	 * 
	 * @param content	String to set.
	 */
	private void setReplyContent(String replyContent) {
		this.replyContent = "--- reply ---\n" + replyContent + "\n--- reply ---\n";
	}
	
	private void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
	
	/**
	 * ActionListener for download attachment button.
	 * 
	 * @author Ivar Lund
	 *
	 */
	private class downloadListener implements ActionListener {
		private MimeBodyPart body;

		/**
		 * Class constructor.
		 * 
		 * @param body	the attachment part of the email body.
		 */
		public downloadListener(MimeBodyPart body) {
			this.body = body;
		}

		/**
		 * Method call for action listener.
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				download(body);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}
	
	private class replyListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			EmailTransmitter et = new EmailTransmitter(properties, session, replyTo, replyContent);
			Object[] option = { "Send", "Cancel" };
			int answer = JOptionPane.showOptionDialog(getContentPane(), et, "Compose new mail",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, option, option[0]);
			
			if (answer != JOptionPane.OK_OPTION) {
				return;
			} else {
				try {
					et.sendMsg();
					JOptionPane.showMessageDialog(getContentPane(), "Mail sent!");
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(getContentPane(), "Error: could not send mail", "Error", JOptionPane.ERROR_MESSAGE);
				}

			}
		}
		
	}

}
