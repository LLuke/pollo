package org.outerj.pollo.util;

import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * An extension of JComboBox that implements the Valuable interface
 * from CustomTableCellEditor.
 *
 * @author Bruno Dumon
 */
public class ComboBoxValuable extends JComboBox implements CustomTableCellEditor.Valuable
{
	public static org.apache.log4j.Category logcat =
		org.apache.log4j.Category.getInstance(ComboBoxValuable.class.getName());

	public ComboBoxValuable()
	{
		super();
		setEditable(true);
	}

	public void setValue(String value)
	{
		JTextField text = (JTextField)getEditor().getEditorComponent();
		text.setText(value);
	}

	public String getValue()
	{
		JTextField text = (JTextField)getEditor().getEditorComponent();
		return text.getText();
	}

	public void insertString(String value)
	{
		JTextField text = (JTextField)getEditor().getEditorComponent();
		int pos = text.getCaretPosition();
		try
		{
			text.getDocument().insertString(pos, value, null);
		}
		catch (Exception e)
		{
			logcat.error("Error inserting text", e);
		}
	}
}
