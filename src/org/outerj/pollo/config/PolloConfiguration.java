package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.exception.PolloConfigurationException;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;
import java.io.FileOutputStream;
import java.io.File;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class PolloConfiguration
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	protected LinkedList viewTypes = new LinkedList();
	protected LinkedList templates = new LinkedList();
	protected LinkedList xpathQueries = new LinkedList();

	protected String fileOpenDialogPath = null;
	protected String lookAndFeel = null;

	public static final String USER_CONF_FILE_NAME = ".pollorc";

	public static final String EL_POLLO = "pollo";
	public static final String EL_FILE_OPEN_DIALOG_PATH = "file-open-dialog-path";
	public static final String EL_LOOK_AND_FEEL = "look-and-feel";


	public void addViewType(ViewTypeConf viewType)
	{
		viewTypes.add(viewType);
	}

	public Collection getViewTypes()
	{
		return viewTypes;
	}

	public void addTemplate(TemplateConfItem template)
	{
		templates.add(template);
	}

	public void addXPathQuery(XPathQuery query)
	{
		xpathQueries.add(query);
	}

	public Collection getTemplates()
	{
		return templates;
	}

	public Collection getXPathQueries()
	{
		return xpathQueries;
	}

	public String getFileOpenDialogPath()
	{
		return fileOpenDialogPath;
	}

	public void setFileOpenDialogPath(String path)
	{
		fileOpenDialogPath = path;
	}

	public String getLookAndFeel()
	{
		return lookAndFeel;
	}

	public void setLookAndFeel(String lookAndFeel)
	{
		this.lookAndFeel = lookAndFeel;
	}

	public void store()
		throws PolloConfigurationException
	{
		File file = new File(System.getProperty("user.home"), USER_CONF_FILE_NAME);
		FileOutputStream fos = null;

		try
		{
			fos = new FileOutputStream(file);
			OutputFormat outputFormat = new OutputFormat();
			outputFormat.setIndenting(true);
			outputFormat.setIndent(2);

			XMLSerializer serializer = new XMLSerializer(fos, outputFormat);

			serializer.startDocument();
			serializer.startElement(null, EL_POLLO, EL_POLLO, new AttributesImpl());

			store(serializer);

			serializer.endElement(null, EL_POLLO, EL_POLLO);
			serializer.endDocument();
		}
		catch (Exception e)
		{
			String message = "Could not store user configuration.";
			logcat.error("PolloConfiguration: " + message);
			throw new PolloConfigurationException(message, e);
		}
		finally
		{
			if (fos != null) try { fos.close(); } catch (Exception e) {};
		}
	}

	public void store(ContentHandler handler)
		throws SAXException
	{
		if (fileOpenDialogPath != null)
		{
			handler.startElement(null, EL_FILE_OPEN_DIALOG_PATH, EL_FILE_OPEN_DIALOG_PATH, new AttributesImpl());
			handler.characters(fileOpenDialogPath.toCharArray(), 0, fileOpenDialogPath.length());
			handler.endElement(null, EL_FILE_OPEN_DIALOG_PATH, EL_FILE_OPEN_DIALOG_PATH);
		}

		if (lookAndFeel != null)
		{
			handler.startElement(null, EL_LOOK_AND_FEEL, EL_LOOK_AND_FEEL, new AttributesImpl());
			handler.characters(lookAndFeel.toCharArray(), 0, lookAndFeel.length());
			handler.endElement(null, EL_LOOK_AND_FEEL, EL_LOOK_AND_FEEL);
		}
	}
}
