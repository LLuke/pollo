package org.outerj.pollo.xmleditor.attreditor;

import org.outerj.pollo.xmleditor.attreditor.AttributesTableModel.TempAttrEditInfo;

import java.awt.Component;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * TableCellRenderer used by the attributes panel. If an attribute has no
 * value, the attribute name will appear 'disabled' (usually light grey)
 * and for its value a dash (-) will be shown.
 *
 * @author Bruno Dumon
 */
public class AttributeTableCellRenderer extends DefaultTableCellRenderer
{
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

		AttributesTableModel tableModel = (AttributesTableModel)table.getModel();
		TempAttrEditInfo taei = tableModel.getTempAttrEditInfo(row);

		switch (column)
		{
			case 0:
				setText(taei.getQName());
				if (taei.value == null)
					setEnabled(false);
				else
					setEnabled(true);
				break;
			case 1:
				if (taei.value == null)
				{
					setEnabled(false);
					setText(" - ");
				}
				else
				{
					setEnabled(true);
					setText(taei.value);
				}
				break;
		}
		return this;
	}

	protected void setValue(Object value)
	{
		// empty on purpose
	}
}
