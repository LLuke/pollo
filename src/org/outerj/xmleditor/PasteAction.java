package org.outerj.xmleditor;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;

public class PasteAction extends AbstractAction
{
	public static final int PASTE_BEFORE  = 1;
	public static final int PASTE_AFTER   = 2;
	public static final int PASTE_ASCHILD = 3;

	protected XmlContentEditor xmlContentEditor;
	protected Element element;
	protected int behaviour;

	public PasteAction(XmlContentEditor xmlContentEditor, Element element, int behaviour)
	{
		super(getDisplayName(behaviour));

		this.xmlContentEditor = xmlContentEditor;
		this.element = element;
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
		Element newElement = xmlContentEditor.getClipboard();
		Element parent = (Element)element.getParentNode();
		if (newElement == null)
		{
			JOptionPane.showMessageDialog(xmlContentEditor, "Clipboard is empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		newElement = (Element)xmlContentEditor.getXmlModel().getDocument().importNode(newElement, true);

		if (behaviour == PASTE_BEFORE)
		{
			parent.insertBefore(newElement, element);
		}
		else if (behaviour == PASTE_AFTER)
		{
			Element nextElement = xmlContentEditor.getXmlModel().getNextElementSibling(element);
			parent.insertBefore(newElement, nextElement);
		}
		else if (behaviour == PASTE_ASCHILD)
		{
			element.appendChild(newElement);
		}
	}
}
