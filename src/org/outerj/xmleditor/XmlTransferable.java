package org.outerj.xmleditor;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.io.StringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

public class XmlTransferable implements Transferable
{

	public static DataFlavor xmlFlavor;
	public static DataFlavor [] supportedFlavors;
	protected Document doc;

	static
	{
		try
		{
			xmlFlavor = new DataFlavor("application/x-dom");
			supportedFlavors = new DataFlavor[1];
			supportedFlavors[0] = new DataFlavor("application/x-dom");
		}
		catch (Exception e)
		{
			System.out.println("Error in XmlTransferable static initializer: " + e.toString());
		}
	}

	public XmlTransferable(Element element)
	{
		try
		{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			doc.appendChild(doc.importNode(element, true));
		}
		catch (Exception e)
		{
			System.out.println("Error in XmlTransferable(element): " + e.toString());
		}
	}

	public DataFlavor [] getTransferDataFlavors()
	{
		return supportedFlavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if (flavor.equals(xmlFlavor))
			return true;
		else
			return false;
	}

	public Object getTransferData(DataFlavor dataFlavor)
	{
		if (dataFlavor.equals(xmlFlavor))
		{
			return doc.getDocumentElement();
		}
		return null;
	}
}

