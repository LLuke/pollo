package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextDocument;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class RedoAction extends AbstractAction
{
	protected XmlTextDocument xmlTextDocument;

	public RedoAction(XmlTextDocument xmlTextDocument)
	{
		super("Redo");
		this.xmlTextDocument = xmlTextDocument;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlTextDocument.redo();
	}

}
