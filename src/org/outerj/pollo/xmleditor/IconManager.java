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

	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.GUI);


	/**
	 * This method can return null, it will not throw an exception.
	 *
	 * FIXME: we should return a dummy icon if it could not be loaded.
	 */
	public static ImageIcon getIcon(String iconpath)
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
				logcat.error("[IconManager] Could not load the icon " + iconpath, e);
			}
		}

		return (ImageIcon)icons.get(iconpath);
	}
}
