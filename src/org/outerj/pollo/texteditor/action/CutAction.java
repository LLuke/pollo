package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextEditor;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class CutAction extends AbstractAction
{
	protected XmlTextEditor xmlTextEditor;

	public CutAction(XmlTextEditor xmlTextEditor)
	{
		super("Cut");
		this.xmlTextEditor = xmlTextEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlTextEditor.cut();
	}

}
