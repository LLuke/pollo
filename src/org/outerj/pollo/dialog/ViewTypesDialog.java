package org.outerj.pollo.dialog;

import org.outerj.pollo.Pollo;

import java.util.Vector;
import java.util.StringTokenizer;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A dialog from which the user can select a viewtype.
 *
 * @author Bruno Dumon
 */
public class ViewTypesDialog extends JDialog implements ActionListener
{
	protected boolean ok = false;
	protected JList viewTypesList;

	public ViewTypesDialog()
	{
		super((java.awt.Frame)null, "Select view type");
		setModal(true);
		addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent evt) { ok = false; }});

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(panel);

		panel.setLayout(new BorderLayout(12, 12));

		JLabel caption = new JLabel("Choose the type of view to create:");
		panel.add(caption, BorderLayout.NORTH);


		// read all the 'viewtypes' from the properties
		Pollo pollo = Pollo.getInstance();
		Vector viewTypesVector = new Vector();
		String viewtypes = pollo.getProperty("viewtypes");
		StringTokenizer commaTokenizer = new StringTokenizer(viewtypes, ",");

		while (commaTokenizer.hasMoreTokens())
		{
			String name = commaTokenizer.nextToken().trim();
			String description = pollo.getProperty("viewtype." + name + ".description").trim();

			ViewType viewType = new ViewType(name, description);
			viewTypesVector.add(viewType);
		}

		viewTypesList = new JList(viewTypesVector);
		JScrollPane scrollPane = new JScrollPane(viewTypesList);
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

		// center on screen
		Dimension dimension = getSize();
		Dimension dimension2 = getToolkit().getScreenSize();
		setLocation((dimension2.width - dimension.width) / 2, (dimension2.height - dimension.height) / 2);
	}


	/**
	 * Combines the name and description of a viewtype.
	 */
	public class ViewType
	{
		protected String name;
		protected String description;

		public ViewType(String name, String description)
		{
			this.name = name;
			this.description = description;
		}

		public String getName()
		{
			return name;
		}

		public String toString()
		{
			return description;
		}
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

	/**
	 * @return true if the user selected ok, otherwise false
	 */
	public boolean showDialog()
	{
		show();
		return ok;
	}

	class WindowHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent evt)
		{
			ok = false;
		}
	}

	/**
	 * Get the name of the viewtype the user selected.
	 */
	public String getSelectedViewTypeName()
	{
		return ((ViewType)viewTypesList.getSelectedValue()).getName();
	}
}
