package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.util.NodeMap;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.StringTokenizer;

import java.awt.Color;
import java.awt.Font;
import javax.swing.Icon;

import javax.xml.parsers.*;

/**
 * The DisplaySpecification class provides information about how to show
 * element nodes (what colours and what attributes). This information is read
 * from an XML file. You can think of the DisplaySpecification as the
 * equivalent of CSS.
 *
 * There are two kinds of specs: {@link #org.outerj.pollo.xmleditor.DisplaySpecification.ElementSpec}'s
 * and {@link #org.outerj.pollo.xmleditor.DisplaySpecification.AttributeSpec}'s.
 *
 * @author Bruno Dumon
 */
public class DisplaySpecification
{
	/** Contains the instances of the ElementSpec class */
	protected NodeMap elementSpecs = new NodeMap();

	/** Contains the instances of the AttributeSpec class */
	protected NodeMap attributeSpecs = new NodeMap();

	/** Default color for elements. */
	protected Color defaultColor;
	/** Font to use for element names. */
	protected Font elementNameFont;
	/** Font to use for attribute names. */
	protected Font attributeNameFont;
	/** Font to use for attribute values. */
	protected Font attributeValueFont;
	/** Color to use as the background of the XmlEditor. */
	protected Color backgroundColor = new Color(235, 235, 235); // light grey

	/** Cache of the instances of this class. */
	protected static HashMap instances = new HashMap(2);

	public static DisplaySpecification getInstance(String location)
		throws Exception
	{
		DisplaySpecification displaySpec = (DisplaySpecification)instances.get(location);
		if (displaySpec == null)
		{
			displaySpec = new DisplaySpecification(location);
			instances.put(location, displaySpec);
		}
		return displaySpec;
	}

	/**
	 * Do not use the constructor directly, use {@link #getInstance(String location)}
	 * instead.
	 */
	protected DisplaySpecification(String location)
		throws Exception
	{
		defaultColor = new Color(255, 255, 255);

		// FIXME these should eventually also be specified in the XML file.
		elementNameFont = new Font("Default", 0, 12);
		attributeNameFont = new Font("Default", Font.ITALIC, 12);
		attributeValueFont = new Font("Default", 0, 12);

		// parse the XML file.
		DisplaySpecHandler displaySpecHandler = new DisplaySpecHandler();
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(new InputSource(location), displaySpecHandler);
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	public void addElementSpec(ElementSpec elementSpec)
	{
		elementSpecs.put(elementSpec.nsUri, elementSpec.localName, elementSpec);
	}

	public void addAttributeSpec(String uri, String localName, AttributeSpec attributeSpec)
	{
		attributeSpecs.put(uri, localName, attributeSpec);
	}

	public ElementSpec getElementSpec(String uri, String localName)
	{
		if (uri == null) uri = "";
		ElementSpec elementSpec = (ElementSpec)elementSpecs.get(uri, localName);
		if (elementSpec == null)
		{
			elementSpec = new ElementSpec();
			elementSpec.nsUri = uri;
			elementSpec.localName = localName;
			elementSpec.attributesToShow = new ArrayList();
			elementSpec.backgroundColor = defaultColor;
			elementSpec.viewType = elementSpec.BLOCK_VIEW;
			elementSpec.icon = new ElementColorIcon(defaultColor);
			addElementSpec(elementSpec);
		}
		return elementSpec;
	}
	
	public AttributeSpec getAttributeSpec(String uri, String localName)
	{
		AttributeSpec attributeSpec = (AttributeSpec)attributeSpecs.get(uri, localName);
		if (attributeSpec == null)
		{
			attributeSpec = new AttributeSpec();
			attributeSpec.nsUri = uri;
			attributeSpec.localName = localName;
			addAttributeSpec(uri, localName, attributeSpec);
		}
		return attributeSpec;
	}

	public Font getAttributeNameFont()
	{
		return attributeNameFont;
	}

	public Font getAttributeValueFont()
	{
		return attributeValueFont;
	}

	public Font getElementNameFont()
	{
		return elementNameFont;
	}


	public Collection getElementSpecs()
	{
		return elementSpecs.values();
	}



	/*
	  Here comes the parser.
	 */
	public class DisplaySpecHandler extends DefaultHandler
	{
		protected boolean inElement = false;
		protected ElementSpec elementSpec; // the elementSpec that we're currently constructing
		protected NamespaceSupport nsSupport = new NamespaceSupport();
		protected String[] nameParts = new String[3];

		
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
				elementSpec = new ElementSpec();
				nameParts = nsSupport.processName(atts.getValue("name"), nameParts, false);
				elementSpec.nsUri = nameParts[0];
				elementSpec.localName = nameParts[1];
			}
			else if (localName.equals("display"))
			{
				if (!inElement) throw new SAXException("display element only allowed inside 'element' element.");
				String displayType = atts.getValue("type");
				if (displayType != null && displayType.equals("inline"))
					elementSpec.viewType = elementSpec.INLINE_VIEW;
				else
					elementSpec.viewType = elementSpec.BLOCK_VIEW;
			}
			else if (localName.equals("background-color"))
			{
				if (!inElement) throw new SAXException("background-color element only allowed inside 'element' element.");
				int red   = Integer.parseInt(atts.getValue("red"));
				int green = Integer.parseInt(atts.getValue("green"));
				int blue  = Integer.parseInt(atts.getValue("blue"));
				elementSpec.backgroundColor = new Color(red, green, blue);
				elementSpec.icon = new ElementColorIcon(elementSpec.backgroundColor);
			}
			else if (localName.equals("showattributes"))
			{
				if (!inElement) throw new SAXException("showattributes element only allowed inside 'element' element.");
				String attrNames = atts.getValue("names");
				if (attrNames != null)
				{
					StringTokenizer tokenizer = new StringTokenizer(attrNames, ",");
					while (tokenizer.hasMoreTokens())
					{
						String attrName = tokenizer.nextToken();
						nameParts = nsSupport.processName(attrName, nameParts, false);
						AttributeSpec attrSpec = new AttributeSpec();
						attrSpec.nsUri = nameParts[0];
						attrSpec.localName = nameParts[1];
						elementSpec.attributesToShow.add(attrSpec);
					}
				}
			}
		}

		
		public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException
		{
			if (localName.equals("element"))
			{
				inElement = false;

				addElementSpec(elementSpec);
			}
			nsSupport.popContext();
		}
	}


	public class ElementSpec implements Comparable
	{
		public String nsUri;
		public String localName;
		public Color backgroundColor;
		public ArrayList attributesToShow = new ArrayList();
		public Icon icon;
		public short viewType;

		public static final short BLOCK_VIEW = 1;
		public static final short INLINE_VIEW = 2;

		public int compareTo(Object o)
		{
			if (o instanceof ElementSpec)
				return localName.compareTo(((ElementSpec)o).localName);
			else
				throw new RuntimeException("Can only compare to ElementSpec's!");
		}
	}

	public class AttributeSpec
	{
		public String nsUri;
		public String localName;
	}
}
