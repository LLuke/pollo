package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.SelectionListener;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RemoveAction extends AbstractAction implements SelectionListener
{
	protected XmlEditor xmlEditor;

	public RemoveAction(XmlEditor xmlEditor)
	{
		super("Delete");
		this.xmlEditor = xmlEditor;
		xmlEditor.getSelectionInfo().addListener(this);
	}

	public void actionPerformed(ActionEvent e)
	{
		Node node = xmlEditor.getSelectionInfo().getSelectedNode();
		Element parent = (Element)node.getParentNode();
		parent.removeChild(node);
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
