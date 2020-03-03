import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Class for sending email. Sets logged in account as predefined sender.
 * 
 * @author Ivar Lund ivlu1468 ivarnilslund@gmail.com
 *
 */
@SuppressWarnings("serial")
public class EmailTransmitter extends JPanel {

	private Properties properties;
	private Session session;
	private JTextArea display;
	private JTextField to = new JTextField(20);
	private JTextField subject = new JTextField(20);
	private JPanel bottom;
	private Boolean hasAttachment = false;
	private File file;

	/**
	 * Class constructor. Takes a Properties and Session object for sending emails.
	 * 
	 * @param properties   the properties object for sending email.
	 * @param session      the Session object for sending email.
	 * @param replyTo      email address provided if this class is called as a
	 *                     reply.
	 * @param replyContent email content provided if this class is called as a
	 *                     reply.
	 */
	public EmailTransmitter(Properties properties, Session session, String replyTo, String replyContent) {
		this.properties = properties;
		this.session = session;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel top = new JPanel();
		JPanel left = new JPanel();
		JPanel right = new JPanel();
		bottom = new JPanel();

		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		bottom.setLayout(new BorderLayout());

		display = new JTextArea(15, 30);
		display.setEditable(true);
		JScrollPane pane = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JButton addAttachment = new JButton("Add attachment");
		addAttachment.addActionListener(new AttachmentListener());

		to.setText(replyTo);
		display.append(replyContent);

		left.add(new JLabel("To: "));
		left.add(new JLabel("Subject: "));
		right.add(to);
		right.add(subject);
		top.add(left);
		top.add(right);
		bottom.add(addAttachment, BorderLayout.CENTER);

		add(top);
		add(pane);
		add(bottom);
	}

	public EmailTransmitter(Properties properties, Session session) {
		this.properties = properties;
		this.session = session;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel top = new JPanel();
		JPanel left = new JPanel();
		JPanel right = new JPanel();
		bottom = new JPanel();

		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		bottom.setLayout(new BorderLayout());

		display = new JTextArea(15, 30);
		display.setEditable(true);
		JScrollPane pane = new JScrollPane(display, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JButton addAttachment = new JButton("Add attachment");
		addAttachment.addActionListener(new AttachmentListener());

		left.add(new JLabel("To: "));
		left.add(new JLabel("Subject: "));
		right.add(to);
		right.add(subject);
		top.add(left);
		top.add(right);
		bottom.add(addAttachment, BorderLayout.CENTER);

		add(top);
		add(pane);
		add(bottom);
	}

	/**
	 * Sets the boolean to judge whether the email has an attachment.
	 * 
	 * @param hasAttachment the value to set.
	 */
	private void setHasAttachment(Boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}

	/**
	 * Getter for JTextfield 'to'
	 */
	public String getTo() {
		return to.getText();
	}

	/**
	 * Getter for JTextfield 'subject'
	 */
	public String getSubject() {
		return subject.getText();
	}

	/**
	 * Getter for JTextfield 'content'
	 */
	public String getContent() {
		return display.getText();
	}

	/**
	 * Creates a Message object and sets it up for transportation. If
	 * 'hasAttachment' is true, a attachment body part is included in the email.
	 */
	public void sendMsg() throws Exception {
		Message msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(properties.getProperty("username")));
		msg.setRecipient(RecipientType.TO, new InternetAddress(getTo()));
		msg.setSubject(getSubject());

		BodyPart msgBody = new MimeBodyPart();

		msgBody.setText(getContent());

		Multipart multi = new MimeMultipart();
		multi.addBodyPart(msgBody);

		if (hasAttachment) {
			msgBody = new MimeBodyPart();
			DataSource data = new FileDataSource(file);
			msgBody.setDataHandler(new DataHandler(data));
			msgBody.setFileName(file.getName());
			msgBody.setDisposition(Part.ATTACHMENT);
			multi.addBodyPart(msgBody);

		}
		msg.setContent(multi);
		Transport.send(msg);
	}

	/**
	 * Set JTextField 'to' if this class is called as a reply
	 * 
	 * @param replyTo the reply address.
	 */
	public void setTo(String replyTo) {
		to.setText(replyTo);
	}

	/**
	 * Set JTextArea 'display' if this class is called as a reply
	 * 
	 * @param replyContent the reply content
	 */
	public void setContent(String replyContent) {
		display.append(replyContent);
	}

	/**
	 * Opens a JFileChooser to choose what attachment to be included. Also sets the
	 * hasAttachment to true if a file is chosen.
	 */
	private void getAttachment() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(System.getProperty("user.home")));
		int answer = jfc.showOpenDialog(this);
		if (answer != JFileChooser.APPROVE_OPTION) {
			return;
		} else {
			file = jfc.getSelectedFile();
			setHasAttachment(true);
			JLabel attachment = new JLabel("Attachment: " + file.getName());
			bottom.add(attachment, BorderLayout.SOUTH);
			this.revalidate();
		}

	}

	/**
	 * ActionListener for Add attachment button.
	 * 
	 * @author Ivar Lund
	 *
	 */
	private class AttachmentListener implements ActionListener {

		/**
		 * Method call for ActionListener
		 */
		public void actionPerformed(ActionEvent e) {
			getAttachment();
		}
	}

}
