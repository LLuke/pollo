package org.outerj.pollo.xmleditor.attreditor;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import org.outerj.pollo.xmleditor.model.Schema.ElementSchema;
import org.outerj.pollo.xmleditor.model.Schema.AttributeSchema;
import org.outerj.pollo.xmleditor.model.Schema;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.util.QuickSort;
import org.outerj.pollo.xmleditor.util.DomUtils;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;


/**
  A TableModel for the attributes of a DOM element.
  <p>
  The attributes are read once during the {@link #setElement(org.w3c.dom.Element) setElement} method, and for each
  attribute an instance of {@link org.outerj.pollo.xmleditor.AttributesTableModel.TempAttrEditInfo TempAttrEditInfo}
  is created. These are put in an ArrayList.
  <p>
  Appart from the attributes that exist on the element, the schema is also
  checked to see what attributes this element can have, and for these also
  TempAttrEditInfo's are created.
  <p>
  One of the reasons for working this way is because the attributes of an
  element have no sequence.
  <p>
  If the AttributeTableModel is no longer needed, setElement(null) should be
  called so that the DOM event listener is removed.

  @author Bruno Dumon.
 */
public class AttributesTableModel extends AbstractTableModel implements EventListener
{
	/** The element of which the attributes are currently shown. */
	protected Element element;
	/** The list containing instances of TempAttrEditInfo. */
	protected ArrayList attributes = new ArrayList();
	/** Flag indicating that DOM change events should be ignored because they are coming from us. */
	protected boolean doingApplyChanges = false;
	/** Reference to the schema. */
	protected Schema schema;
	protected XmlModel xmlModel;
	protected QuickSort sorter = new QuickSort(new TempAttrEditInfoComparator());


	/**
	 * Constructor.
	 *
	 * @param schema The schema to use.
	 */
	public AttributesTableModel(Schema schema, XmlModel xmlModel)
	{
		this.schema = schema;
		this.xmlModel = xmlModel;
	}

	public int getRowCount()
	{
		if (attributes != null)
		{
			return attributes.size();
		}
		return 0;
	}

	public int getColumnCount()
	{
		// 2 columns: qualified name, value
		return 2;
	}

	public Object getValueAt(int row, int column)
	{
		TempAttrEditInfo taei = (TempAttrEditInfo)attributes.get(row);

		switch (column)
		{
			case 0:
				return taei.getQName();
			case 1:
				return taei.value;
		}

		return null;
	}

	protected TempAttrEditInfo getTempAttrEditInfo(int row)
	{
		TempAttrEditInfo taei = (TempAttrEditInfo)attributes.get(row);
		return taei;
	}


	public void setValueAt(Object value, int row, int column)
	{
		if (value == null)
			value = "";

		TempAttrEditInfo taei = (TempAttrEditInfo)attributes.get(row);
		if (taei.value == null || !taei.value.equals(value))
		{
			taei.value = (String)value;

			// apply change to the DOM
			doingApplyChanges = true;
			element.setAttributeNS(taei.uri, DomUtils.getQName(taei.prefix, taei.name), taei.value);
			taei.isNew = false;
			doingApplyChanges = false;

			// make the whole row repaint
			fireTableChanged(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS,
						TableModelEvent.UPDATE));
		}

	}

	public boolean isCellEditable(int row, int column)
	{
		// only the attribute value is editable, not it's name
		if (column == 1)
			return true;
		else
			return false;
	}

	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0:
				return "Qualified Name";
			case 1:
				return "Value";
		}
		return "unknown";
	}


	/**
	  Changes the element of which the attributes are shown.
	  <p>
	  null is an allowed value.
	 */
	public void setElement(Element element)
	{
		// do some cleanup of the previous element.
		if (this.element != null)
		{
			((EventTarget)this.element).removeEventListener("DOMAttrModified", this, false);
		}
		this.element = element;
		this.attributes.clear();

		if (element == null)
			return;

		// create TempAttrEditInfo's for this element's attributes
		NamedNodeMap attrs = element.getAttributes();
		HashSet attrNames = new HashSet(attrs.getLength());
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Attr attr = (Attr)attrs.item(i);
			TempAttrEditInfo taei = new TempAttrEditInfo();
			taei.uri = attr.getNamespaceURI();
			taei.prefix = attr.getPrefix();
			taei.name = attr.getLocalName();
			taei.value = attr.getValue();
			taei.isNew = false;

			attributes.add(taei);

			attrNames.add((taei.uri != null ? taei.uri : "") + taei.name);
		}

		// get all possible attributes from the Schema, and merge them
		// with the attributes we already have.
		Iterator attrSchemaIt = schema.getAttributesFor(element).iterator();
		AttributeSchema attrSchema = null;
		while (attrSchemaIt.hasNext())
		{
			attrSchema = (AttributeSchema)attrSchemaIt.next();

			if (!attrNames.contains(attrSchema.namespaceURI + attrSchema.localName))
			{
				TempAttrEditInfo taei = new TempAttrEditInfo();
				taei.uri = attrSchema.namespaceURI;
				if (taei.uri != null)
					taei.prefix = xmlModel.findPrefixForNamespace(element, attrSchema.namespaceURI);
				taei.name = attrSchema.localName;
				taei.value = null;
				taei.isNew = true;

				attributes.add(taei);
			}
		}

		sorter.sort(attributes);

		// add an event listener
		((EventTarget)element).addEventListener("DOMAttrModified", this, false);
		fireTableChanged(new TableModelEvent(this));
	}



	/**
	  Removes an attribute. Changes are not directly applied to the DOM, call {@link #applyChanges()}
	  to do that.
	 */
	public void deleteAttribute(int row)
	{
		TempAttrEditInfo taei = (TempAttrEditInfo)attributes.get(row);
		taei.value = null;
		
		// apply change to the DOM
		if (!taei.isNew)
		{
			doingApplyChanges = true;
			element.removeAttributeNS(taei.uri, taei.name);
			doingApplyChanges = false;
		}
		taei.isNew = true;

		fireTableChanged(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
	}

	public void addAttribute(String namespaceURI, String prefix, String localName)
	{
		// check if this attribute already exists
		Iterator attributesIt = attributes.iterator();
		while (attributesIt.hasNext())
		{
			TempAttrEditInfo taei = (TempAttrEditInfo)attributesIt.next();
			if (((taei.uri == null && namespaceURI == null)
						|| (namespaceURI != null && taei.uri.equals(namespaceURI)))
					&& taei.name.equals(localName))
			{
				System.out.println("This attribute already exists, so I'm not going to add it.");
				return;
			}
		}

		// create the attribute
		TempAttrEditInfo taei = new TempAttrEditInfo();

		taei.prefix = prefix;
		taei.uri = namespaceURI;
		taei.name = localName;
		taei.value = null;
		taei.isNew = true;

		attributes.add(taei);

		int row = attributes.size() - 1;
		fireTableChanged(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	}

	/**
	 * This class holds temporary data about the attributes being edited.
	 * There is one instance of this class for each attribute.
	 * <p>
	 * Note: out of pure laziness, and since this class is only used internally inside
	 * {@link org.outerj.pollo.xmleditor.AttributesTableModel AttributesTableModel}, I havn't written
	 * getters/setters yet for the attributes.
	 */
	protected class TempAttrEditInfo
	{
		public String uri;
		public String prefix;
		public String name;
		public String value;
		public boolean isNew;

		public String getQName()
		{
			return DomUtils.getQName(prefix, name);
		}
	}

	public class TempAttrEditInfoComparator implements QuickSort.Comparator
	{
		public int compare(Object obj1, Object obj2)
		{
			String s1 = ((TempAttrEditInfo)obj1).getQName();
			String s2 = ((TempAttrEditInfo)obj2).getQName();
			return s1.compareTo(s2);
		}
	}


	/**
	 * The element who's attributes we show.
	 * */
	public Element getElement()
	{
		return element;
	}


	/**
	 * DOM event handler. If someone else changed this element's attributes,
	 * do a reinitialisation.
	 * */
	public void handleEvent(Event e)
	{
		try
		{
			if (!doingApplyChanges)
			{
				setElement(element);
				fireTableChanged(new TableModelEvent(this));
			}
		}
		catch (Exception exc)
		{
			// note: events thrown inside a 'handleEvent' method get catched by
			// the DOM implementation, so if we don't print a message here we
			// might not even know an exception occured.
			System.out.println("Error in AttributesTableModel.handleEvent: " + e);
		}
	}
}
