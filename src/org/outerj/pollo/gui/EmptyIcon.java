package org.outerj.pollo.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Invisible icon, size 16 x 16. Used to align menu items.
 */
public class EmptyIcon implements Icon
{
	protected static final Icon instance = new EmptyIcon();

	public static Icon getInstance()
	{
		return instance;
	}

	public void paintIcon(Component c, Graphics g, int x, int y)
	{
	}

	public int getIconWidth()
	{
		return 16;
	}

	public int getIconHeight()
	{
		return 16;
	}
}
