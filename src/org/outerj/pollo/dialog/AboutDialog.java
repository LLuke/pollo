package org.outerj.pollo.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * About dialog for Pollo.
 *
 * @author Bruno Dumon
 */
public class AboutDialog extends JDialog
{
	public AboutDialog(Frame parent)
	{
		super(parent, "About Pollo");

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);

		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(12, 12, 12, 12);
		JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/org/outerj/pollo/dialog/logo.png")));
		layout.setConstraints(logo, constraints);
		getContentPane().add(logo);

		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;


		JTabbedPane licenseTabs = new JTabbedPane();
		licenseTabs.add("About", createTextArea("/org/outerj/pollo/resource/general.txt"));
		licenseTabs.add("Pollo license", createTextArea("/org/outerj/pollo/resource/pollo_license.txt"));
		licenseTabs.add("Changelog", createTextArea("/org/outerj/pollo/resource/ChangeLog"));
		licenseTabs.add("Acknowledgements", createTextArea("/org/outerj/pollo/resource/acknowledgments.txt"));

		layout.setConstraints(licenseTabs, constraints);
		getContentPane().add(licenseTabs);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { hide(); } });
		constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(12, 12, 12, 12);
		layout.setConstraints(closeButton, constraints);
		getContentPane().add(closeButton);

		pack();
	}

	protected JScrollPane createTextArea(String resourceName)
	{
		JTextArea textArea = new JTextArea();
		try
		{
			 BufferedReader in = new BufferedReader(new InputStreamReader(getClass()
						 .getResource(resourceName).openStream()));
			 StringBuffer text = new StringBuffer();
			 String line;
			 do
			 {
				 line = in.readLine();
				 if (line != null)
					 text.append("\n").append(line);
			 }
			 while (line != null);

			 textArea.setText(text.toString());
		}
		catch (Exception e)
		{
			textArea.setText("Error: Could not load text.");
		}
		textArea.setFont(new Font("Monospaced", 0, 12));
		textArea.setEditable(false);
		textArea.setCaretPosition(0);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(600, 225));
		return scrollPane;
	}

}
