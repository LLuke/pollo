package org.outerj.pollo.plugin;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

/**
 * Interface for factories of action plugins.
 */
public interface IActionPluginFactory
{
	public IActionPlugin getActionPlugin(HashMap initParams)
		throws PolloException;
}
