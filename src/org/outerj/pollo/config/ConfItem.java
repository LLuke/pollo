package org.outerj.pollo.config;

import java.util.HashMap;

public class ConfItem
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	protected String factoryClass;
	protected HashMap initParams = new HashMap();

	public void setFactoryClass(String factoryClass)
	{
		logcat.debug("ConfItem.setFactoryName: " + factoryClass);
		this.factoryClass = factoryClass;
	}

	public String getFactoryClass()
	{
		return factoryClass;
	}

	public void addInitParam(String name, String value)
	{
		logcat.debug("ConfItem.addInitParam: " + name + " = " + value);
		initParams.put(name, value);
	}

	public HashMap getInitParams()
	{
		return initParams;
	}
}
