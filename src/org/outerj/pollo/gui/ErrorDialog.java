package org.outerj.pollo.gui;

import org.outerj.pollo.xmleditor.exception.PolloException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ErrorDialog extends JDialog implements ActionListener
{
	protected JList messageList;
	protected JTextArea stackTraceView;

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

		Box box = new Box(BoxLayout.Y_AXIS);

		// make list of nested exceptions
		ArrayList tempExceptionList = new ArrayList();
		addExceptionsToList(exception, tempExceptionList);
		messageList = new JList(tempExceptionList.toArray());
		JScrollPane messageListScrollPane = new JScrollPane(messageList);
		// put nice border around it
		Border border = BorderFactory.createEtchedBorder(BevelBorder.LOWERED);
		Border border2 = BorderFactory.createTitledBorder(border, "Nested messages :");
		Border border3 = BorderFactory.createCompoundBorder(border2, messageListScrollPane.getBorder());
		messageListScrollPane.setBorder(border3);
		box.add(messageListScrollPane);

		// make view of last stack trace
		stackTraceView = new JTextArea(stackTrace);
		stackTraceView.setCaretPosition(0);
		stackTraceView.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(stackTraceView);
		scrollPane.setPreferredSize(new Dimension(400, 200));

		Border border4 = BorderFactory.createEtchedBorder(BevelBorder.LOWERED);
		Border border5 = BorderFactory.createTitledBorder(border4, "Stacktrace :");
		Border border6 = BorderFactory.createCompoundBorder(border5, scrollPane.getBorder());
		scrollPane.setBorder(border6);

		box.add(scrollPane);
		panel.add(box, BorderLayout.CENTER);

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

	public void addExceptionsToList(Exception e, List list)
	{
		list.add(e.getMessage());
		if (e instanceof PolloException)
		{
			PolloException pe = (PolloException)e;
			if (pe.getNestedException() != null)
			{
				addExceptionsToList(pe.getNestedException(), list);
			}
		}
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("close"))
		{
			this.hide();
		}
	}

}
