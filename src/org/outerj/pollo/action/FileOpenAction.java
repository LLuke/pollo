package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;

import org.outerj.pollo.util.ExtensionFileFilter;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;

public class FileOpenAction extends AbstractAction
{
	public FileOpenAction()
	{
		super("Open file...");
	}

	public void actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		FileFilter defaultFilter = chooser.getFileFilter();

		ExtensionFileFilter filter1 = new ExtensionFileFilter(".xml", "XML files (*.xml)");
		chooser.addChoosableFileFilter(filter1);
		ExtensionFileFilter filter2 = new ExtensionFileFilter(".xmap", "Cocoon Sitemap files (*.xmap)");
		chooser.addChoosableFileFilter(filter2);

		chooser.setFileFilter(defaultFilter);

		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			Pollo.getInstance().openFile(chooser.getSelectedFile());
		}
	}
}
