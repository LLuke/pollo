package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.DisplaySpecification.ElementSpec;
import org.outerj.pollo.xmleditor.util.DomUtils;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class InsertElementAction extends AbstractAction
{
	public static final int BEFORE  = 1;
	public static final int AFTER   = 2;
	public static final int ASCHILD = 3;

	protected XmlEditor xmlEditor;
	protected int behaviour;
	protected Element element;
	protected ElementSpec elementSpec;

	public InsertElementAction(XmlEditor xmlEditor, Element element, int behaviour, ElementSpec elementSpec)
	{
		super(elementSpec.localName, elementSpec.icon);
		this.xmlEditor = xmlEditor;
		this.behaviour = behaviour;
		this.element = element;
		this.elementSpec = elementSpec;
	}

	public void actionPerformed(ActionEvent e)
	{
		Element parent = (Element)element.getParentNode();

		// search for a namespace declaration so that we have a prefix
		String prefix = null;
		if ( (behaviour == BEFORE || behaviour == AFTER) && elementSpec.nsUri != null && elementSpec.nsUri.length() > 0)
		{
			prefix = xmlEditor.getXmlModel().findPrefixForNamespace(parent, elementSpec.nsUri);
		}
		else if (behaviour == ASCHILD && elementSpec.nsUri != null && elementSpec.nsUri.length() > 0)
		{
			prefix = xmlEditor.getXmlModel().findPrefixForNamespace(element, elementSpec.nsUri);
		}

		// if a prefix declaration wasn't found, abort
		if (elementSpec.nsUri != null && elementSpec.nsUri.length() > 0 && prefix == null)
		{
			JOptionPane.showMessageDialog(xmlEditor,
					"No namespace declaration found for namespace " + elementSpec.nsUri,
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// create the new element
		Element newElement = xmlEditor.getXmlModel().getDocument()
			.createElementNS(elementSpec.nsUri, DomUtils.getQName(prefix, elementSpec.localName));
		
		// insert it at the right position
		if (behaviour == BEFORE)
		{
			parent.insertBefore(newElement, element);
		}
		else if (behaviour == AFTER)
		{
			Element nextElement = xmlEditor.getXmlModel().getNextElementSibling(element);
			parent.insertBefore(newElement, nextElement);
		}
		else if (behaviour == ASCHILD)
		{
			element.appendChild(newElement);
		}
	}
}
