package org.outerj.pollo.config;

import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.plugin.IActionPluginFactory;
import org.outerj.pollo.xmleditor.ComponentManager;
import org.outerj.pollo.xmleditor.exception.PolloException;

public class ActionPluginConfItem extends ConfItem
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	public ActionPluginConfItem()
	{
	}

	public IActionPlugin createActionPlugin()
		throws PolloException
	{
		IActionPlugin actionPlugin = null;
		try
		{
			IActionPluginFactory actionPluginFactory = (IActionPluginFactory)ComponentManager.getFactoryInstance(getFactoryClass());
			actionPlugin = actionPluginFactory.getActionPlugin(getInitParams());
		}
		catch (Exception e)
		{
			logcat.error("Exception creating ActionPlugin", e);
			throw new PolloException("[ActionPluginConfItem] Error creating action plugins.", e);
		}
		return actionPlugin;
	}
}
