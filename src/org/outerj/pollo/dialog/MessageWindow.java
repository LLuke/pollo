package org.outerj.pollo.dialog;

import javax.swing.JWindow;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import java.awt.Dimension;

public class MessageWindow extends JWindow
{
	protected JLabel messageLabel;

	public MessageWindow(String message)
	{
		messageLabel = new JLabel(message);
		messageLabel.setBorder(new EmptyBorder(30, 30, 30, 30));

		this.getContentPane().add(messageLabel);
		pack();

		Dimension dimension = getSize();
		Dimension dimension2 = getToolkit().getScreenSize();
		setLocation((dimension2.width - dimension.width) / 2, (dimension2.height - dimension.height) / 2);
	}

	public void show()
	{
		super.show();
		messageLabel.paintImmediately(new java.awt.Rectangle(0, 0,
					messageLabel.getWidth(), messageLabel.getHeight()));
	}
}