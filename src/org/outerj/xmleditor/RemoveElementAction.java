package org.outerj.xmleditor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RemoveElementAction extends AbstractAction
{
	protected XmlContentEditor xmlContentEditor;
	protected Element element;

	public RemoveElementAction(XmlContentEditor xmlContentEditor, Element element)
	{
		super("Remove");
		this.xmlContentEditor = xmlContentEditor;
		this.element = element;
	}

	public void actionPerformed(ActionEvent e)
	{
		Element parent = (Element)element.getParentNode();
		parent.removeChild(element);
	}
}
