package org.outerj.pollo.xmleditor.displayspec;

import org.outerj.pollo.util.URLFactory;
import org.outerj.pollo.xmleditor.ElementColorIcon;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.util.NodeMap;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * An implementation of the IDisplaySpecification interface.
 * The displayspecification is read from an XML file, see the
 * files included with Pollo for examples.
 *
 * @author Bruno Dumon
 */
public class BasicDisplaySpecification implements IDisplaySpecification
{
	/** Contains the instances of the ElementSpec class */
	protected NodeMap elementSpecs = new NodeMap();

	/** Font to use for element names. */
	protected Font elementNameFont;
	/** Font to use for attribute names. */
	protected Font attributeNameFont;
	/** Font to use for attribute values. */
	protected Font attributeValueFont;
	/** Color to use as the background of the XmlEditor. */
	protected Color backgroundColor;


	protected void init(HashMap initParams)
		throws Exception
	{
		// FIXME read this values from the xml file
		/*
		elementNameFont = new Font("Default", 0, 12);
		attributeNameFont = new Font("Default", Font.ITALIC, 12);
		attributeValueFont = new Font("Default", 0, 12);
		*/

		String source = (String)initParams.get("source");
		if (source == null || source.trim().equals(""))
		{
			throw new PolloException("[BasicDisplaySpecification] The 'source' init-param is not specified!");
		}

		// parse the XML file.
		DisplaySpecHandler displaySpecHandler = new DisplaySpecHandler();
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		SAXParser parser = parserFactory.newSAXParser();
		InputStream is = URLFactory.createUrl(source).openStream();
		try
		{
			parser.parse(new InputSource(is), displaySpecHandler);
		}
		finally
		{
			try { is.close(); } catch (Exception e) {}
		}
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	public void addElementSpec(ElementSpec elementSpec)
	{
		elementSpecs.put(elementSpec.nsUri, elementSpec.localName, elementSpec);
	}

	public ElementSpec getElementSpec(String uri, String localName)
	{
		ElementSpec elementSpec = (ElementSpec)elementSpecs.get(uri, localName);
		return elementSpec;
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
						attrSpec.nsUri = nameParts[0].equals("") ? null : nameParts[0];
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

}
