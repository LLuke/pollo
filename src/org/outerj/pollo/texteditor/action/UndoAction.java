package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextDocument;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class UndoAction extends AbstractAction
{
	protected XmlTextDocument xmlTextDocument;

	public UndoAction(XmlTextDocument xmlTextDocument)
	{
		super("Undo");
		this.xmlTextDocument = xmlTextDocument;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlTextDocument.undo();
	}

}
