package org.outerj.xmleditor.model;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import org.outerj.xmleditor.XmlEditor;

public class UndoAction extends AbstractAction
{
	protected Undo undo;

	/**
	  Undo actions should be created through the Undo class.
	 */
	protected UndoAction(Undo undo)
	{
		super("Undo");
		this.undo = undo;
	}

	public void actionPerformed(ActionEvent e)
	{
		System.out.println("now undoing...");
		undo.undo();
	}
}
