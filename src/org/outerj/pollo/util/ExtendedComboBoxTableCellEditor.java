package org.outerj.pollo.util;

import javax.swing.table.TableCellEditor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ExtendedComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor
{
	protected ExtendedComboBox comboBox;

	public ExtendedComboBoxTableCellEditor(Component extraStuff)
	{
		comboBox = new ExtendedComboBox(extraStuff);
	}

	public Object getCellEditorValue()
	{
		return comboBox.getEditor().getItem();
	}

	public Valuable getValuable()
	{
		return comboBox;
	}

	public void setModel(ComboBoxModel comboBoxModel)
	{
		comboBox.setModel(comboBoxModel);
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
												 boolean isSelected,
												 int row, int column)
	{
		comboBox.getEditor().setItem(value);
		return comboBox;
	}

	protected class ExtendedComboBox extends JComboBox implements Valuable
	{
		protected JTextField textField;
		protected ExtendedComboBoxPanel textFieldContainer;

		public ExtendedComboBox(Component extraStuff)
		{
			setEditable(true);
			textField = new JTextField("");
			textField.setBorder(null);
			textFieldContainer = new ExtendedComboBoxPanel();
			textFieldContainer.setLayout(new BoxLayout(textFieldContainer, BoxLayout.X_AXIS));
			textFieldContainer.add(textField);
			textFieldContainer.add(extraStuff);
			setEditor(textFieldContainer);

			addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					ExtendedComboBoxTableCellEditor.this.fireEditingStopped();
					comboBox.actionPerformed(new ActionEvent(textFieldContainer, 0, ""));
				}
			});
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


		protected class ExtendedComboBoxPanel extends JPanel implements ComboBoxEditor
		{
			public Component getEditorComponent()
			{
				return this;
			}

			public void setItem(Object anObject)
			{
                if (anObject != null)
					textField.setText(anObject.toString());
				else
					textField.setText("");
			}

			public Object getItem()
			{
				return textField.getText();
			}

			public void selectAll()
			{
				textField.selectAll();
				textField.requestFocus();
			}

			public void addActionListener(ActionListener listener)
			{
				textField.addActionListener(listener);
			}

			public void removeActionListener(ActionListener listener)
			{
				textField.removeActionListener(listener);
			}

		}
	}
}
