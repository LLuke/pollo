package org.outerj.pollo.xmleditor;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * Transferable for DOM fragments. Used for drag-and-drop.
 *
 * @author Bruno Dumon
 */
public class XmlTransferable implements Transferable
{

	public static DataFlavor xmlFlavor;
	public static DataFlavor [] supportedFlavors;
	protected DocumentFragment documentFragment;

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

	public XmlTransferable(DocumentFragment documentFragment)
	{
		try
		{
			this.documentFragment = documentFragment;
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
			return documentFragment;
		}
		return null;
	}
}

