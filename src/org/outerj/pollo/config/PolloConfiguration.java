package org.outerj.pollo.config;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;

public class PolloConfiguration
{
	LinkedList viewTypes = new LinkedList();
	LinkedList templates = new LinkedList();
	LinkedList xpathQueries = new LinkedList();

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

}
