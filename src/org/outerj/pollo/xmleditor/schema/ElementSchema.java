package org.outerj.pollo.xmleditor.schema;

import org.w3c.dom.Element;
import java.util.LinkedList;
import org.outerj.pollo.xmleditor.util.NodeMap;

/**
 * This class represents the definition of an element
 * in a Schema
 *
 * @author Bruno Dumon
 */
public class ElementSchema
{
	public String namespaceURI;
	public String localName;

	public LinkedList attributes = new LinkedList();
	public NodeMap subelements = new NodeMap();

	public boolean isAllowedAsSubElement(Element element)
	{
		SubElement subelement = (SubElement)subelements.get(element.getNamespaceURI(), element.getLocalName());
		if (subelement == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public class SubElement
	{
		public String namespaceURI;
		public String localName;
	}

	public SubElement createSubElement()
	{
		return new SubElement();
	}
}
