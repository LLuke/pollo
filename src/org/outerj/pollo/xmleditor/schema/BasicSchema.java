package org.outerj.pollo.xmleditor.schema;

import org.outerj.pollo.xmleditor.util.NodeMap;
import org.outerj.pollo.xmleditor.schema.ElementSchema.SubElement;
import org.outerj.pollo.xmleditor.exception.PolloException;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;

import javax.xml.parsers.*;

import org.jaxen.SimpleNamespaceContext;


/**
 * Simple schema implementation. This class can provide the following information:
 * <ul>
 * <li>which attributes an element can have</li>
 * <li>(optionally) a list of possible attribute values to select form</li>
 * <li>which subelements an element can have</li>
 * <ul>
 * The schema information is read from an XML file with a custom syntax, for
 * an example see the sitemapschema.xml file.
 *
 * @author Bruno Dumon
 */
public class BasicSchema implements ISchema
{
	protected SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
	protected NodeMap elementSchemas;

	/**
	 * Returns the list of attributes an element can have.
	 */
	public Collection getAttributesFor(Element element)
	{
		ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());

		if (elementSchema == null)
			return new LinkedList();
		else
			return elementSchema.attributes;
	}

	/**
	 * Returns true if the element <i>child</i> is allowed as child
	 * of the element <i>parent</i>.
	 */
	public boolean isChildAllowed(Element parent, Element child)
	{
		ElementSchema elementSchema = getElementSchema(parent.getNamespaceURI(), parent.getLocalName());
		if (elementSchema != null)
		{
			return elementSchema.isAllowedAsSubElement(child);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns an array containing a list of possible values an attribute can have,
	 * or null if such a list is not available.
	 */
	public String [] getPossibleAttributeValues(Element element, String namespaceURI, String localName)
	{
		AttributeSchema attrSchema = getAttributeSchema(element, namespaceURI, localName);
		if (attrSchema != null)
		{
			return attrSchema.getPossibleValues(element);
		}
		return null;
	}


	public Collection getAllowedSubElements(Element element)
	{
		ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());
		if (elementSchema != null)
		{
			return elementSchema.subelements.values();
		}
		else
		{
			return Collections.EMPTY_LIST;
		}
	}


	// The rest is not part of the public interface

	protected void init(HashMap initParams)
		throws Exception
	{
		String source = (String)initParams.get("source");
		if (source == null || source.trim().equals(""))
		{
			throw new PolloException("[BasicSchema] The source init-param is not specified!");
		}

		SchemaHandler schemaHandler = new SchemaHandler();
		elementSchemas = new NodeMap();

		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(new InputSource(this.getClass().getClassLoader().getResourceAsStream(source)),
				schemaHandler);
	}

	protected void addElementSchema(ElementSchema elementSchema)
	{
		elementSchemas.put(elementSchema.namespaceURI, elementSchema.localName, elementSchema);
	}

	protected AttributeSchema getAttributeSchema(Element element, String namespaceURI, String localName)
	{
		ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());

		if (elementSchema == null)
			return null;
		else
		{
			Iterator attrSchemaIt = elementSchema.attributes.iterator();
			while (attrSchemaIt.hasNext())
			{
				AttributeSchema attrSchema = (AttributeSchema)attrSchemaIt.next();
				if (((attrSchema.namespaceURI == null && namespaceURI == null)
							|| (attrSchema.namespaceURI != null
								&& attrSchema.namespaceURI.equals(namespaceURI)))
						&& attrSchema.localName.equals(localName))
				{
					return attrSchema;
				}
			}
		}
		return null;
	}

	protected ElementSchema getElementSchema(String namespaceURI, String localName)
	{
		return (ElementSchema)elementSchemas.get(namespaceURI, localName);
	}



	/*
	  Here comes the parser.
	 */
	protected class SchemaHandler extends DefaultHandler
	{
		protected boolean inElement;
		protected boolean inAttribute;
		protected String elementName, attributeName;

		protected ElementSchema currentElementSchema;
		protected AttributeSchema currentAttributeSchema;
		protected NamespaceSupport nsSupport = new NamespaceSupport();
		protected String[] nameParts = new String[3];



		public SchemaHandler()
		{
		}
		
		public void startPrefixMapping(String prefix, String uri)
			throws SAXException
		{
			nsSupport.declarePrefix(prefix, uri);
		}

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts)
			throws SAXException
		{
			nsSupport.pushContext();
			if (localName.equals("element"))
			{
				inElement = true;
				elementName = atts.getValue("name");
				currentElementSchema = new ElementSchema();
				nameParts = nsSupport.processName(elementName, nameParts, false);
				currentElementSchema.namespaceURI = nameParts[0].equals("") ? null : nameParts[0];
				currentElementSchema.localName = nameParts[1];
			}
			else if (localName.equals("attribute"))
			{
				if (!inElement) throw new SAXException("SchemaHandler: 'attribute' element only allowed inside an 'element' element.");
				inAttribute = true;
				attributeName = atts.getValue("name");
				nameParts = nsSupport.processName(attributeName, nameParts, false);
				currentAttributeSchema = new AttributeSchema(namespaceContext);
				currentAttributeSchema.namespaceURI = nameParts[0].equals("") ? null : nameParts[0];
				currentAttributeSchema.localName = nameParts[1];
				currentAttributeSchema.xpathExpr = atts.getValue("readvaluesfrom");
				if (currentAttributeSchema.xpathExpr == null)
				{
					String choosefrom = atts.getValue("choosefrom");
					if (choosefrom != null)
					{
						StringTokenizer tokenizer = new StringTokenizer(choosefrom, ",");
						currentAttributeSchema.values = new String[tokenizer.countTokens()];
						int i = 0;
						while (tokenizer.hasMoreTokens())
						{
							currentAttributeSchema.values[i] = tokenizer.nextToken();
							i++;
						}
					}
				}
			}
			else if (localName.equals("allowedsubelements"))
			{
				if (!inElement) throw new SAXException("SchemaHandler: 'allowedsubelements' element only allowed inside an 'element' element.");
				String names = atts.getValue("names");
				StringTokenizer tokenizer = new StringTokenizer(names, ",");

				int i = 0;
				while (tokenizer.hasMoreTokens())
				{
					SubElement subelement = currentElementSchema.createSubElement();
					nameParts = nsSupport.processName(tokenizer.nextToken(), nameParts, false);
					subelement.namespaceURI = nameParts[0];
					subelement.localName    = nameParts[1];
					currentElementSchema.subelements.put(subelement.namespaceURI, subelement.localName, subelement);
					i++;
				}
			}
			else if (localName.equals("xpath-ns-prefixes"))
			{
				int count = atts.getLength();
				for (int i = 0; i < count; i++)
				{
					namespaceContext.addNamespace(atts.getQName(i), atts.getValue(i));
				}
			}
		}

		
		public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException
		{
			if (localName.equals("element"))
			{
				inElement = false;
				addElementSchema(currentElementSchema);
				currentElementSchema = null;
			}
			else if (localName.equals("attribute"))
			{
				inAttribute = false;
				currentElementSchema.attributes.add(currentAttributeSchema);
				currentAttributeSchema = null;
			}
			nsSupport.popContext();
		}
		
		public void characters(char[] ch, int start, int length)
			throws SAXException
		{
		}

		public void endDocument()
		{
		}

	}
}