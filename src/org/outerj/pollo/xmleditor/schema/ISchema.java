package org.outerj.pollo.xmleditor.schema;

import java.util.Collection;
import org.w3c.dom.Element;

/**
 * Schema implementations should implement this interface.
 * Because Element's are passed as arguments to the methods,
 * rather than String, the Schema implementation could potentially
 * be made context-sensitive (support name overloading).
 *
 * @author Bruno Dumon
 */
public interface ISchema
{
	public Collection getAttributesFor(Element element);

	public boolean isChildAllowed(Element parent, Element child);

	public String [] getPossibleAttributeValues(Element element, String namespaceURI, String localName);

	public Collection getAllowedSubElements(Element element);
}
