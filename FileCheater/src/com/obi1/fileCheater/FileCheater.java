package com.obi1.fileCheater;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

public class FileCheater extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static FileCheater instance = null;
	
	public FileCheater() throws Throwable {
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
		final JComboBox<String> cmbAction = new JComboBox<String>(actions);
		cmbAction.setBounds(142, 13, 157, 22);
		final JTextField txtFile = new JTextField();
		txtFile.setLocation(142, 40);
		final JFormattedTextField txtSize = new JFormattedTextField(new NumberFormatter());
		txtSize.setBounds(142, 67, 157, 22);
		
		txtFile.setSize(293, 22);
		getContentPane().setLayout(null);
		
		getContentPane().add(lblAction);
		getContentPane().add(cmbAction);
		getContentPane().add(lblFile);
		getContentPane().add(txtFile);
		getContentPane().add(lblSize);
		getContentPane().add(txtSize);
		
		JButton btnGo = new JButton("Go!");
		btnGo.setBounds(374, 66, 97, 25);
		getContentPane().add(btnGo);
		
		btnGo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cmbAction.getSelectedItem().equals("split"))
					split(txtFile.getText(), txtSize.getText().equals("") || Integer.parseInt(txtSize.getText()) <= 0 ? -1 
							: Integer.parseInt(txtSize.getText()));
				else
					merge(txtFile.getText());
			}
		});
		
		JButton btnOpen = new JButton("...");
		btnOpen.setBounds(434, 39, 37, 23);
		getContentPane().add(btnOpen);
		
		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(FileCheater.this) == JFileChooser.APPROVE_OPTION)
					txtFile.setText(fc.getSelectedFile().getPath());
			}
		});
		
		setSize(489, 135);
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
			
			if (action.equals("split")) split(fileName, Integer.parseInt(numBytes));
			else if (action.equals("merge")) merge(fileName);
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
	
	private static void split(String fileName, int numBytes) {
		FileOutputStream fos;
		
		try {
			byte[] data = invertBytes(Files.readAllBytes(Paths.get(fileName)));
			byte[] fileData;
			byte[] lastFile;
			int maxBytes = numBytes == -1 ? data.length : ((numBytes * 1024) * 1024);
			int newFileSize = 0;
			int numFile = 0;
			
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
				fos = new FileOutputStream(new File(fileName + ".ADELBS."+ numFile));
				fos.write(fileData);
				fos.close();
			}
			
			showMessage("DONE! File splited in "+ numFile +" file(s).");
		} 
		catch (Exception x) {
			x.printStackTrace();
			showMessage(x.toString() +"\n\n"+ x.getMessage());
		}
	}
	
	private static void merge(String fileName) {
		
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
		}
	}
	
	private static byte[] invertBytes(byte[] data) {
		byte[] newData;
		newData = new byte[data.length];
		for (int i = 0; i < data.length; i++)
			newData[data.length - i -1] = data[i];
		return newData;
	}
	
	private static void showMessage(String msg) {
		if (instance == null)
			System.out.println(msg);
		else
			JOptionPane.showMessageDialog(instance, msg);
	}
}
