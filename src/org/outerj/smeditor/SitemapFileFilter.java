package org.outerj.smeditor;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class SitemapFileFilter extends FileFilter
{
	public boolean accept(File f)
	{
		if (f.isDirectory())
			return true;

		if (f.getName().endsWith(".xmap"))
			return true;
		else
			return false;
	}

	public String getDescription()
	{
		return "Cocoon sitemap files (*.xmap)";
	}
}
