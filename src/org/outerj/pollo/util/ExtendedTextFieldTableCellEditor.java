package org.outerj.pollo.util;

import javax.swing.table.TableCellEditor;
import javax.swing.*;
import java.awt.*;

public class ExtendedTextFieldTableCellEditor extends AbstractCellEditor implements TableCellEditor, Valuable
{
	protected JPanel panel;
	protected JTextField textField;

	public ExtendedTextFieldTableCellEditor(Component extraStuff)
	{
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		textField = new JTextField("");
		textField.setBorder(null);
		panel.add(textField);
		panel.add(extraStuff);
	}

	public JTextField getTextField()
	{
		return textField;
	}

	public Valuable getValuable()
	{
		return this;
	}

	public void setValue(String value)
	{
		textField.setText(value);
	}

	public String getValue()
	{
		return textField.getText();
	}

	public void insertString(String value)
	{
		int pos = textField.getCaretPosition();
		try
		{
			textField.getDocument().insertString(pos, value, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public Component getTableCellEditorComponent(JTable table, Object value,
												 boolean isSelected,
												 int row, int column)
	{
		textField.setText(value != null ? value.toString() : "");
		return panel;
	}

	public Object getCellEditorValue()
	{
		return textField.getText();
	}
}
