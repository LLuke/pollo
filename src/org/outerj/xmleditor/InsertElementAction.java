package org.outerj.xmleditor;

import org.outerj.xmleditor.DisplaySpecification.ElementSpec;

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

	protected XmlContentEditor xmlContentEditor;
	protected int behaviour;
	protected Element element;
	protected ElementSpec elementSpec;

	public InsertElementAction(XmlContentEditor xmlContentEditor, Element element, int behaviour, ElementSpec elementSpec)
	{
		super(elementSpec.localName, elementSpec.icon);
		this.xmlContentEditor = xmlContentEditor;
		this.behaviour = behaviour;
		this.element = element;
		this.elementSpec = elementSpec;
	}

	public void actionPerformed(ActionEvent e)
	{
		Element newElement = xmlContentEditor.getXmlModel().getDocument()
			.createElementNS(elementSpec.nsUri, elementSpec.localName);
		Element parent = (Element)element.getParentNode();
		
		// search for a namespace declaration so that we have a prefix
		String prefix = null;
		if ( (behaviour == BEFORE || behaviour == AFTER) && elementSpec.nsUri != null && elementSpec.nsUri.length() > 0)
		{
			prefix = xmlContentEditor.getXmlModel().findPrefixForNamespace(parent, elementSpec.nsUri);
		}
		else if (behaviour == ASCHILD && elementSpec.nsUri != null && elementSpec.nsUri.length() > 0)
		{
			prefix = xmlContentEditor.getXmlModel().findPrefixForNamespace(element, elementSpec.nsUri);
		}

		if (elementSpec.nsUri != null && elementSpec.nsUri.length() > 0 && prefix == null)
		{
			JOptionPane.showMessageDialog(xmlContentEditor,
					"No namespace declaration found for namespace " + elementSpec.nsUri,
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (prefix != null)
			newElement.setPrefix(prefix);


		if (behaviour == BEFORE)
		{
			parent.insertBefore(newElement, element);
		}
		else if (behaviour == AFTER)
		{
			Element nextElement = xmlContentEditor.getXmlModel().getNextElementSibling(element);
			parent.insertBefore(newElement, nextElement);
		}
		else if (behaviour == ASCHILD)
		{
			element.appendChild(newElement);
		}
	}
}
