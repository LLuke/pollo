package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextEditor;
import org.outerj.pollo.xmleditor.IconManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class PasteAction extends AbstractAction
{
	protected XmlTextEditor xmlTextEditor;

	public PasteAction(XmlTextEditor xmlTextEditor)
	{
		super("Paste", IconManager.getIcon("org/outerj/pollo/resource/stock_paste-16.png"));
		this.xmlTextEditor = xmlTextEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlTextEditor.paste();
	}

}
