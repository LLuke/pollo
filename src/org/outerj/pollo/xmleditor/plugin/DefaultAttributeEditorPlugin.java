package org.outerj.pollo.xmleditor.plugin;

import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import java.util.HashMap;

import org.w3c.dom.Element;

/**
 * Default implementation of the AttributeEditorPlugin abstract class.
 *
 * @author Bruno Dumon
 */
public class DefaultAttributeEditorPlugin implements IAttributeEditorPlugin
{
	XmlModel xmlModel;
	ISchema schema;

	public void init(HashMap initParams, XmlModel xmlModel, ISchema schema)
	{
		this.xmlModel = xmlModel;
		this.schema = schema;
	}

	public TableCellEditor getAttributeEditor(Element element, String namespaceURI, String localName)
	{
		String [] values = schema.getPossibleAttributeValues(element, namespaceURI, localName);
		if (values != null)
		{
			JComboBox combo = new JComboBox(values);
			combo.setEditable(true);
			return new DefaultCellEditor(combo);
		}
		return null;
	}
}
