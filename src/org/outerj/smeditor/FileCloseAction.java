package org.outerj.smeditor;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FileCloseAction extends AbstractAction
{
	protected SitemapEditor sitemapEditor;

	public FileCloseAction(SitemapEditor sitemapEditor, String name)
	{
		super(name);
		this.sitemapEditor = sitemapEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		sitemapEditor.closeCurrentFile();
	}
}
