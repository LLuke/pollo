package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.view.View;

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
		// delete the selected node and select the previous node
		Node node = xmlEditor.getSelectionInfo().getSelectedNode();

		// it is not allowed to delete the (visible) root node
		if (node != xmlEditor.getRootView().getNode())
		{
			View newSelectedView = xmlEditor.getSelectionInfo().getSelectedNodeView().getNextButNotChild();
			Node parent = node.getParentNode();
			parent.removeChild(node);

			if (newSelectedView != null)
			{
				int startV = newSelectedView.getVerticalPosition();
				int startH = newSelectedView.getHorizontalPosition();
				newSelectedView.markAsSelected(startH, startV);
				xmlEditor.scrollAlignTop(startV, newSelectedView.getHeight());
			}
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
