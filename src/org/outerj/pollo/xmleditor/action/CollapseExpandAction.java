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
 * @author Bruno Dumon
 */
public class CollapseExpandAction extends AbstractAction implements SelectionListener
{
	public static final int COLLAPSE     = 1;
	public static final int COLLAPSE_ALL = 2;
	public static final int EXPAND       = 3;
	public static final int EXPAND_ALL   = 4;

	protected int behaviour;
	protected XmlEditor xmlEditor;

	/**
	 * @param behaviour one of COLLAPSE, COLLAPSE_ALL, EXPAND, EXPAND_ALL
	 */
	public CollapseExpandAction(XmlEditor xmlEditor, int behaviour)
	{
		super(getDescription(behaviour));
		this.behaviour = behaviour;
		this.xmlEditor = xmlEditor;

		xmlEditor.getSelectionInfo().addListener(this);
		setEnabled(false);
	}

	private static String getDescription(int behaviour)
	{
		switch (behaviour)
		{
			case COLLAPSE:
				return "Collapse";
			case COLLAPSE_ALL:
				return "Collapse All";
			case EXPAND:
				return "Expand";
			case EXPAND_ALL:
				return "Expand All";
			default:
				throw new RuntimeException("[CollapseExpandAction] behaviour not supported: " + behaviour);
		}
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
