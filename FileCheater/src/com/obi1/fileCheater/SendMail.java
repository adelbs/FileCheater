package com.obi1.fileCheater;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class SendMail extends JDialog {
	
	private static final long serialVersionUID = 1L;

	private static Sender sender;
	
	private JProgressBar progressBar;
	private JTextField txtTo;
	private JTextField txtFrom;
	private JTextField txtUser;
	private JPasswordField txtPwd;
	private JTextField txtHost;
	private JTextField txtPort;
	private JTextPane txtMessage;
	private JButton btnSend;
	
	private ArrayList<File> files;
	
	public SendMail(FileCheater parent) {
		super(parent, "Send e-mail", true);
		
		sender = new Sender();
		sender.start();
		
		setResizable(false);
		getContentPane().setLayout(null);
		
		JLabel lblTo = new JLabel("To:");
		lblTo.setBounds(12, 13, 68, 16);
		getContentPane().add(lblTo);
		
		JLabel lblFrom = new JLabel("From:");
		lblFrom.setBounds(12, 42, 68, 16);
		getContentPane().add(lblFrom);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(12, 71, 68, 16);
		getContentPane().add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(12, 100, 68, 16);
		getContentPane().add(lblPassword);
		
		JLabel lblHost = new JLabel("Host:");
		lblHost.setBounds(12, 129, 68, 16);
		getContentPane().add(lblHost);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(12, 158, 68, 16);
		getContentPane().add(lblPort);
		
		txtTo = new JTextField();
		txtTo.setBounds(92, 13, 247, 22);
		getContentPane().add(txtTo);
		txtTo.setColumns(10);
		
		txtFrom = new JTextField();
		txtFrom.setBounds(92, 39, 247, 22);
		getContentPane().add(txtFrom);
		txtFrom.setColumns(10);
		
		txtUser = new JTextField();
		txtUser.setBounds(92, 68, 247, 22);
		getContentPane().add(txtUser);
		txtUser.setColumns(10);
		
		txtPwd = new JPasswordField();
		txtPwd.setBounds(92, 97, 247, 22);
		getContentPane().add(txtPwd);
		txtPwd.setColumns(10);
		
		txtHost = new JTextField();
		txtHost.setText("outlook.office365.com");
		txtHost.setBounds(92, 126, 247, 22);
		getContentPane().add(txtHost);
		txtHost.setColumns(10);
		
		txtPort = new JTextField();
		txtPort.setText("587");
		txtPort.setBounds(92, 155, 116, 22);
		getContentPane().add(txtPort);
		txtPort.setColumns(10);
		
		btnSend = new JButton("Send");
		btnSend.setBounds(242, 396, 97, 25);
		getContentPane().add(btnSend);
		
		JLabel lblMessage = new JLabel("Message:");
		lblMessage.setBounds(12, 187, 68, 16);
		getContentPane().add(lblMessage);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(92, 190, 250, 199);
		getContentPane().add(scrollPane);
		
		txtMessage = new JTextPane();
		txtMessage.setText("You are gonna to receive several e-mails with a splited file. To merge these files, "+
							"follow the instructions below:\r\n\r\n1) Rename the FileCheater.pdf to FileCheater.jar\r\n"+
							"2) Run the FileCheater.jar\r\n3) Select the \"merge\" option\r\n4) Inform the name of the "+
							"splited file (without the ADELBS.n extension)\r\n");
		scrollPane.setViewportView(txtMessage);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(0, 431, 355, 14);
		getContentPane().add(progressBar);

		setSize(361, 477);
		
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isSomeEmpty()) {
					sender.enable(false);
					sender.sendMail();
				}
			}
		});
	}
	
	public void openDialog(ArrayList<File> files) {
		this.files = files;
		
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2-this.getSize().width/2, 
				Toolkit.getDefaultToolkit().getScreenSize().height/2-this.getSize().height/2);

		
		setVisible(true);
	}
	
	private boolean isSomeEmpty() {
		return txtTo.getText().trim().equals("") || txtFrom.getText().trim().equals("") || txtUser.getText().trim().equals("") ||
				new String(txtPwd.getPassword()).trim().equals("") || txtHost.getText().trim().equals("") || 
				txtPort.getText().trim().equals("") || txtMessage.getText().trim().equals("");
	}
	
	private class Sender extends Thread {

		private boolean send = false;
		
		public void run() {
		
			while (true) {
				try {
					if (send) {
						String jarPath = (SendMail.class.getProtectionDomain().getCodeSource().getLocation().getPath());
						
						sendMail(txtTo.getText(), txtFrom.getText(), txtUser.getText(), new String(txtPwd.getPassword()), 
								txtHost.getText(), txtPort.getText(), "Splited File: instruction", txtMessage.getText(), jarPath);
						
						for (int i = 0; i < files.size(); i++) {
							sendMail(txtTo.getText(), txtFrom.getText(), txtUser.getText(), new String(txtPwd.getPassword()), 
									txtHost.getText(), txtPort.getText(), "Splited File: "+ (i + 1) +" of "+ files.size(), 
									"", files.get(i).getPath());
							
							updateStatus(new Long((i * 100L) / files.size()).intValue());
						}
						
						updateStatus(100);
						JOptionPane.showMessageDialog(SendMail.this, "E-mail sent!");
					 	
						enable(true);
						send = false;
						SendMail.this.setVisible(false);
					}
					
					Thread.sleep(100);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendMail() {
			send = true;
		}
		
		/**
		 * from = felipe.jacob@ca.com
		 * username = jacfe02@ca.com
		 * host = outlook.office365.com
		 * port = 587
		 */
		private void sendMail(String to, String from, final String username, final String password, String host, String port, 
				String title, String msg, String filename) {

			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);

			// Get the Session object.
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
					return new javax.mail.PasswordAuthentication(username, password);
				}
			});

			try {
				// Create a default MimeMessage object.
				Message message = new MimeMessage(session);

				// Set From: header field of the header.
				message.setFrom(new InternetAddress(from));

				// Set To: header field of the header.
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

				// Set Subject: header field
				message.setSubject(title);

				// Create the message part
				BodyPart messageBodyPart = new MimeBodyPart();

				// Now set the actual message
				messageBodyPart.setText(msg);

				// Create a multipar message
				Multipart multipart = new MimeMultipart();

				// Set text message part
				multipart.addBodyPart(messageBodyPart);

				// Part two is attachment
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filename);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(filename);
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);

				// Send message
				Transport.send(message);

				System.out.println("Sent message ("+ title +") successfully....");
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void updateStatus(int value) {
			if (progressBar != null)
				progressBar.setValue(value);
		}

		private void enable(boolean value) {
			if (progressBar != null) {
				txtTo.setEnabled(value);
				txtFrom.setEnabled(value);
				txtUser.setEnabled(value);
				txtPwd.setEnabled(value);
				txtHost.setEnabled(value);
				txtPort.setEnabled(value);
				txtMessage.setEnabled(value);
				btnSend.setEnabled(value);
			}
		}
	}
}
