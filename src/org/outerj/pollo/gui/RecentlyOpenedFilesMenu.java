package org.outerj.pollo.gui;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.Pollo;
import org.outerj.pollo.EditorPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * A JMenu from which the user can select a recently opened file.
 *
 * @author Bruno Dumon
 */
public class RecentlyOpenedFilesMenu extends JMenu
{
	PolloFrame polloFrame;

	public RecentlyOpenedFilesMenu(PolloFrame polloFrame)
	{
		super ("Recent files");
		this.polloFrame = polloFrame;
	}

	public void setPopupMenuVisible(boolean visible)
	{
		if (visible)
		{
			removeAll();
			// create the items in the menu, one for each XmlModel
			List recentFiles = Pollo.getInstance().getConfiguration().getRecentlyOpenedFiles();
			for (int i = recentFiles.size() - 1; i >= 0; i--)
			{
				add(new OpenRecentFileAction((String)recentFiles.get(i)));
			}
		}
		super.setPopupMenuVisible(visible);
	}

	public class OpenRecentFileAction extends AbstractAction
	{
		String fullpath;

		public OpenRecentFileAction(String fullpath)
		{
			super(fullpath);
			this.fullpath = fullpath;
		}

		public void actionPerformed(ActionEvent e)
		{
			File file = new File(fullpath);
			if (!file.exists())
			{
				JOptionPane.showMessageDialog(polloFrame, "This file does not exist anymore:\n" + fullpath, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Pollo.getInstance().openFile(file, polloFrame);
		}
	}

}
