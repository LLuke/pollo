package org.outerj.pollo.xmleditor.plugin;

import javax.swing.table.TableCellEditor;
import org.w3c.dom.Element;

public interface IAttributeEditorPlugin
{
	public TableCellEditor getAttributeEditor(Element element,
			String namespaceURI, String localName);
}
