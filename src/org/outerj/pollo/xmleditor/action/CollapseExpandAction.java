package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action that expands or collapses the currently selected node, possibly
 * recursively (aka "expand all" and "collapse all").
 *
 * The action enables/disables itself.
 *
 * @author Bruno Dumon, Curt Thompson (Toggle idea & code)
 */
public class CollapseExpandAction extends AbstractAction implements SelectionListener
{
	public static final int COLLAPSE     = 1;
	public static final int COLLAPSE_ALL = 2;
	public static final int EXPAND       = 3;
	public static final int EXPAND_ALL   = 4;
	public static final int TOGGLE       = 5;

	protected int behaviour;
	protected XmlEditor xmlEditor;

	/**
	 * @param behaviour one of COLLAPSE, COLLAPSE_ALL, EXPAND, EXPAND_ALL
	 */
	public CollapseExpandAction(XmlEditor xmlEditor, int behaviour)
	{
		this.behaviour = behaviour;
		this.xmlEditor = xmlEditor;

		xmlEditor.getSelectionInfo().addListener(this);
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent e)
	{
		View view = xmlEditor.getSelectionInfo().getSelectedNodeView();
		if (view.getNode().getNodeType() != Node.DOCUMENT_NODE)
		{
			switch (behaviour)
			{
				case COLLAPSE:
					view.collapse();
					break;
				case COLLAPSE_ALL:
					view.collapseAll();
					break;
				case EXPAND:
					view.expand();
					break;
				case EXPAND_ALL:
					view.expandAll();
					break;
				case TOGGLE:
					if (view.isCollapsed())
						view.expand();
					else
						view.collapse();
					break;
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
