package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.SelectionListener;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import org.w3c.dom.Node;

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
		xmlEditor.putOnClipboard(xmlEditor.getSelectedNode());
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
