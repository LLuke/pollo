package org.outerj.xmleditor;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.dnd.*;
import java.util.HashMap;

import org.outerj.xmleditor.DisplaySpecification.ElementSpec;

/**
  This is the panel from which the user can drag elements to insert them into
  sitemap. It consists of an extended JList ({@link org.outerj.xmleditor.ElementDragPanel.DraggableList DraggableList})
  using a custom ListCellRenderer to show the colored icons
  ({@link org.outerj.xmleditor.ElementDragPanel.ElementSpecCellRenderer ElementSpecCellRenderer}).

  The drag-and-drop uses the {@link org.outerj.xmleditor.CommandTransferable CommandTransferable} class
  to insert new elements.
 */
public class ElementDragPanel extends JPanel
{
	DraggableList elementList;

	public ElementDragPanel(XmlEditor xmlEditor)
	{
		Object [] elementSpecsArray = xmlEditor.getXmlContentEditor().getDisplaySpec().getElementSpecs().toArray();
		elementList = new DraggableList(elementSpecsArray);
		elementList.setCellRenderer(new ElementSpecCellRenderer());
		
		// the height of items in the list is set manually, because JList only
		// looks at the height of the font causing the icons to overlap
		// (depending on look-and-feel and platform)
		int fontsize = elementList.getFont().getSize();
		int iconsize = ElementColorIcon.ICON_HEIGHT;
		int maxheight = fontsize > iconsize ? fontsize : iconsize;
		elementList.setFixedCellHeight(maxheight + 4);

		this.setLayout(new BorderLayout());
		add(elementList, BorderLayout.CENTER);
	}

	
	/**
	  A custom ListCellRenderer that can display both an icon and text.
	 */
	public class ElementSpecCellRenderer extends JLabel implements ListCellRenderer
	{
		public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
		{
			ElementSpec elementSpec = (ElementSpec)value;
			setText(elementSpec.localName);
			setIcon(elementSpec.icon);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			return this;
		}
	}

	public class DraggableList extends JList implements DragSourceListener, DragGestureListener
	{
		public DraggableList(Object [] data)
		{
			super (data);

			DragSource dragSource = DragSource.getDefaultDragSource();
			dragSource.createDefaultDragGestureRecognizer(
					this, DnDConstants.ACTION_MOVE, this);
		}


		public void dragGestureRecognized(DragGestureEvent dragGestureEvent)
		{
			int selected = locationToIndex(dragGestureEvent.getDragOrigin());

			if (selected == -1)
				return;

			ElementSpec selectedValue = (ElementSpec)getModel().getElementAt(selected);
			if (selectedValue == null)
			{
				// Nothing selected, nothing to drag
				getToolkit().beep();
			}
			else
			{
				HashMap commandInfo = new HashMap();
				commandInfo.put("command", "insertelement");
				commandInfo.put("uri", selectedValue.nsUri);
				commandInfo.put("localName", selectedValue.localName);
				CommandTransferable transferable = new CommandTransferable(commandInfo);

				dragGestureEvent.startDrag(DragSource.DefaultMoveDrop, transferable, this);
			}
		}

		public void dragDropEnd(DragSourceDropEvent DragSourceDropEvent){}
		public void dragEnter(DragSourceDragEvent DragSourceDragEvent){}
		public void dragExit(DragSourceEvent DragSourceEvent){}
		public void dragOver(DragSourceDragEvent DragSourceDragEvent){}
		public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent){}
	}

}
