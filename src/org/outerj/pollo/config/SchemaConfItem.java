package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.schema.ISchemaFactory;
import org.outerj.pollo.xmleditor.FactoryFactory;
import org.outerj.pollo.xmleditor.exception.PolloException;

public class SchemaConfItem extends ConfItem
{
	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

	public SchemaConfItem()
	{
		logcat.debug("New SchemaConfItem created");
	}

	public ISchema createSchema()
		throws PolloException
	{
		ISchema schema = null;
		try
		{
			ISchemaFactory schemaFactory = (ISchemaFactory)FactoryFactory.getInstance(getFactoryClass());
			schema = schemaFactory.getSchema(getInitParams());
		}
		catch (Exception e)
		{
			logcat.error("Exception creating schema", e);
			throw new PolloException("[SchemaConfItem] Error creating schema.", e);
		}
		return schema;
	}
}
