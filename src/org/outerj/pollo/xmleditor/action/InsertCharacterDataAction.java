package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.DocumentFragment;

/**
  An action to insert comment, text or cdata nodes before, after
  or as child of the currently selected node.
 */
public class InsertCharacterDataAction extends AbstractAction
{
	public static final int INSERT_BEFORE  = 1;
	public static final int INSERT_AFTER   = 2;
	public static final int INSERT_INSIDE  = 3;

	public static final int TYPE_TEXT      = 4;
	public static final int TYPE_COMMENT   = 5;
	public static final int TYPE_CDATA     = 6;

	protected XmlEditor xmlEditor;
	protected int behaviour;
	protected int type;

	public InsertCharacterDataAction(XmlEditor xmlEditor, int behaviour, int type)
	{
		super(getDisplayName(behaviour));

		this.xmlEditor = xmlEditor;
		this.behaviour = behaviour;
		this.type      = type;
	}

	protected static String getDisplayName(int behaviour)
	{
		if (behaviour == INSERT_BEFORE)
		{
			return "before";
		}
		else if (behaviour == INSERT_AFTER)
		{
			return "after";
		}
		else if (behaviour == INSERT_INSIDE)
		{
			return "inside";
		}
		return "unkown?!";
	}

	public void actionPerformed(ActionEvent e)
	{
		Node selectedNode = xmlEditor.getSelectedNode();

		Element parent = (Element)selectedNode.getParentNode();

		Node newNode = null;
		switch (type)
		{
			case TYPE_TEXT:
				newNode = xmlEditor.getXmlModel().getDocument().
					createTextNode("I'm a new text node.");
				break;
			case TYPE_COMMENT:
				newNode = xmlEditor.getXmlModel().getDocument().
					createComment("I'm a new comment node.");
				break;
			case TYPE_CDATA:
				newNode = xmlEditor.getXmlModel().getDocument().
					createCDATASection("I'm a new CDATA section.");
				break;
		}


		if (behaviour == INSERT_BEFORE)
		{
			parent.insertBefore(newNode, selectedNode);
		}
		else if (behaviour == INSERT_AFTER)
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
		else if (behaviour == INSERT_INSIDE)
		{
			selectedNode.appendChild(newNode);
		}
	}
}
