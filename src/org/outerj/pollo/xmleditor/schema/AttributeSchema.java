package org.outerj.pollo.xmleditor.schema;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Iterator;

import org.jaxen.dom.XPath;
import org.jaxen.NamespaceContext;
import org.jaxen.dom.DocumentNavigator;
import org.jaxen.function.StringFunction;

/**
 * This class represents the definition of an attribute
 * in a Schema.
 *
 * @author Bruno Dumon
 */
public class AttributeSchema
{
	public NamespaceContext namespaceContext;
	public String namespaceURI;
	public String localName;
	public String xpathExpr;
	public String [] values;

	public AttributeSchema(NamespaceContext namespaceContext)
	{
		this.namespaceContext = namespaceContext;
	}

	public String [] getPossibleValues(Element element)
	{
		if (xpathExpr == null)
		{
			if (values != null)
				return values;
			return null;
		}
		else
		{
			// execute xpath
			List nodes;
			try
			{
				XPath xpath = new XPath(xpathExpr);
				xpath.setNamespaceContext(namespaceContext);
				nodes = xpath.selectNodes(element);
			}
			catch (Exception e)
			{
				System.out.println("Error executing xpath " + xpathExpr + ": " + e.toString());
				return null;
			}

			// construct return value array
			String [] values = new String[nodes.size()];
			Iterator nodesIt = nodes.iterator();
			int i = 0;
			DocumentNavigator navigator = new DocumentNavigator();
			while (nodesIt.hasNext())
			{
				Node node = (Node)nodesIt.next();
				values[i] = StringFunction.evaluate(node, navigator);
				i++;
			}
			return values;
		}
	}

	public boolean hasPickList()
	{
		if (xpathExpr != null || values != null)
			return true;
		else
			return false;
	}
}
