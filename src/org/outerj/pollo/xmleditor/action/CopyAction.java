package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.IconManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * A Swing Action that places the currently selected node on the clipboard
 * (an internal clipboard, not the operating system clipboard).
 *
 * This action automatically enables/disables itself.
 *
 * @author Bruno Dumon
 */
public class CopyAction extends AbstractAction implements SelectionListener
{
	protected XmlEditor xmlEditor;

	public CopyAction(XmlEditor xmlEditor)
	{
		super("Copy");

		this.xmlEditor = xmlEditor;
		xmlEditor.getSelectionInfo().addListener(this);
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent e)
	{
		Node node = xmlEditor.getSelectedNode();
		if (!(node instanceof Document))
		{
			xmlEditor.putOnClipboard(xmlEditor.getSelectedNode());
		}
	}

	public void nodeUnselected(Node node)
	{
		setEnabled(false);
	}

	public void nodeSelected(Node node)
	{
		setEnabled(true);
	}
}
