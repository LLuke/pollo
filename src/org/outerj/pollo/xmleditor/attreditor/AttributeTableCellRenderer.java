package org.outerj.pollo.xmleditor.attreditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.outerj.pollo.xmleditor.attreditor.AttributesTableModel.TempAttrEditInfo;
import java.awt.*;

/**
 * TableCellRenderer used by the attributes panel. If an attribute has no
 * value, the attribute name will appear 'disabled' (usually light grey)
 * and for its value a dash (-) will be shown.
 *
 * @author Bruno Dumon
 */
public class AttributeTableCellRenderer extends DefaultTableCellRenderer
{
	protected TempAttrEditInfo taei = null;
	protected int column = 0;

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

		AttributesTableModel tableModel = (AttributesTableModel)table.getModel();
		taei = tableModel.getTempAttrEditInfo(row);
		this.column = column;

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

	public void paint(Graphics g)
	{
		super.paint(g);
		if (column == 1 && taei.attrSchema != null && taei.attrSchema.hasPickList())
		{
			g.setColor(UIManager.getColor("Label.disabledForeground"));

			int arrowWidth = 10;
			int arrowHeight = 5;
			int x = getWidth() - arrowWidth - 2;
			int y = (getHeight() / 2) - 2;

			g.translate(x, y);

			g.drawLine( 0, 0, arrowWidth - 1, 0 );
			g.drawLine( 1, 1, 1 + (arrowWidth - 3), 1 );
			g.drawLine( 2, 2, 2 + (arrowWidth - 5), 2 );
			g.drawLine( 3, 3, 3 + (arrowWidth - 7), 3 );
			g.drawLine( 4, 4, 4 + (arrowWidth - 9), 4 );

			g.translate(-x, -y);
		}
	}

}
