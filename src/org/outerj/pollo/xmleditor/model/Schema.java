package org.outerj.pollo.xmleditor.model;

import org.outerj.pollo.xmleditor.util.NodeMap;
import org.outerj.pollo.xmleditor.util.MyPrefixResolver;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.NodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.DOMHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;

import javax.xml.parsers.*;


/**
  Simple schema implementation. This class can provide the following information:
  <ul>
    <li>which attributes an element can have</li>
	<li>(optionally) a list of possible attribute values to select form</li>
	<li>which subelements an element can have</li>
  <ul>
  The schema information is read from an XML file with a custom syntax, for
  an example see the sitemapschema.xml file.
  <p>
  The long-term goal is to enhance this class with support for standard XML Schemas.
  <p>
  Do not instantiate this class directly but use the getInstance method.

  @author <a href="mailto:bruno.dumon@advalvas.be">Bruno Dumon</a>
 */
public class Schema
{
	protected static HashMap instances = new HashMap(2);
	protected MyPrefixResolver prefixResolver = new MyPrefixResolver();
	protected NodeMap elementSchemas;

	/**
	 * Get a schema object. Schema objects are cached (based on their location),
	 * so that the same schema isn't read twice.
	 *
	 * @param location the location of the schema file.
	 */
	public static synchronized Schema getInstance(String location)
		throws Exception
	{
		Schema schema = (Schema)instances.get(location);
		if (schema == null)
		{
			schema = new Schema(location);
			instances.put(location, schema);
		}
		return schema;
	}

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

	protected Schema(String location)
		throws Exception
	{
		SchemaHandler schemaHandler = new SchemaHandler(this);
		elementSchemas = new NodeMap();

		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(new InputSource(location), schemaHandler);
	}

	protected void addElementSchema(ElementSchema elementSchema)
	{
		elementSchemas.put(elementSchema.namespaceURI, elementSchema.localName, elementSchema);
	}

	protected AttributeSchema getAttributeSchema(Element element, String namespaceURI, String localName)
	{
		ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());
		if (namespaceURI == null) namespaceURI = "";

		if (elementSchema == null)
			return null;
		else
		{
			Iterator attrSchemaIt = elementSchema.attributes.iterator();
			while (attrSchemaIt.hasNext())
			{
				AttributeSchema attrSchema = (AttributeSchema)attrSchemaIt.next();
				if (attrSchema.namespaceURI.equals(namespaceURI) && attrSchema.localName.equals(localName))
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
	}

	public class AttributeSchema
	{
		public String namespaceURI;
		public String localName;
		public String xpath;
		public String [] values;

		public String [] getPossibleValues(Element element)
		{
			if (xpath == null)
			{
				if (values != null)
					return values;
				return null;
			}
			else
			{
				// execute xpath
				NodeList nodes;
				try
				{
					// the low-level xalan xpath api is used so that a
					// custom prefixresolver can be specified.
					XPath compiledXPath = new XPath(xpath, null, prefixResolver, XPath.SELECT, null);
					XObject list = compiledXPath.execute(new XPathContext(), element /*context node*/, prefixResolver);
					nodes = list.nodelist();
				}
				catch (Exception e)
				{
					System.out.println("Error executing xpath " + xpath + ": " + e.toString());
					return null;
				}

				// construct return value array
				String [] values = new String[nodes.getLength()];
				for (int i = 0; i < nodes.getLength(); i++)
				{
					Node node = nodes.item(i);
					values[i] = DOMHelper.getNodeData(node);
				}
				return values;
			}
		}
	}

	public class SubElement
	{
		public String namespaceURI;
		public String localName;
	}


	/*
	  Here comes the parser.
	 */
	protected class SchemaHandler extends DefaultHandler
	{
		protected Schema schema;

		protected boolean inElement;
		protected boolean inAttribute;
		protected String elementName, attributeName;

		protected ElementSchema currentElementSchema;
		protected AttributeSchema currentAttributeSchema;
		protected NamespaceSupport nsSupport = new NamespaceSupport();
		protected String[] nameParts = new String[3];



		public SchemaHandler(Schema schema)
		{
			this.schema = schema;
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
				currentElementSchema.namespaceURI = nameParts[0];
				currentElementSchema.localName = nameParts[1];
			}
			else if (localName.equals("attribute"))
			{
				if (!inElement) throw new SAXException("SchemaHandler: 'attribute' element only allowed inside an 'element' element.");
				inAttribute = true;
				attributeName = atts.getValue("name");
				nameParts = nsSupport.processName(attributeName, nameParts, false);
				currentAttributeSchema = new AttributeSchema();
				currentAttributeSchema.namespaceURI = nameParts[0];
				currentAttributeSchema.localName = nameParts[1];
				currentAttributeSchema.xpath = atts.getValue("readvaluesfrom");
				if (currentAttributeSchema.xpath == null)
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
					SubElement subelement = new SubElement();
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
					prefixResolver.add(atts.getQName(i), atts.getValue(i));
				}
			}
		}

		
		public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException
		{
			if (localName.equals("element"))
			{
				inElement = false;
				schema.addElementSchema(currentElementSchema);
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
