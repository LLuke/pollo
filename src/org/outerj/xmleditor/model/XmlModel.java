package org.outerj.xmleditor.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;


/**
  DOM-based model for an xml file. This class adds:
  <ul>
    <li>loading/saving of the XML file</li>
	<li>functions for: prefix to namespace, namespace to prefix, finding default namespace.</li>
	<li>function for getting an element based on an xpath expression</li>
  </ul>
 */
public class XmlModel
{
	Document document;
	Element pipelines;
	String source;
	Undo undo;

	public XmlModel(String source)
		throws InvalidXmlException
	{
		System.out.println("Parsing XML file...");
		this.source = source;
		try
		{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", new Boolean(false));
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(new InputSource(source));
		}
		catch (Exception e)
		{
			throw new InvalidXmlException("Error occured during parsing xml: " + e.getMessage());
		}
		System.out.println("done");
		undo = new Undo(this);
	}

	public Document getDocument()
	{
		return document;
	}

	public void save(String filename)
		throws TransformerConfigurationException, TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty("method", "xml");
		String encoding = document.getEncoding();
		if (encoding != null)
			serializer.setOutputProperty("encoding", encoding);
		serializer.transform(new DOMSource(getDocument()), new StreamResult(filename));
	}

	public void save()
		throws TransformerConfigurationException, TransformerException
	{
		save(source);
	}

	public Element getNextElementSibling(Element element)
	{
		// search the next element sibling (null is also allowed)
		Element nextElement = null;
		Node nextNode = element;
		while ((nextNode = nextNode.getNextSibling()) != null)
		{
			if (nextNode.getNodeType() == Node.ELEMENT_NODE)
			{
				nextElement = (Element)nextNode;
				break;
			}
		}
		return nextElement;
	}


	/**
	  Finds the namespace with which the prefix is associated, or null
	  if not found.

	  @param element Element from which to start searching
	 */
	public String findNamespaceForPrefix(Element element, String prefix)
	{
		if (prefix == null)
			return null;

		if (prefix.equals("xml"))
			return "http://www.w3.org/XML/1998/namespace";

		if (prefix.equals("xmlns"))
			return null; // xmlns is itself not bound to a namespace

		Element currentEl = element;
		String searchForAttr = "xmlns:" + prefix;

		do
		{
			String attrValue = currentEl.getAttribute(searchForAttr);
			if (attrValue != null && attrValue.length() > 0)
			{
				return attrValue;
			}

			if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
				currentEl = (Element)currentEl.getParentNode();
			else
				currentEl = null;
		}
		while (currentEl != null);

		return null;
	}


	/**
	  Finds a prefix declaration for the given namespace, or null if
	  not found.

	  @param element Element from which to start searching
	 */
	public String findPrefixForNamespace(Element element, String ns)
	{
		if (ns == null)
			return null;

		if (ns.equals("http://www.w3.org/XML/1998/namespace"))
			return "xml";

		Element currentEl = element;

		do
		{
			NamedNodeMap attrs = currentEl.getAttributes();

			for (int i = 0; i < attrs.getLength(); i++)
			{
				Attr attr = (Attr)attrs.item(i);
				if (attr.getValue().equals(ns) && attr.getPrefix() != null && attr.getPrefix().equals("xmlns") )
				{
					return attr.getLocalName();
				}
			}
			if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
				currentEl = (Element)currentEl.getParentNode();
			else
				currentEl = null;
		}
		while (currentEl != null);

		return null;
	}


	/**
	  Finds a default namespace declaration.
	 */
	public String findDefaultNamespace(Element element)
	{
		// Note: the prefix xmlns is not bound to any namespace URI
		Element currentEl = element;
		do
		{
			String xmlns = currentEl.getAttribute("xmlns");
			if (xmlns != null)
				return xmlns;

			if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
				currentEl = (Element)currentEl.getParentNode();
			else
				currentEl = null;
		}
		while (currentEl != null);

		return null;
	}

	public Element getNode(String xpath)
	{
		try
		{
			Element el =  (Element)XPathAPI.selectSingleNode(document.getDocumentElement(), xpath);
			if (el == null)
				System.out.println("xpath returned null: " + xpath);
			return el;
		}
		catch (Exception e)
		{
			System.out.println("error executing xpath: " + xpath);
			return null;
		}
	}

	public Undo getUndo()
	{
		return undo;
	}
}
