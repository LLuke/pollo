package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.DocumentFragment;

public class PasteAction extends AbstractAction
{
	public static final int PASTE_BEFORE  = 1;
	public static final int PASTE_AFTER   = 2;
	public static final int PASTE_ASCHILD = 3;

	protected XmlEditor xmlEditor;
	protected int behaviour;

	public PasteAction(XmlEditor xmlEditor, int behaviour)
	{
		super(getDisplayName(behaviour));

		this.xmlEditor = xmlEditor;
		this.behaviour = behaviour;
	}

	protected static String getDisplayName(int behaviour)
	{
		if (behaviour == PASTE_BEFORE)
		{
			return "paste before";
		}
		else if (behaviour == PASTE_AFTER)
		{
			return "paste after";
		}
		else if (behaviour == PASTE_ASCHILD)
		{
			return "paste inside";
		}
		return "unkown?!";
	}

	public void actionPerformed(ActionEvent e)
	{
		DocumentFragment clipboard = xmlEditor.getClipboard();
		if (clipboard == null)
		{
			JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
					"Clipboard is empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Node newNode = clipboard.getFirstChild();
		Node selectedNode = xmlEditor.getSelectedNode();

		Element parent = (Element)selectedNode.getParentNode();

		newNode = xmlEditor.getXmlModel().getDocument().importNode(newNode, true);

		if (behaviour == PASTE_BEFORE)
		{
			parent.insertBefore(newNode, selectedNode);
		}
		else if (behaviour == PASTE_AFTER)
		{
			Node nextSibling = selectedNode.getNextSibling();
			if (nextSibling != null)
			{
				parent.insertBefore(newNode, nextSibling);
			}
			else
			{
				parent.appendChild(newNode);
			}
		}
		else if (behaviour == PASTE_ASCHILD)
		{
			selectedNode.appendChild(newNode);
		}
	}
}