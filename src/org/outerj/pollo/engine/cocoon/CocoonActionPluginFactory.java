package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.plugin.IActionPluginFactory;
import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

public class CocoonActionPluginFactory implements IActionPluginFactory
{
	public IActionPlugin getActionPlugin(HashMap initParams)
			throws PolloException
	{
		try
		{
			CocoonActionPlugin plugin = new CocoonActionPlugin();
			plugin.init(initParams);
			return plugin;
		}
		catch (Exception e)
		{
			throw new PolloException("[CocoonActionPluginFactory] Could not create plugin.", e);
		}
	}

}
