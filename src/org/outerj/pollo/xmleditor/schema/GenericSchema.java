package org.outerj.pollo.xmleditor.schema;

import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Element;

/**
 * This is a generic implementation of the ISchema interface.
 * Generic means that it is useable for any type of XML file
 * (but it doesn't do anything usefull).
 *
 * @author Bruno Dumon
 */
public class GenericSchema implements ISchema
{
	public void init(HashMap initParams)
	{
	}

	public Collection getAttributesFor(Element element)
	{
		return Collections.EMPTY_LIST;
	}

	public boolean isChildAllowed(Element parent, Element child)
	{
		return true;
	}

	public String [] getPossibleAttributeValues(Element element, String namespaceURI, String localName)
	{
		return new String[0];
	}

	public Collection getAllowedSubElements(Element element)
	{
		return Collections.EMPTY_LIST;
	}
}
