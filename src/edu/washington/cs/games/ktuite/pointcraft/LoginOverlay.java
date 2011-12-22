package edu.washington.cs.games.ktuite.pointcraft;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class LoginOverlay extends Widget {

	final DialogLayout loginPanel;
	final EditField efName;
	final EditField efPassword;
	final Button btnLogin;
	Label errorLabel;

	public LoginOverlay() {
		loginPanel = new DialogLayout();
		loginPanel.setTheme("login-panel");

		efName = new EditField();

		efPassword = new EditField();
		efPassword.setPasswordMasking(true);

		Label lName = new Label("Name");
		lName.setLabelFor(efName);

		Label lPassword = new Label("Password");
		lPassword.setLabelFor(efPassword);

		Button signUp = new Button("Get an account online");
		signUp.setTheme("boldLabel");

		Button newerVersion = new Button("");
		if (!Main.server.is_up_to_date){
			newerVersion.setText("A newer version of PointCraft is available online!! Go get it!!");
		}
		newerVersion.setTheme("attention");

		errorLabel = new Label("");
		errorLabel.setTheme("errorLabel");

		signUp.addCallback(new Runnable() {
			public void run() {
				openSignupPage();
			}
		});

		newerVersion.addCallback(new Runnable() {
			public void run() {
				openPointcraftPage();
			}
		});

		btnLogin = new Button("LOGIN");
		btnLogin.addCallback(new Runnable() {
			public void run() {
				login();
			}
		});

		DialogLayout.Group hLabels = loginPanel.createParallelGroup(lName,
				lPassword);
		DialogLayout.Group hFields = loginPanel.createParallelGroup(efName,
				efPassword);
		DialogLayout.Group hBtn = loginPanel.createSequentialGroup().addGap()
				.addWidget(btnLogin);

		DialogLayout.Group hNewerVersion = loginPanel.createSequentialGroup()
				.addGap().addWidget(newerVersion).addGap();
		DialogLayout.Group vNewerVersion = loginPanel.createSequentialGroup()
				.addGap().addWidget(newerVersion);//

		if (!Main.server.is_up_to_date)
			vNewerVersion = vNewerVersion.addGap(DialogLayout.LARGE_GAP);

		DialogLayout.Group hSignUp = loginPanel.createSequentialGroup()
				.addGap().addWidget(signUp).addGap();
		DialogLayout.Group vSignUp = loginPanel.createSequentialGroup()
				.addGap().addWidget(signUp);

		DialogLayout.Group hError = loginPanel.createSequentialGroup().addGap()
				.addWidget(errorLabel).addGap();
		DialogLayout.Group vError = loginPanel.createSequentialGroup().addGap()
				.addWidget(errorLabel).addGap(DialogLayout.LARGE_GAP);

		loginPanel.setHorizontalGroup(loginPanel.createParallelGroup()
				.addGroup(hNewerVersion).addGroup(hSignUp).addGroup(hError)
				.addGroup(loginPanel.createSequentialGroup(hLabels, hFields))
				.addGroup(hBtn));
		loginPanel
				.setVerticalGroup(loginPanel
						.createSequentialGroup()
						.addGroup(vNewerVersion)
						.addGroup(vSignUp)
						.addGroup(vError)
						.addGroup(loginPanel.createParallelGroup(lName, efName))
						.addGroup(
								loginPanel.createParallelGroup(lPassword,
										efPassword)).addWidget(btnLogin));

		add(loginPanel);
	}

	private void openSignupPage() {
		BrowserControl
				.openUrl("http://www.photocitygame.com/pointcraft/register.php");
	}

	private void openPointcraftPage() {
		BrowserControl.openUrl("http://www.photocitygame.com/pointcraft/");
	}

	public static String toHex(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	private void login() {
		GUI gui = getGUI();
		if (gui != null) {
			// step 1: disable all controls
			efName.setEnabled(false);
			efPassword.setEnabled(false);
			btnLogin.setEnabled(false);

			String username = efName.getText();
			try {
				MessageDigest sha1 = MessageDigest.getInstance("SHA1");
				String hashed = toHex(
						sha1.digest(efPassword.getText().getBytes()))
						.toLowerCase();
				Main.is_logged_in = Main.server.attemptLogin(username, hashed);
				if (!Main.is_logged_in) {
					errorLabel.setText("wrong username/password");
				}
				efName.setEnabled(true);
				efPassword.setEnabled(true);
				efPassword.setText("");
				btnLogin.setEnabled(true);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * // step 2: start a timer to simulate the process of talking to a
			 * // remote server Timer timer = gui.createTimer();
			 * timer.setCallback(new Runnable() { public void run() { // once
			 * the timer fired re-enable the controls and clear the // password
			 * efName.setEnabled(true); efPassword.setEnabled(true);
			 * efPassword.setText(""); btnLogin.setEnabled(true); } });
			 * timer.setDelay(2500); timer.start();
			 */
			/*
			 * NOTE: in a real app you would need to keep a reference to the
			 * timer object to cancel it if the user closes the dialog which
			 * uses the timer.
			 * 
			 * @see Widget#beforeRemoveFromGUI(de.matthiasmann.twl.GUI)
			 */
		}
	}

	@Override
	protected void layout() {

		// login panel is centered
		loginPanel.adjustSize();
		loginPanel.setPosition(
				getInnerX() + (getInnerWidth() - loginPanel.getWidth()) / 2,
				getInnerY() + (getInnerHeight() - loginPanel.getHeight()) / 2);
	}
}
