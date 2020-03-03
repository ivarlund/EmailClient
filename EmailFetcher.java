import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Class to fetch emails in the inbox of logged in account.
 * 
 * @author Ivar Lund, ivarnilslund@gmail.com
 *
 */
public class EmailFetcher {

	private Session session;
	private Message[] messages;
	private Folder emailFolder;
	private Store store;

	/**
	 * Class constructor. Takes a session.
	 * 
	 * @param session Session object to connect with.
	 */
	public EmailFetcher(Session session) {
		this.session = session;
	}

	/**
	 * Opens the inbox folder of logged in account and extract messages.
	 * 
	 * @return Array of messages from the Inbox.
	 * @throws Exception problems with getting emails.
	 */
	public Message[] fetch() throws Exception {

		store = session.getStore("pop3s");
		store.connect();

		emailFolder = store.getFolder("INBOX");
		emailFolder.open(Folder.READ_ONLY);

		this.messages = emailFolder.getMessages();
		return messages;
	}
	
}
