package org.outerj.pollo.xmleditor.model;

import org.apache.xerces.parsers.DOMParser;

/**
 * A variant of the Xerces DOMParser which does not create text nodes
 * if the text only consists of whitespace.
 *
 * Normally the decission to ignore whitespace is based on the dtd or
 * schema, but for the purposes of Pollo this is more usefull.
 *
 * @author Bruno Dumon
 */
public class PolloDOMParser extends DOMParser
{
	public PolloDOMParser()
		throws Exception
	{
		super.setIncludeIgnorableWhitespace(false);
		super.setCreateEntityReferenceNodes(false);
	}



	public void characters(int data)
		throws Exception
	{
		// ignore text consisting of whitespace only
		if (getIncludeIgnorableWhitespace() || (fStringPool.toString(data).trim().length() > 0))
		{
			super.characters(data);
		}
	}
}
