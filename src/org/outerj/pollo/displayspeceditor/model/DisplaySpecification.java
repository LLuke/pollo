package org.outerj.pollo.displayspeceditor.model;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Model of a display specification file (as used by the display specification editor, it is not
 * used by Pollo itself). It consists of a list of ElementSpecification's,
 * which in return contain a list of AttributeSpecification's. This class also contains
 * the functionality to load/store the display specifications. Undo is also supported.
 *
 * @author Bruno Dumon
 */
public class DisplaySpecification
{
    private ElementSpecifications elements;
    private Namespaces namespaces;
    private File file;
    private Stack undoables = new Stack();
    private UndoAction undoAction;

    public DisplaySpecification()
    {
        elements = new ElementSpecifications(this);
        namespaces = new Namespaces();
        undoAction = new UndoAction();
        undoAction.updateStatus();
    }

    public ElementSpecifications getElements()
    {
        return elements;
    }

    public Namespaces getNamespaces()
    {
        return namespaces;
    }

    public void load(File file) throws SAXException, IOException, ParserConfigurationException
    {
        DisplaySpecHandler displaySpecHandler = new DisplaySpecHandler();
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        SAXParser parser = parserFactory.newSAXParser();
        InputStream is = new FileInputStream(file);
        try
        {
            parser.parse(new InputSource(is), displaySpecHandler);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (Exception e)
            {
            }
        }
        this.file = file;
    }

    public void store() throws FileNotFoundException, SAXException
    {
        FileOutputStream fos = null;

        fos = new FileOutputStream(file);
        OutputFormat outputFormat = new OutputFormat();
        outputFormat.setIndenting(true);
        outputFormat.setIndent(2);

        XMLSerializer serializer = new XMLSerializer(fos, outputFormat);

        final String DISPLAY_SPEC_EL = "displayspec";
        final String ELEMENT_EL = "element";
        final String SHOWATTRS_EL = "showattributes";
        final String BACKCOLOR_EL = "background-color";
        final String NAMES_ATTR = "names";
        final String RED_ATTR = "red";
        final String GREEN_ATTR = "green";
        final String BLUE_ATTR = "blue";
        final String NAME_ATTR = "name";

        serializer.startDocument();

        // create attributes to declare namespaces
        AttributesImpl displaySpecAttrs = new AttributesImpl();
        Iterator prefixIt = getNamespaces().getPrefixIterator();
        while (prefixIt.hasNext())
        {
            String prefix = (String)prefixIt.next();
            String namespace = getNamespaces().getNamespace(prefix);
            displaySpecAttrs.addAttribute("", "xmlns:" + prefix, "xmlns:" + prefix, "CDATA", namespace);
        }

        serializer.startElement("", DISPLAY_SPEC_EL, DISPLAY_SPEC_EL, displaySpecAttrs);
        Iterator elementSpecificationsIt = getElements().iterator();
        while (elementSpecificationsIt.hasNext())
        {
            ElementSpecification elementSpecification = (ElementSpecification)elementSpecificationsIt.next();
            AttributesImpl elementAttrs = new AttributesImpl();
            elementAttrs.addAttribute("", NAME_ATTR, NAME_ATTR, "CDATA", elementSpecification.getElementPath());
            serializer.startElement("", ELEMENT_EL, ELEMENT_EL, elementAttrs);

            // generate background color element
            AttributesImpl backColorAttrs = new AttributesImpl();
            Color color = elementSpecification.getColor();
            backColorAttrs.addAttribute("", RED_ATTR, RED_ATTR, "CDATA", String.valueOf(color.getRed()));
            backColorAttrs.addAttribute("", GREEN_ATTR, GREEN_ATTR, "CDATA", String.valueOf(color.getGreen()));
            backColorAttrs.addAttribute("", BLUE_ATTR, BLUE_ATTR, "CDATA", String.valueOf(color.getBlue()));
            serializer.startElement("", BACKCOLOR_EL, BACKCOLOR_EL, backColorAttrs);
            serializer.endElement("", BACKCOLOR_EL, BACKCOLOR_EL);

            // generate showattributes element
            AttributesImpl showAttrs = new AttributesImpl();
            showAttrs.addAttribute("", NAMES_ATTR, NAMES_ATTR, "CDATA", elementSpecification.getAttributes().toCommaSeparatedString());
            serializer.startElement("", SHOWATTRS_EL, SHOWATTRS_EL, showAttrs);
            serializer.endElement("", SHOWATTRS_EL, SHOWATTRS_EL);

            serializer.endElement("", ELEMENT_EL, ELEMENT_EL);
        }
        serializer.endElement("", DISPLAY_SPEC_EL, DISPLAY_SPEC_EL);
        serializer.endDocument();

    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    /**
     * Simple SAX handler to parse an XML display specification file to a DisplaySpecification object model.
     */
    public class DisplaySpecHandler extends DefaultHandler
    {
        protected boolean inElement = false;
        protected ElementSpecification elementSpecification;
        protected NamespaceSupport nsSupport = new NamespaceSupport();

        public void startPrefixMapping(String prefix, String uri)
                throws SAXException
        {
            nsSupport.declarePrefix(prefix, uri);
            getNamespaces().addPrefix(prefix, uri);
        }

        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
                throws SAXException
        {
            nsSupport.pushContext();
            if (localName.equals("element"))
            {
                inElement = true;
                try
                {
                    elementSpecification = getElements().addElement(atts.getValue("name"));
                }
                catch (InvalidQNameException e)
                {
                    throw new SAXException(e.getMessage(), e);
                }
            }
            else if (localName.equals("background-color"))
            {
                if (!inElement) throw new SAXException("background-color element only allowed inside 'element' element.");
                int red = Integer.parseInt(atts.getValue("red"));
                int green = Integer.parseInt(atts.getValue("green"));
                int blue = Integer.parseInt(atts.getValue("blue"));
                elementSpecification.setColor(new Color(red, green, blue));
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
                        try
                        {
                            elementSpecification.getAttributes().addAttribute(attrName);
                        }
                        catch (InvalidQNameException e)
                        {
                            throw new SAXException(e.getMessage(), e);
                        }
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
            }
            nsSupport.popContext();
        }
    }

    public interface Undoable
    {
        public void undo();

        public String getDescription();
    }

    public void addUndoable(Undoable undoable)
    {
        undoables.push(undoable);
        undoAction.updateStatus();
    }

    public class UndoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            Undoable undoable = (Undoable)undoables.pop();
            undoable.undo();
            updateStatus();
        }

        public void updateStatus()
        {
            if (undoables.empty())
            {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
            else
            {
                setEnabled(true);
                Undoable undoable = (Undoable)undoables.peek();
                putValue(Action.NAME, "Undo " + undoable.getDescription());
            }
        }
    }

    public UndoAction getUndoAction()
    {
        return undoAction;
    }

}
