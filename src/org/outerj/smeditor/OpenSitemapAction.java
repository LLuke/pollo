package org.outerj.smeditor;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import org.xml.sax.InputSource;
import org.outerj.xmleditor.model.XmlModel;

public class OpenSitemapAction extends AbstractAction
{
	protected SitemapEditor sitemapEditor;

	public OpenSitemapAction(SitemapEditor sitemapEditor, String name)
	{
		super(name);
		this.sitemapEditor = sitemapEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		SitemapFileFilter filter = new SitemapFileFilter();
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(sitemapEditor.getSitemapEditorFrame());
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			sitemapEditor.setFile(chooser.getSelectedFile().getAbsolutePath());
		}
	}
}
