package org.outerj.pollo.config;

import org.apache.commons.digester.Digester;
import org.apache.avalon.framework.configuration.*;
import org.outerj.pollo.xmleditor.exception.PolloConfigurationException;

import java.io.File;
import java.awt.*;

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

    public static PolloConfiguration loadConfiguration()
        throws PolloConfigurationException
    {
        final PolloConfiguration polloConfiguration = new PolloConfiguration();

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

        // action plugins
        digester.addObjectCreate("pollo/viewtypes/viewtype/action-plugins/action-plugin",
                "org.outerj.pollo.config.ActionPluginConfItem");
        digester.addSetNext("pollo/viewtypes/viewtype/action-plugins/action-plugin", "addActionPlugin");

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
        finally
        {
            digester.clear();
        }

        //
        // load user configuration
        //

        File file = new File(System.getProperty("user.home"), PolloConfiguration.USER_CONF_FILE_NAME);
        if (file.exists())
        {
            try
            {
                Configuration config = new DefaultConfigurationBuilder().buildFromFile(file);

                String fileOpenDialogPath = config.getChild("file-open-dialog-path").getValue(null);
                if (fileOpenDialogPath != null)
                    polloConfiguration.setFileOpenDialogPath(fileOpenDialogPath);

                String schemaOpenDialogPath = config.getChild("schema-open-dialog-path").getValue(null);
                if (schemaOpenDialogPath != null)
                    polloConfiguration.setSchemaOpenDialogPath(schemaOpenDialogPath);

                polloConfiguration.setSplitPane1Pos(config.getChild("splitpane1-pos").getValueAsInteger(620));
                polloConfiguration.setSplitPane2Pos(config.getChild("splitpane2-pos").getValueAsInteger(370));
                polloConfiguration.setWindowHeight(config.getChild("window-height").getValueAsInteger(600));
                polloConfiguration.setWindowWidth(config.getChild("window-width").getValueAsInteger(800));

                Configuration[] recentFileConfs = config.getChild("recent-files").getChildren("recent-file");
                for (int i = 0; i < recentFileConfs.length; i++)
                    polloConfiguration.putRecentlyOpenedFile(recentFileConfs[i].getValue(""));

                Configuration[] recentXPathConfs = config.getChild("recent-xpaths").getChildren("recent-xpath");
                for (int i = 0; i < recentXPathConfs.length; i++)
                    polloConfiguration.putRecentlyUsedXPath(recentXPathConfs[i].getValue(""));

                Configuration[] recentSchemaConfs = config.getChild("recent-schemas").getChildren("recent-schema");
                for (int i = 0; i < recentSchemaConfs.length; i++)
                    polloConfiguration.putRecentlyUsedSchema(recentSchemaConfs[i].getValue(""));

                Configuration elementNameFontConf = config.getChild("element-name-font");
                polloConfiguration.setElementNameFontSize(elementNameFontConf.getAttributeAsInteger("size", 12));
                polloConfiguration.setElementNameFontStyle(elementNameFontConf.getAttributeAsInteger("style", 0));

                Configuration attributeNameFontConf = config.getChild("attribute-name-font");
                polloConfiguration.setAttributeNameFontSize(attributeNameFontConf.getAttributeAsInteger("size", 12));
                polloConfiguration.setAttributeNameFontStyle(attributeNameFontConf.getAttributeAsInteger("style", Font.ITALIC));

                Configuration attributeValueFontConf = config.getChild("attribute-value-font");
                polloConfiguration.setAttributeValueFontSize(attributeValueFontConf.getAttributeAsInteger("size", 12));
                polloConfiguration.setAttributeValueFontStyle(attributeValueFontConf.getAttributeAsInteger("style", 0));

                Configuration textFontConf = config.getChild("text-font");
                polloConfiguration.setTextFontSize(textFontConf.getAttributeAsInteger("size", 12));

                polloConfiguration.setTextAntialiasing(config.getChild("text-antialiasing").getValueAsBoolean(false));
            }
            catch (Exception e)
            {
                logcat.error("PolloConfiguration: exception parsing the user configuration file", e);
                throw new PolloConfigurationException("Exception parsing the user configuration file.", e);
            }
            finally
            {
                digester.clear();
            }

        }
        else
        {
            logcat.info("No user configuration file found at: " + file);
        }

        return polloConfiguration;
    }
}
