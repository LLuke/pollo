package org.outerj.pollo.xmleditor.displayspec;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Class that holds information about how to show an element.
 * ElementSpec's are managed by a DisplaySpecification.
 *
 * @author Bruno Dumon
 */
public class ElementSpec implements Comparable
{
	public String nsUri;
	public String localName;
	public Color backgroundColor;
	public ArrayList attributesToShow = new ArrayList();
	public Icon icon;
	public short viewType;

	public static final short BLOCK_VIEW = 1;
	public static final short INLINE_VIEW = 2;

	public int compareTo(Object o)
	{
		if (o instanceof ElementSpec)
			return localName.compareTo(((ElementSpec)o).localName);
		else
			throw new RuntimeException("Can only compare to ElementSpec's!");
	}
}
