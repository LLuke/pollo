package org.outerj.pollo.xmleditor;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import java.awt.Color;

/**
 * A icon consisting of a colored square.
 *
 * @author Bruno Dumon
 */
public class ElementColorIcon implements Icon
{
	public static final int ICON_WIDTH = 10;
	public static final int ICON_HEIGHT = 10;

	protected Color color;

	public ElementColorIcon(Color color)
	{
		this.color = color;
	}

	public int getIconHeight()
	{
		return ICON_HEIGHT;
	}

	public int getIconWidth()
	{
		return ICON_WIDTH;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) 
	{
		g.setColor(color);
		g.fillRect(x, y, ICON_HEIGHT, ICON_WIDTH);
		g.setColor(Color.black);
		g.drawRect(x, y, ICON_HEIGHT, ICON_WIDTH);
	}
}
