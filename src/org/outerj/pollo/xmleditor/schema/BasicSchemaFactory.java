package org.outerj.pollo.xmleditor.schema;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.WeakHashMap;
import java.util.HashMap;

/**
 * Factory for BasicSchema's.
 * This implementation caches its instances using a WeakHashMap.
 *
 * @author Bruno Dumon
 */
public class BasicSchemaFactory implements ISchemaFactory
{
	protected WeakHashMap schemaInstances = new WeakHashMap();
	protected org.apache.log4j.Category logcat =
		org.apache.log4j.Category.getInstance(BasicSchemaFactory.class.getName());

	public ISchema getSchema(HashMap initParams)
		throws PolloException
	{
		if (!schemaInstances.containsKey(initParams))
		{
			try
			{
				BasicSchema newSchema = new BasicSchema();
				newSchema.init(initParams);
				schemaInstances.put(initParams, newSchema);
			}
			catch (Exception e)
			{
				logcat.error("[BasicSchemaFactory] Could not create schema.", e);
				throw new PolloException("[BasicSchemaFactory] Could not create schema.", e);
			}
		}

		return (ISchema)schemaInstances.get(initParams);
	}
}