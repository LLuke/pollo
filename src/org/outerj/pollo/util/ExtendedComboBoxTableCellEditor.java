package org.outerj.pollo.util;

import javax.swing.table.TableCellEditor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A TableCellEditor that consists of a combobox with (optionally) some
 * extra components next to it (e.g. a button to browse for a file name, etc.).
 * The extra components are placed left of the dropdown button.
 *
 * <p>Using the methods in the Valuable interface, the contents of the textfield
 * can be manipulated in a uniform way as for the ExtendedTextFieldCellEditor.
 *
 * @author Bruno Dumon
 */
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

	public JTextField getTextField()
	{
		return comboBox.textField;
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
		protected PublicKeyBindingTextField textField = new PublicKeyBindingTextField();
		protected ExtendedComboBoxPanel textFieldContainer;

		public ExtendedComboBox(Component extraStuff)
		{
			setEditable(true);
			textField.setBorder(null);
			textFieldContainer = new ExtendedComboBoxPanel();
			textFieldContainer.setLayout(new BoxLayout(textFieldContainer, BoxLayout.X_AXIS));
			textFieldContainer.add(textField);
			textFieldContainer.add(extraStuff);
			setEditor(textFieldContainer);

			textField.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					ExtendedComboBoxTableCellEditor.this.fireEditingStopped();
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

			public void requestFocus()
			{
				textField.requestFocus();
			}

		}


		protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
											int condition, boolean pressed)
		{
			if (textField.processKeyBindingPublic(ks, e, condition, pressed))
				return true;
			else
				return super.processKeyBinding(ks, e, condition, pressed);
		}
	}
}
