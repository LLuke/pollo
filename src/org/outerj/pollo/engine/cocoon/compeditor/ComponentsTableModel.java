package org.outerj.pollo.engine.cocoon.compeditor;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.Disposable;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;

public class ComponentsTableModel extends AbstractTableModel implements EventListener, Disposable
{
	protected Element componentsEl;
	protected String  prefix;
	protected XmlModel xmlModel;
	protected String componentsName;
	protected String componentsURI;
	protected boolean labels; // indicates wheter this component can have a 'label' attribute
	protected String classColumn;
	
	protected ArrayList componentElements;

	public ComponentsTableModel(Element componentsEl, String componentsName, String componentsURI,
			String classColumn, boolean labels, XmlModel xmlModel)
	{
		this.componentsEl = componentsEl;
		this.xmlModel = xmlModel;
		this.componentsName = componentsName;
		this.componentsURI = componentsURI;
		this.labels = labels;
		this.classColumn = classColumn;

		((EventTarget)componentsEl).addEventListener("DOMAttrModified", this, false);
		((EventTarget)componentsEl).addEventListener("DOMNodeInserted", this, false);
		((EventTarget)componentsEl).addEventListener("DOMNodeRemoved", this, false);

		refresh(null);
	}

	public int getRowCount()
	{
		return componentElements.size();
	}


	public int getColumnCount()
	{
		// 4 columns: default, name, label, src
		if (labels)
			return 4;
		else
			return 3;
	}

	public Object getValueAt(int row, int column)
	{
		Element element = (Element)componentElements.get(row);
		switch (column)
		{
			case 0:
				{
					String defaultOne = componentsEl.getAttributeNS(null, "default");
					if (defaultOne != null && element.getAttributeNS(null, "name").equals(defaultOne))
					{
						return new ImageIcon(getClass().getResource("/org/outerj/pollo/engine/cocoon/compeditor/default.png"));
					}
				}
			case 1:
				return element.getAttributeNS(null, "name");
			case 2:
				if (labels)
					return element.getAttributeNS(null, "label");
				else
					return element.getAttributeNS(null, classColumn);
			case 3:
				return element.getAttributeNS(null, classColumn);
		}
		return "unknown";
	}

	public void setValueAt(Object value, int row, int column)
	{
		Element currentEl = getElementOnRow(row);
		if (column == 2 && labels)
		{
			currentEl.setAttributeNS(null, "label", value.toString());
		}
		else if (column == 2 && !labels)
		{
			currentEl.setAttributeNS(null, classColumn, value.toString());
		}
		else if (column == 3 && labels)
		{
			currentEl.setAttributeNS(null, classColumn, value.toString());
		}
	}

	public boolean isCellEditable(int row, int column)
	{
		if (column == 2 || column == 3)
			return true;
		else
			return false;
	}

	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0:
				return ""; // default
			case 1:
				return "Name";
			case 2:
				if (labels)
					return "Label";
				else
					return classColumn;
			case 3:
				return classColumn;
		}
		return "unknown";
	}


	/**
	  @param removedElement element that has been removed, or null if none
	  */
	public void refresh(Element removedElement)
	{
		this.componentElements = new ArrayList();

		NodeList nodeList = componentsEl.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Element.ELEMENT_NODE && node.getLocalName() != null &&
					node.getLocalName().equals(componentsName) && node.getNamespaceURI() != null
					&& node.getNamespaceURI().equals(componentsURI) && node != removedElement)
			{
				componentElements.add(node);
			}
		}
	}

	public Class getColumnClass(int c)
	{
		if (c == 0)
			return ImageIcon.class;
		else
			return String.class;
	}

	public void handleEvent(Event e)
	{
		try
		{
			if (e.getType().equals("DOMNodeInserted"))
			{
				refresh(null);
			}
			else if (e.getType().equals("DOMNodeRemoved"))
			{
				refresh((Element)e.getTarget());
			}

			fireTableChanged(new TableModelEvent(this));
		}
		catch (Exception exc)
		{
			System.out.println("Error in ComponentsTableModel.handleEvent:");
			exc.printStackTrace();
		}
	}

	public Element getElementOnRow(int row)
	{
		return (Element)componentElements.get(row);
	}

	public void dispose()
	{
		((EventTarget)componentsEl).removeEventListener("DOMAttrModified", this, false);
		((EventTarget)componentsEl).removeEventListener("DOMNodeInserted", this, false);
		((EventTarget)componentsEl).removeEventListener("DOMNodeRemoved", this, false);
	}
}
