package org.outerj.pollo.dialog;

import java.io.StringWriter;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ErrorDialog extends JDialog implements ActionListener
{
	public ErrorDialog(Frame parent, String message, Exception exception)
	{
		super(parent, "Error");

		setModal(true);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(12,12,12,12));

		panel.setLayout(new BorderLayout(12, 12));
		StringWriter stackTraceWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTraceWriter));
		String stackTrace = stackTraceWriter.toString();


		JLabel messageLabel = new JLabel(message);
		panel.add(messageLabel, BorderLayout.NORTH);

		JTextArea textArea = new JTextArea(stackTrace);
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(400, 200));

		Border border = BorderFactory.createEtchedBorder(BevelBorder.LOWERED);
		Border border2 = BorderFactory.createTitledBorder(border, "Stacktrace :");
		Border border3 = BorderFactory.createCompoundBorder(border2, scrollPane.getBorder());
		scrollPane.setBorder(border3);

		panel.add(scrollPane, BorderLayout.CENTER);

		Box buttons = new Box(BoxLayout.X_AXIS);
		buttons.add(Box.createGlue());
		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		buttons.add(closeButton);

		panel.add(buttons, BorderLayout.SOUTH);

		this.setContentPane(panel);

		pack();

		// center on screen
		Dimension dimension = getSize();
		Dimension dimension2 = getToolkit().getScreenSize();
		setLocation((dimension2.width - dimension.width) / 2, (dimension2.height - dimension.height) / 2);
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("close"))
		{
			this.hide();
		}
	}
}
