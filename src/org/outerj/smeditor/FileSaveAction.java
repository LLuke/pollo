package org.outerj.smeditor;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FileSaveAction extends AbstractAction
{
	protected SitemapEditor sitemapEditor;

	public FileSaveAction(SitemapEditor sitemapEditor, String name)
	{
		super(name);
		this.sitemapEditor = sitemapEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		try
		{
			sitemapEditor.getXmlEditor().getXmlModel().save();
		}
		catch (Exception exc)
		{
			JOptionPane.showMessageDialog(sitemapEditor.getSitemapEditorFrame(), exc.toString(),
					"Exception occured", JOptionPane.ERROR_MESSAGE);
		}
	}
}
