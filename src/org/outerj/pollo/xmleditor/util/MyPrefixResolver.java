package org.outerj.pollo.xmleditor.util;

import java.util.HashMap;

import org.apache.xml.utils.PrefixResolver;

/**
  * A prefixresolver for use with the xalan XPath api.
  *
  * @author Bruno Dumon
  */
public class MyPrefixResolver implements PrefixResolver
{
	protected HashMap prefixes;

	public void add(String prefix, String namespace)
	{
		if (prefixes == null)
		{
			prefixes = new HashMap();
		}
		prefixes.put(prefix, namespace);
	}

	public String getBaseIdentifier()
	{
		return null;
	}

	public String getNamespaceForPrefix(String prefix)
	{
		if (prefixes == null)
			return null;

		return (String)prefixes.get(prefix);
	}

	public String getNamespaceForPrefix(String prefix, org.w3c.dom.Node context)
	{
		return getNamespaceForPrefix(prefix);
	}
}
