package org.outerj.xmleditor;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

import java.awt.Color;
import java.awt.Font;
import javax.swing.Icon;

import javax.xml.parsers.*;

public class DisplaySpecification
{
	protected HashMap elementSpecs = new HashMap();
	protected HashMap attributeSpecs = new HashMap();

	protected Color defaultColor;
	protected Font defaultElementFont;
	protected Font defaultAttributeNameFont;
	protected Font defaultAttributeValueFont;
	protected Color backgroundColor = new Color(235, 235, 235);

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

	protected DisplaySpecification(String location)
		throws Exception
	{
		defaultColor = new Color(255, 255, 255);
		defaultElementFont = new Font("Dialog", 0, 12);
		defaultAttributeNameFont = new Font("Dialog", Font.ITALIC, 12);
		defaultAttributeValueFont = new Font("Dialog", 0, 12);

		DisplaySpecHandler displaySpecHandler = new DisplaySpecHandler(this);

		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(new InputSource(location), displaySpecHandler);
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	public void addElementSpec(String uri, String localName, ElementSpec elementSpec)
	{
		elementSpecs.put(getHashString(uri, localName), elementSpec);
	}

	public void addAttributeSpec(String uri, String localName, AttributeSpec attributeSpec)
	{
		attributeSpecs.put(getHashString(uri, localName), attributeSpec);
	}

	public ElementSpec getElementSpec(String uri, String localName)
	{
		if (uri == null) uri = "";
		ElementSpec elementSpec = (ElementSpec)elementSpecs.get(getHashString(uri, localName));
		if (elementSpec == null)
		{
			elementSpec = new ElementSpec();
			elementSpec.nsUri = uri;
			elementSpec.localName = localName;
			elementSpec.attributesToShow = new ArrayList();
			elementSpec.backgroundColor = defaultColor;
			elementSpec.viewType = elementSpec.STRUCTURAL_VIEW;
			elementSpec.icon = new ElementColorIcon(defaultColor);
			elementSpec.font = defaultElementFont;
			addElementSpec(uri, localName, elementSpec);
		}
		return elementSpec;
	}
	
	public AttributeSpec getAttributeSpec(String uri, String localName)
	{
		AttributeSpec attributeSpec = (AttributeSpec)attributeSpecs.get(getHashString(uri, localName));
		if (attributeSpec == null)
		{
			attributeSpec = new AttributeSpec();
			attributeSpec.nsUri = uri;
			attributeSpec.localName = localName;
			attributeSpec.nameFont = defaultAttributeNameFont;
			addAttributeSpec(uri, localName, attributeSpec);
		}
		return attributeSpec;
	}

	public Font getDefaultAttributeNameFont()
	{
		return defaultAttributeNameFont;
	}

	public Font getDefaultAttributeValueFont()
	{
		return defaultAttributeValueFont;
	}

	public Font getDefaultElementFont()
	{
		return defaultElementFont;
	}


	public Collection getElementSpecs()
	{
		return elementSpecs.values();
	}


	private String getHashString(String uri, String localName)
	{
		if (uri == null) uri = "";
		StringBuffer fqn = new StringBuffer();
		fqn.append("{").append(uri).append("}").append(localName);
		return fqn.toString();
	}


	/**
	  Makes the list of attributesToShow in elementSpec
	  to point to other attributesspecs. (This can only be done
	  after the whole xml file has been read)
	 */
	public void postProcessElementSpecs()
	{
		Iterator elementSpecsIt = elementSpecs.values().iterator();
		while (elementSpecsIt.hasNext())
		{
			ElementSpec elementSpec = (ElementSpec)elementSpecsIt.next();

			// process attributesToShow
			int totalCount = elementSpec.attributesToShowTemp.size() / 2;
			elementSpec.attributesToShow = new ArrayList();
			for (int i = 0; i < totalCount; i++)
			{
				String uri = (String)elementSpec.attributesToShowTemp.get(2*i);
				String localName = (String)elementSpec.attributesToShowTemp.get((2*i)+1);
				elementSpec.attributesToShow.add(getAttributeSpec(uri, localName));
			}
			elementSpec.attributesToShowTemp = null;
		}
	}



	/*
	  Here comes the parser.
	 */
	public class DisplaySpecHandler extends DefaultHandler
	{
		protected DisplaySpecification ds;

		protected String elementName = null;
		protected String attributeName = null;
		protected String uri = null;
		protected String displayType = null;
		protected Font font = null;
		protected String fontName = null;
		protected boolean fontBold;
		protected int fontSize;
		protected Color color = null;
		protected int red;
		protected int green;
		protected int blue;
		protected boolean inElement = false;
		protected boolean inShowAttributes = false;
		protected boolean inAttr = false;
		protected boolean inAttribute = false;
		protected boolean inAllowedSubElements = false;
		protected boolean inEl = false;
		protected HashMap prefixMappings = new HashMap(); // should be a stack
		protected ArrayList attributes; // contains sequence uri/name/uri/name/...

		public DisplaySpecHandler(DisplaySpecification ds)
		{
			this.ds = ds;
		}

		
		
		public void startPrefixMapping(String prefix, String uri)
			throws SAXException
		{
			prefixMappings.put(prefix, uri);
		}

		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts)
			throws SAXException
		{
			if (localName.equals("element"))
			{
				inElement = true;
				elementName = atts.getValue("name");
				String [] uriAndLocalName = getUriAndLocalName(elementName);
				uri = uriAndLocalName[0];
				elementName = uriAndLocalName[1];
				attributes = new ArrayList();
			}
			if (localName.equals("attribute"))
			{
				inAttribute = true;
				attributeName = atts.getValue("name");
				String [] uriAndLocalName = getUriAndLocalName(attributeName);
				uri = uriAndLocalName[0];
			}
			else if (localName.equals("display"))
			{
				if (!inElement) throw new SAXException("display element only allowed inside 'element' element.");
				displayType = atts.getValue("type");
			}
			else if (localName.equals("name-font"))
			{
				if (!inElement) throw new SAXException("name-font element only allowed inside 'element' element.");
				fontName = atts.getValue("name");
				fontBold = Boolean.valueOf(atts.getValue("bold")).booleanValue();
				fontSize = Integer.parseInt(atts.getValue("size"));
				font = new Font(fontName, fontBold == true ? Font.BOLD : 0, fontSize);
			}
			else if (localName.equals("background-color"))
			{
				if (!inElement) throw new SAXException("background-color element only allowed inside 'element' element.");
				red   = Integer.parseInt(atts.getValue("red"));
				green = Integer.parseInt(atts.getValue("green"));
				blue  = Integer.parseInt(atts.getValue("blue"));
				color = new Color(red, green, blue);
			}
			else if (localName.equals("showattributes"))
			{
				if (!inElement) throw new SAXException("showattributes element only allowed inside 'element' element.");
				inShowAttributes = true;
			}
			else if (localName.equals("att"))
			{
				if (!inShowAttributes)
					throw new SAXException("att element only allowed inside 'showattributes' element.");
				inAttr = true;
			}
		}

		
		public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException
		{
			if (localName.equals("element"))
			{
				inElement = false;

				ElementSpec elementSpec = new ElementSpec();
				elementSpec.nsUri = uri;
				elementSpec.localName = elementName;
				if (displayType != null && displayType.equals("inline"))
					elementSpec.viewType = elementSpec.INLINE_VIEW;
				else
					elementSpec.viewType = elementSpec.STRUCTURAL_VIEW;
				elementSpec.backgroundColor = color;
				elementSpec.attributesToShowTemp = attributes;
				elementSpec.font = font != null ? font : ds.getDefaultElementFont();
				elementSpec.icon = new ElementColorIcon(color);
				//elementSpec.allowedAttributes = (String [])attributes.toArray(new String[0]);

				ds.addElementSpec(uri, elementName, elementSpec);
			}
			else if (localName.equals("attribute"))
			{
				inAttribute = false;
				AttributeSpec attributeSpec = new AttributeSpec();
				attributeSpec.nsUri = uri;
				attributeSpec.localName = attributeName;
				attributeSpec.nameFont = ds.getDefaultAttributeNameFont();
				ds.addAttributeSpec(uri, attributeName, attributeSpec);
			}
			else if (localName.equals("att"))
			{
				inAttr = false;
			}
			else if (localName.equals("showattributes"))
			{
				inShowAttributes = false;
			}
			else if (localName.equals("el"))
			{
				inEl = false;
			}
		}
		
		public void characters(char[] ch, int start, int length)
			throws SAXException
		{
			if (inAttr)
			{
				String [] uriAndLocalName = getUriAndLocalName(new String(ch, start, length));
				attributes.add(uriAndLocalName[0]);
				attributes.add(uriAndLocalName[1]);
			}
		}

		public void endDocument()
		{
			ds.postProcessElementSpecs();
		}

		public String [] getUriAndLocalName(String qname)
		{
			String uri = null;
			String localName = null;
			int pos = qname.indexOf(":");
			if (pos == -1)
			{
				uri = "";
				localName = qname;
			}
			else
			{
				String prefix = qname.substring(0, pos);
				uri = (String)prefixMappings.get(prefix);
				if (uri == null)
				{
					uri = "";
				}
				localName = qname.substring(pos + 1, qname.length());
			}
			return new String [] {uri, localName};
		}
		
	}


	public class ElementSpec
	{
		public String nsUri;
		public String localName;
		public Color backgroundColor;
		public ArrayList attributesToShow;
		public Font font;
		public Icon icon;
		public short viewType;

		public static final short STRUCTURAL_VIEW = 1;
		public static final short INLINE_VIEW = 2;

		ArrayList attributesToShowTemp;
		ArrayList allowedSubElementsTemp;
	}

	public class AttributeSpec
	{
		public String nsUri;
		public String localName;
		public Font nameFont;
	}

}
