package org.outerj.pollo.template;

import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;

import java.util.HashMap;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;

public class FileTemplate implements ITemplate
{
	protected String source;
	
	public void init(HashMap initParams)
		throws PolloException
	{
		source = (String)initParams.get("source");
		if (source == null)
		{
			throw new PolloException("[FileTemplate] No source init-param given!");
		}
	}

	public XmlModel createNewDocument()
		throws PolloException
	{
		XmlModel model = null;
		try
		{
			model = new XmlModel(new InputSource(this.getClass().getClassLoader().getResourceAsStream(source)));
		}
		catch (Exception e)
		{
			throw new PolloException("[FileTemplate] Could not create file based on template " + source + ".", e);
		}
		return model;
	}
}
