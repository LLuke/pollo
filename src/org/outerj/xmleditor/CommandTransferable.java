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
import java.util.HashMap;


/**
  This class is used for drag-and-drop between the list of available elements
  and the XmlContentEditor. Instead of containing data to be dragged, it contains
  a 'command' (e.g. insert new element).
 */
public class CommandTransferable implements Transferable
{

	public static DataFlavor commandFlavor;
	public static DataFlavor [] supportedFlavors;

	protected HashMap commandInfo;

	static
	{
		try
		{
			commandFlavor = new DataFlavor("application/x-smgui-command");
			supportedFlavors = new DataFlavor[1];
			supportedFlavors[0] = new DataFlavor("application/x-smgui-command");
		}
		catch (Exception e)
		{
			System.out.println("Error in CommandTransferable static initializer: " + e.toString());
		}
	}

	public CommandTransferable(HashMap commandInfo)
	{
		this.commandInfo = commandInfo;
	}

	public DataFlavor [] getTransferDataFlavors()
	{
		return supportedFlavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if (flavor.equals(commandFlavor))
			return true;
		else
			return false;
	}

	public Object getTransferData(DataFlavor dataFlavor)
	{
		if (dataFlavor.equals(commandFlavor))
		{
			return commandInfo;
		}
		return null;
	}
}

