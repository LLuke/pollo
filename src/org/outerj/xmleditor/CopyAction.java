package org.outerj.xmleditor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;

public class CopyAction extends AbstractAction
{
	protected XmlContentEditor xmlContentEditor;
	protected Element element;

	public CopyAction(XmlContentEditor xmlContentEditor, Element element)
	{
		super("Copy");
		this.xmlContentEditor = xmlContentEditor;
		this.element = element;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlContentEditor.putOnClipboard(element);
	}
}
