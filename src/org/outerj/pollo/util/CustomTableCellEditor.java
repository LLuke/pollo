package org.outerj.pollo.util;

import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.JTextField;


/**
 * This class implements the javax.swing.table.TableCellEditor interface.
 * It is meant to be a generic solution where any java.awt.Component can
 * be used as a cell editor.
 *
 * For this to work, two objects must be passed to the constructor:
 *  - the component
 *  - an object implementing the 'Valuable' interface
 *
 * The reason for this split is to support the case where the component itself
 * is eg a JPanel or a Box containing multiple components (such as a textfield
 * with some buttons next to it). One of the components on the panel will
 * display/edit the value. It is this component that must implement the
 * Valuable interface.
 *
 * Two default implementation are provided of the Valuable interface: a
 * TextFieldValuable and a ComboBoxValuable.
 *
 * @author Bruno Dumon
 */
public class CustomTableCellEditor extends AbstractCellEditor implements TableCellEditor
{ 

	public interface Valuable
	{
		public String getValue();

		public void setValue(String value);

		/**
		 * Inserts the given string at the caret position.
		 */
		public void insertString(String value);
	}


    protected Valuable valuable;
	protected Component component;


    public CustomTableCellEditor(Component component, Valuable valuable)
	{
		this.component = component;
		this.valuable = valuable;
    }



    public Object getCellEditorValue()
	{
		return valuable.getValue();
    }

    public boolean isCellEditable(EventObject event)
	{ 
		return true;
    }
    
    public boolean shouldSelectCell(EventObject event)
	{ 
		return true;
    }

    public boolean stopCellEditing()
	{
	    fireEditingStopped(); 
		return true;
    }

    public void cancelCellEditing()
	{
	   fireEditingCanceled(); 
    }


	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column)
	{
		valuable.setValue((String)(value == null ? "" : value.toString()));
		return component;
    }
}
