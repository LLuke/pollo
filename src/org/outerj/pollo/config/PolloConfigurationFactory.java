package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.exception.PolloConfigurationException;
import org.apache.commons.digester.Digester;
import java.io.File;

/**
 * Reads the pollo configuration file and builds the configuration object
 * model.
 *
 * @author Bruno Dumon.
 */
public class PolloConfigurationFactory
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	public static PolloConfiguration createConfiguration()
		throws PolloConfigurationException
	{
		PolloConfiguration polloConfiguration = new PolloConfiguration();

		Digester digester = new Digester();
		digester.push(polloConfiguration);

		// rules for viewtypes
		digester.addObjectCreate("pollo/viewtypes/viewtype", "org.outerj.pollo.config.ViewTypeConf");
		digester.addSetNext("pollo/viewtypes/viewtype", "addViewType");
		digester.addCallMethod("*/viewtype/class-name", "setClassName", 0);
		digester.addCallMethod("*/viewtype/name", "setName", 0);
		digester.addCallMethod("*/viewtype/description", "setDescription", 0);

		// rules common for schemas, display specifications, plugins, and templates
		digester.addCallMethod("*/factory-class", "setFactoryClass", 0);
		digester.addCallMethod("*/init-param", "addInitParam", 2);
		digester.addCallParam("*/init-param/param-name", 0);
		digester.addCallParam("*/init-param/param-value", 1);

		// schemas
		digester.addObjectCreate("pollo/viewtypes/viewtype/schemas/schema",
				"org.outerj.pollo.config.SchemaConfItem");
		digester.addSetNext("pollo/viewtypes/viewtype/schemas/schema", "addSchema");

		// display specifications
		digester.addObjectCreate("pollo/viewtypes/viewtype/display-specifications/display-specification",
				"org.outerj.pollo.config.DisplaySpecConfItem");
		digester.addSetNext("pollo/viewtypes/viewtype/display-specifications/display-specification", "addDisplaySpec");

		// attribute editor plugins
		digester.addObjectCreate("pollo/viewtypes/viewtype/attribute-editor-plugins/attribute-editor-plugin",
				"org.outerj.pollo.config.AttrEditorPluginConfItem");
		digester.addSetNext("pollo/viewtypes/viewtype/attribute-editor-plugins/attribute-editor-plugin", "addAttrEditorPlugin");
			
		// templates
		digester.addObjectCreate("pollo/templates/template", "org.outerj.pollo.config.TemplateConfItem");
		digester.addCallMethod("pollo/templates/template/description", "setDescription", 0);
		digester.addSetNext("pollo/templates/template", "addTemplate");

		// xpath queries
		digester.addObjectCreate("pollo/xpath-queries/query", "org.outerj.pollo.config.XPathQuery");
		digester.addCallMethod("pollo/xpath-queries/query/description", "setDescription", 0);
		digester.addCallMethod("pollo/xpath-queries/query/expression", "setExpression", 0);
		digester.addSetNext("pollo/xpath-queries/query", "addXPathQuery");

		try
		{
			digester.parse(PolloConfigurationFactory.class.getClassLoader()
					.getResourceAsStream("pollo_conf.xml"));
		}
		catch (Exception e)
		{
			logcat.error("PolloConfiguration: exception parsing the configuration file", e);
			throw new PolloConfigurationException("Exception parsing the configuration file.", e);
		}

		return polloConfiguration;
	}
}
