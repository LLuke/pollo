package org.outerj.pollo.xmleditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A Swing Dialog for containing a JTextArea for editing the content
 * of DOM character nodes (eg comments, text nodes, cdata sections).
 *
 * Use the method showWithData(String data).
 *
 * @author Bruno Dumon
 */
public class CharacterDataEditDialog extends JDialog implements ActionListener
{
	protected JTextArea characterDataTextArea;
	protected JScrollPane scrollPane;
	protected boolean ok;

	public CharacterDataEditDialog(Frame parent)
	{
		super(parent, "Edit text");
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // users should select ok or cancel

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(12, 12, 12, 12));
		panel.setLayout(new BorderLayout(12, 12));
		this.setContentPane(panel);

		characterDataTextArea = new JTextArea();
		characterDataTextArea.setFont(new Font("Monospaced", 0, 12));

		scrollPane = new JScrollPane(characterDataTextArea);
		scrollPane.setPreferredSize(new Dimension(600, 350));
		panel.add(scrollPane, BorderLayout.CENTER);

		JButton okButton = new JButton("Okay");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);

		Box buttons = new Box(BoxLayout.X_AXIS);
		buttons.add(Box.createGlue());
		buttons.add(okButton);
		buttons.add(Box.createHorizontalStrut(6));
		buttons.add(cancelButton);
		panel.add(buttons, BorderLayout.SOUTH);

		pack();
	}

	/**
	 * Shows the dialog with the given data.
	 *
	 * @return the (changed) data if the user selects ok, or null if the user
	 *         selected cancel.
	 */
	public String showWithData(String data)
	{
		characterDataTextArea.setText(data);
		characterDataTextArea.setCaretPosition(0);
		show();

		if (ok)
			return characterDataTextArea.getText();
		else
			return null;
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("ok"))
		{
			ok = true;
			hide();
		}
		else if (event.getActionCommand().equals("cancel"))
		{
			ok = false;
			hide();
		}
	}

}
