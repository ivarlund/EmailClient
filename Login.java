import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Login class of email client program. Provides user credentials username &
 * password.
 * 
 * @author Ivar Lund, ivarnilslund@gmail.com
 *
 */
@SuppressWarnings("serial")
public class Login extends JPanel {

	private JTextField username = new JTextField(20);
	private JPasswordField password = new JPasswordField(20);

	/**
	 * Class constructor. Sets up a JPanel for user interaction.
	 */
	public Login() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JPanel left = new JPanel();
		JPanel right = new JPanel();

		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

		left.add(new JLabel("Username: "));
		left.add(new JLabel("Password: "));
		right.add(username);
		right.add(password);

		add(left);
		add(right);
	}

	/**
	 * Getter for JTextfield 'username'
	 */
	public String getUsername() {
		return username.getText();
	}

	/**
	 * Getter for JTextfield 'password'
	 */
	public String getPassword() {
		String pw = new String(password.getPassword());
		return pw;
	}

}
