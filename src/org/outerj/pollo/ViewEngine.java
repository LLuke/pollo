package org.outerj.pollo;

import javax.swing.JPanel;
import org.outerj.pollo.xmleditor.model.XmlModel;

/**
  A view engine is a class implementing the view for a particular type of file, eg
  Cocoon sitemap files or ant files.
 */
public abstract class ViewEngine extends JPanel
{
	protected XmlModel xmlModel;

	public ViewEngine(XmlModel xmlModel)
	{
		this.xmlModel = xmlModel;
	}

	public XmlModel getXmlModel()
	{
		return xmlModel;
	}
}