package org.outerj.pollo.util;

import javax.swing.JTextField;


/**
 * An extension of JTextField that implements the Valuable interface
 * from CustomTableCellEditor.
 *
 * @author Bruno Dumon
 */
public class TextFieldValuable extends JTextField implements CustomTableCellEditor.Valuable
{
	public static org.apache.log4j.Category logcat =
		org.apache.log4j.Category.getInstance(TextFieldValuable.class.getName());

	public void setValue(String value)
	{
		setText(value);
	}

	public String getValue()
	{
		return getText();
	}

	public void insertString(String value)
	{
		int pos = getCaretPosition();
		try
		{
			getDocument().insertString(pos, value, null);
		}
		catch (Exception e)
		{
			logcat.error("Error inserting text.", e);
		}
	}
}
