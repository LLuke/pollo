package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.SelectionListener;

import javax.swing.Action;
import java.awt.event.ActionEvent;

import org.w3c.dom.Node;

/**
 * A Swing Action that places the currently selected node on the clipboard
 * (an internal clipboard, not the operating system clipboard) and then
 * deletes the node.
 *
 * This action automatically enables/disables itself.
 *
 * @author Bruno Dumon
 */
public class CutAction extends RemoveAction
{
	public CutAction(XmlEditor xmlEditor)
	{
		super(xmlEditor);
		putValue(Action.NAME, "Cut");
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlEditor.putOnClipboard(xmlEditor.getSelectedNode());
		super.actionPerformed(e);
	}

}
