package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPluginFactory;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.FactoryFactory;

public class AttrEditorPluginConfItem extends ConfItem
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	public AttrEditorPluginConfItem()
	{
		logcat.debug("New AttrEditorPluginConfItem created");
	}

	public IAttributeEditorPlugin createPlugin(XmlModel xmlModel, ISchema schema)
		throws PolloException
	{
		IAttributeEditorPlugin plugin = null;
		try
		{
			IAttributeEditorPluginFactory pluginFactory =
				(IAttributeEditorPluginFactory)FactoryFactory.getInstance(getFactoryClass());
			plugin = pluginFactory.getInstance(getInitParams(), xmlModel, schema);
		}
		catch (Exception e)
		{
			logcat.error("[AttrEditorPluginConfItem] Error creating plugin", e);
			throw new PolloException("[AttrEditorPluginConfItem] Error creating plugin.", e);
		}
		return plugin;
	}
}
