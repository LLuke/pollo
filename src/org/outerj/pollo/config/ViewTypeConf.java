package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.displayspec.ChainedDisplaySpecification;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.schema.ChainedSchema;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.plugin.AttrEditorPluginChain;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.LinkedList;
import java.util.Iterator;

public class ViewTypeConf
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	protected String name;
	protected String description;
	protected String className;
	protected LinkedList schemas = new LinkedList();
	protected LinkedList displaySpecs = new LinkedList();
	protected LinkedList attrEditorPlugins = new LinkedList();

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		logcat.debug("ViewType.setName: " + name);
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		logcat.debug("ViewType.setDescription: " + description);
		this.description = description;
	}

	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		logcat.debug("ViewType.setClassName: " + className);
		this.className = className;
	}

	public void addSchema(SchemaConfItem schema)
	{
		schemas.add(schema);
	}

	public void addDisplaySpec(DisplaySpecConfItem displaySpec)
	{
		displaySpecs.add(displaySpec);
	}

	public void addAttrEditorPlugin(AttrEditorPluginConfItem attrEditorPlugin)
	{
		attrEditorPlugins.add(attrEditorPlugin);
	}

	public ISchema createSchemaChain()
		throws PolloException
	{
		ChainedSchema schemaChain = new ChainedSchema();

		Iterator it = schemas.iterator();
		while (it.hasNext())
		{
			SchemaConfItem conf = (SchemaConfItem)it.next();
			schemaChain.add(conf.createSchema());
		}

		return schemaChain;
	}

	public IDisplaySpecification createDisplaySpecChain()
		throws PolloException
	{
		ChainedDisplaySpecification displaySpecChain = new ChainedDisplaySpecification();

		Iterator it = displaySpecs.iterator();
		while (it.hasNext())
		{
			DisplaySpecConfItem conf = (DisplaySpecConfItem)it.next();
			displaySpecChain.add(conf.createDisplaySpec());
		}

		return displaySpecChain;
	}

	public IAttributeEditorPlugin createAttrEditorPluginChain(XmlModel xmlModel, ISchema schema)
		throws PolloException
	{
		AttrEditorPluginChain attrEditorPluginChain = new AttrEditorPluginChain();

		Iterator it = attrEditorPlugins.iterator();
		while (it.hasNext())
		{
			AttrEditorPluginConfItem conf = (AttrEditorPluginConfItem)it.next();
			attrEditorPluginChain.add(conf.createPlugin(xmlModel, schema));
		}

		return attrEditorPluginChain;
	}

	public String toString()
	{
		return description;
	}
}
