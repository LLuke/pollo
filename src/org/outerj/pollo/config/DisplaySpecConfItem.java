package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecificationFactory;
import org.outerj.pollo.xmleditor.ComponentManager;
import org.outerj.pollo.xmleditor.exception.PolloException;

public class DisplaySpecConfItem extends ConfItem
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	public DisplaySpecConfItem()
	{
		logcat.debug("New DisplaySpecConfItem created");
	}

	public IDisplaySpecification createDisplaySpec()
		throws PolloException
	{
		IDisplaySpecification displaySpec = null;
		try
		{
			IDisplaySpecificationFactory displaySpecFactory = (IDisplaySpecificationFactory)
				ComponentManager.getFactoryInstance(getFactoryClass());

			displaySpec = displaySpecFactory.getDisplaySpecification(getInitParams());
		}
		catch (Exception e)
		{
			logcat.error("Exception creating display specification", e);
			throw new PolloException("[DisplaySpecConfItem] Error creating display specification.", e);
		}
		return displaySpec;
	}
}
