package org.outerj.pollo.dialog;

import org.outerj.pollo.Pollo;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WelcomeDialog extends JFrame
{

	public WelcomeDialog()
	{
		super("Welcome to Pollo");

		addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent event)
				{ Pollo.getInstance().exit(); } });

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(12, 12, 12, 12));

		setContentPane(panel);

		panel.setLayout(new BorderLayout(12, 12));

		// the logo
		JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/org/outerj/pollo/dialog/logo.png")));
		panel.add(logo, BorderLayout.CENTER);

		// the buttons on bottom of the window
		Box buttons = new Box(BoxLayout.X_AXIS);

		JButton openExistingFileButton = new JButton(Pollo.getInstance().getFileOpenAction());
		buttons.add(openExistingFileButton);

		buttons.add(Box.createHorizontalStrut(6));

		JButton createNewFileButton = new JButton(Pollo.getInstance().getFileNewAction());
		buttons.add(createNewFileButton);

		buttons.add(Box.createGlue());

		JButton exitButton = new JButton(Pollo.getInstance().getExitAction());
		buttons.add(exitButton);

		panel.add(buttons, BorderLayout.SOUTH);

		this.pack();

		// center on screen
		Dimension dimension = getSize();
		Dimension dimension2 = getToolkit().getScreenSize();
		setLocation((dimension2.width - dimension.width) / 2, (dimension2.height - dimension.height) / 2);
	}

}
