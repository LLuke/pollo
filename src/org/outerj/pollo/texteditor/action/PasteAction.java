package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextEditor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class PasteAction extends AbstractAction
{
	protected XmlTextEditor xmlTextEditor;

	public PasteAction(XmlTextEditor xmlTextEditor)
	{
		super("Paste");
		this.xmlTextEditor = xmlTextEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlTextEditor.paste();
	}

}
