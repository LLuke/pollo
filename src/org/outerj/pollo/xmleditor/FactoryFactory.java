package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.exception.PolloException;
import java.util.HashMap;

/**
 * A factory for factories. The purpose of this class is to manage the
 * instances of dynamically instantiated factory classes.
 *
 * @author Bruno Dumon
 */
public class FactoryFactory
{
	protected static HashMap factories = new HashMap();

	public static Object getInstance(String factoryClassName)
		throws PolloException
	{
		if (!factories.containsKey(factoryClassName))
		{
			try
			{
				Object newFactory = Class.forName(factoryClassName).newInstance();
				factories.put(factoryClassName, newFactory);
			}
			catch (Exception e)
			{
				throw new PolloException("Could not instantiate the factory " + factoryClassName, e);
			}
		}

		return factories.get(factoryClassName);
	}
}
