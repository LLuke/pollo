package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.exception.PolloException;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;

/**
 * Manages icon resources. Icons are loaded only once in memory, and the same
 * instance is always returned.
 *
 * @author Bruno Dumon
 */
public class IconManager
{
	protected static HashMap icons = new HashMap();

	public static Icon getIcon(String iconpath)
		throws PolloException
	{
		if (!icons.containsKey(iconpath))
		{
			try
			{
				URL imageUrl = IconManager.class.getClassLoader().getResource(iconpath);
				Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);
				Icon icon = new ImageIcon(image);
				icons.put(iconpath, icon);
			}
			catch (Exception e)
			{
				throw new PolloException("[IconManager] Could not load the icon " + iconpath, e);
			}
		}

		return (Icon)icons.get(iconpath);
	}
}
