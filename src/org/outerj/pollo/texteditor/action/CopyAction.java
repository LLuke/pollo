package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextEditor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class CopyAction extends AbstractAction
{
	protected XmlTextEditor xmlTextEditor;

	public CopyAction(XmlTextEditor xmlTextEditor)
	{
		super("Copy");
		this.xmlTextEditor = xmlTextEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlTextEditor.copy();
	}

}
