package org.outerj.pollo.xmleditor.displayspec;

import java.awt.*;
import java.util.ArrayList;

/**
 * A wrapper class around a display specification that allows chaining it.
 * The way chaining works is that if the current instance returns null,
 * the next instance is consulted for a value. Note that the methods in
 * IDisplaySpecification are supposed to always return a value, so make
 * sure that on the end of the chain there's a display specification that
 * always return something for each method.
 *
 * @author Bruno Dumon
 */
public class ChainedDisplaySpecification implements IDisplaySpecification
{
	protected ArrayList displaySpecs = new ArrayList();

	public void add(IDisplaySpecification displaySpec)
	{
		displaySpecs.add(displaySpec);
	}

	public Color getBackgroundColor()
	{
		for (int i = 0; i < displaySpecs.size(); i++)
		{
			Color result = ((IDisplaySpecification)displaySpecs.get(i))
				.getBackgroundColor();
			if (result != null)
				return result;
		}
		return null;
	}

	public ElementSpec getElementSpec(String namespaceURI, String localName)
	{
		for (int i = 0; i < displaySpecs.size(); i++)
		{
			ElementSpec result = ((IDisplaySpecification)displaySpecs.get(i))
				.getElementSpec(namespaceURI, localName);
			if (result != null)
				return result;
		}
		return null;
	}

	public Font getAttributeNameFont()
	{
		for (int i = 0; i < displaySpecs.size(); i++)
		{
			Font result = ((IDisplaySpecification)displaySpecs.get(i)).getAttributeNameFont();
			if (result != null)
				return result;
		}
		return null;
	}

	public Font getAttributeValueFont()
	{
		for (int i = 0; i < displaySpecs.size(); i++)
		{
			Font result = ((IDisplaySpecification)displaySpecs.get(i)).getAttributeValueFont();
			if (result != null)
				return result;
		}
		return null;
	}

	public Font getElementNameFont()
	{
		for (int i = 0; i < displaySpecs.size(); i++)
		{
			Font result = ((IDisplaySpecification)displaySpecs.get(i)).getElementNameFont();
			if (result != null)
				return result;
		}
		return null;
	}
}
