package org.outerj.pollo.xmleditor.util;

public class DomUtils
{
	/**
	 * Given a prefix and local name, returns the qualified name.
	 * The prefix may be null, in which case the localName is returned as is.
	 */
	public static String getQName(String prefix, String localName)
	{
		if (prefix != null && prefix.length() != 0)
		{
			localName = prefix + ":" + localName;
		}
		return localName;
	}
}
