package org.outerj.pollo.config;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.outerj.pollo.xmleditor.exception.PolloConfigurationException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;

public class PolloConfiguration
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	protected ArrayList viewTypes = new ArrayList();
	protected ArrayList templates = new ArrayList();
	protected ArrayList xpathQueries = new ArrayList();

	protected String fileOpenDialogPath = null;
	protected String schemaOpenDialogPath = null;

	public static final String USER_CONF_FILE_NAME = ".pollorc";

	public static final String EL_POLLO = "pollo";
	public static final String EL_FILE_OPEN_DIALOG_PATH = "file-open-dialog-path";
	public static final String EL_SCHEMA_OPEN_DIALOG_PATH = "schema-open-dialog-path";


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

	public String getSchemaOpenDialogPath()
	{
		return schemaOpenDialogPath;
	}

	public void setSchemaOpenDialogPath(String path)
	{
		schemaOpenDialogPath = path;
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

		if (schemaOpenDialogPath != null)
		{
			handler.startElement(null, EL_SCHEMA_OPEN_DIALOG_PATH, EL_SCHEMA_OPEN_DIALOG_PATH, new AttributesImpl());
			handler.characters(fileOpenDialogPath.toCharArray(), 0, fileOpenDialogPath.length());
			handler.endElement(null, EL_SCHEMA_OPEN_DIALOG_PATH, EL_SCHEMA_OPEN_DIALOG_PATH);
		}

	}
}
