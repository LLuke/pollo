package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Moves focus to the XmlEditor.
 *
 * Meant to be binded to the escape key.
 */
public class FocusOnEditorAction extends AbstractAction
{
	protected XmlEditor xmlEditor;

	public FocusOnEditorAction(XmlEditor xmlEditor)
	{
		this.xmlEditor = xmlEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		xmlEditor.requestFocus();
	}
}
