package org.outerj.pollo.dialog;

import javax.swing.JDialog;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Frame;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
		licenseTabs.add("About", createTextArea("/org/outerj/pollo/general.txt"));
		licenseTabs.add("Pollo license", createTextArea("/org/outerj/pollo/pollo_license.txt"));
		licenseTabs.add("Changelog", createTextArea("/org/outerj/pollo/ChangeLog"));
		licenseTabs.add("Apache", createTextArea("/org/outerj/pollo/apache_license.txt"));
		licenseTabs.add("Jaxen", createTextArea("/org/outerj/pollo/jaxen_license.txt"));
		licenseTabs.add("Kunstoff", createTextArea("/org/outerj/pollo/kunststoff_license.txt"));
		licenseTabs.add("Others", createTextArea("/org/outerj/pollo/others_license.txt"));

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
