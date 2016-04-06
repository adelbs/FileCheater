package com.obi1.fileCheater;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public class FileCheater extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static FileCheater instance = null;
	private static Runner runner;
	
	private SendMail sendMail;
	private JProgressBar progressBar;
	private JComboBox<String> cmbAction;
	private JTextField txtFile;
	private JFormattedTextField txtSize;
	private JButton btnGo;
	private JButton btnOpen;
	
	public FileCheater() throws Throwable {
		runner = new Runner();
		runner.start();

		sendMail = new SendMail(this);
		
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("File Cheater 1.0");

		JLabel lblAction = new JLabel("Action:");
		lblAction.setBounds(12, 13, 118, 22);
		JLabel lblFile = new JLabel("File:");
		lblFile.setBounds(12, 40, 118, 22);
		JLabel lblSize = new JLabel("Max Mb (optional):");
		lblSize.setBounds(12, 67, 118, 22);
		
		String[] actions = new String[]{"split", "merge"};
		cmbAction = new JComboBox<String>(actions);
		cmbAction.setBounds(142, 13, 157, 22);
		txtFile = new JTextField();
		txtFile.setLocation(142, 40);
		txtSize = new JFormattedTextField(new NumberFormatter());
		txtSize.setBounds(142, 67, 157, 22);
		
		txtFile.setSize(293, 22);
		getContentPane().setLayout(null);
		
		getContentPane().add(lblAction);
		getContentPane().add(cmbAction);
		getContentPane().add(lblFile);
		getContentPane().add(txtFile);
		getContentPane().add(lblSize);
		getContentPane().add(txtSize);
		
		btnGo = new JButton("Go!");
		btnGo.setBounds(374, 66, 97, 25);
		getContentPane().add(btnGo);
		
		btnGo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runner.enable(false);
				if (cmbAction.getSelectedItem().equals("split"))
					runner.splitAS(txtFile.getText(), txtSize.getText().equals("") || Integer.parseInt(txtSize.getText()) <= 0 ? -1 
							: Integer.parseInt(txtSize.getText()));
				else
					runner.mergeAS(txtFile.getText());
			}
		});
		
		btnOpen = new JButton("...");
		btnOpen.setBounds(434, 39, 37, 23);
		getContentPane().add(btnOpen);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(0, 102, 483, 14);
		getContentPane().add(progressBar);
		
		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(FileCheater.this) == JFileChooser.APPROVE_OPTION)
					txtFile.setText(fc.getSelectedFile().getPath());
			}
		});
		
		setSize(489, 148);
		setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2-this.getSize().width/2, 
				Toolkit.getDefaultToolkit().getScreenSize().height/2-this.getSize().height/2);
		setVisible(true);
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			try {
				instance = new FileCheater();
			}
			catch (Throwable x) {
				printUsage();
			}
		}
		else {
			String action = args[0];
			String fileName = args[1];
			String numBytes = (args.length == 3) ? args[2] : "-1";
			
			if (action.equals("split")) runner.split(fileName, Integer.parseInt(numBytes));
			else if (action.equals("merge")) runner.merge(fileName);
			else printUsage();
		}
	}
	
	private static void printUsage() {
		String usage = "Expected parameters: [FILENAME] [ACTION: (split|merge)] [MAX_MBYTES (optional)]\n\n"+
					"Example spliting a file considering 2mb as the maximum size:\n"+
					"java -jar FileCheater.jar c:/myfolder/myfile.exe split 2\n\n"+
					"Example merging the splited file above:\n"+
					"java -jar FileCheater.jar c:/myfolder/myfile.exe merge";
		System.out.println(usage);
	}
	
	private class Runner extends Thread {
		
		private String fileName;
		private int numBytes;
		private String currentAction = "";
		
		public void run() {
			while (true) {
				try {
					if (currentAction.equals("split"))
						split(fileName, numBytes);
					else if (currentAction.equals("merge"))
						merge(fileName);
					
					enable(true);
					Thread.sleep(100);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	    }

		public void splitAS(String fileName, int numBytes) {
			if (currentAction.equals("")) {
				this.fileName = fileName;
				this.numBytes = numBytes;
				currentAction = "split";
			}
		}
		
		public void mergeAS(String fileName) {
			if (currentAction.equals("")) {
				this.fileName = fileName;
				currentAction = "merge";
			}
		}
		
		public void split(String fileName, int numBytes) {
			FileOutputStream fos;
			
			try {
				byte[] data = invertBytes(Files.readAllBytes(Paths.get(fileName)));
				byte[] fileData;
				byte[] lastFile;
				int maxBytes = numBytes == -1 ? data.length : ((numBytes * 1024) * 1024);
				int newFileSize = 0;
				int numFile = 0;
				
				ArrayList<File> files = new ArrayList<File>();
				File newFile;
				
				while (newFileSize < data.length) {
					
					fileData = new byte[maxBytes];
					int i = 0;
					for (; i < maxBytes && newFileSize < data.length; i++) {
						fileData[i] = data[newFileSize];
						newFileSize++;
					}
					
					if (i != maxBytes) {
						lastFile = new byte[i];
						for (i = 0; i < lastFile.length; i++)
							lastFile[i] = fileData[i];
						fileData = lastFile;
					}
					
					numFile++;
					newFile = new File(fileName + ".ADELBS."+ numFile);
					fos = new FileOutputStream(newFile);
					fos.write(fileData);
					fos.close();
					
					files.add(newFile);
					
					updateStatus(new Long((newFileSize * 100L) / data.length).intValue());
				}
				
				showMessage("DONE! File splited in "+ numFile +" file(s).");
				if (instance != null) {
					if (numFile <= 20 && numBytes <= 10 && JOptionPane.showConfirmDialog(instance, 
							"Would you like to send the files via e-mail?", "Send e-mail", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						sendMail.openDialog(files);
					}
				}
			} 
			catch (Exception x) {
				x.printStackTrace();
				showMessage(x.toString() +"\n\n"+ x.getMessage());
			}
			finally {
				currentAction = "";
			}
		}
		
		public void merge(String fileName) {
			
			FileOutputStream fos;
			File file = new File(fileName);
			File fileRead;
			
			byte[] data;
			byte[] aux;
			byte[] finalFile = new byte[0];
			int fileNum = 1;
			int totalBytes = 0;
			
			if (!new File(file.getParent()).isDirectory() || !new File(fileName +".ADELBS.1").exists()) {
				showMessage("File not found!");
				currentAction = "";
			}
			else {
				try {
					fileRead = new File(fileName +".ADELBS."+ fileNum);
					while (fileRead.exists()) {
		
						data = Files.readAllBytes(Paths.get(fileRead.getPath()));
						totalBytes += data.length;
						
						aux = finalFile;
						finalFile = new byte[totalBytes];
						int i = 0;
						for (; i < aux.length; i++)
							finalFile[i] = aux[i];
						for (int j = 0; j < data.length; j++) {
							finalFile[i] = data[j];
							i++;
						}
						
						fileNum++;
						fileRead = new File(fileName +".ADELBS."+ fileNum);
					}
					
					finalFile = invertBytes(finalFile);
					fos = new FileOutputStream(file);
					fos.write(finalFile);
					fos.close();
					
					showMessage("DONE! "+ (fileNum - 1) +" file(s) merged.");
				}
				catch (Exception x) {
					x.printStackTrace();
					showMessage(x.toString() +"\n\n"+ x.getMessage());
				}
				finally {
					currentAction = "";
				}
			}
		}
		
		private byte[] invertBytes(byte[] data) {
			byte[] newData;
			newData = new byte[data.length];
			for (int i = 0; i < data.length; i++)
				newData[data.length - i -1] = data[i];
			return newData;
		}
		
		private void showMessage(String msg) {
			if (instance == null)
				System.out.println(msg);
			else
				JOptionPane.showMessageDialog(instance, msg);
		}

		private void updateStatus(int value) {
			if (progressBar != null)
				progressBar.setValue(value);
		}
		
		private void enable(boolean value) {
			if (progressBar != null) {
				cmbAction.setEnabled(value);
				txtFile.setEnabled(value);
				txtSize.setEnabled(value);
				btnGo.setEnabled(value);
				btnOpen.setEnabled(value);
			}
		}
	}
}
