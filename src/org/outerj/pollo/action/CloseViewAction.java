package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.EditorPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Closes a View (an EditorPanel actually) that is part of a PolloFrame.
 */
public class CloseViewAction extends AbstractAction
{
	protected PolloFrame polloFrame;
	protected EditorPanel editorPanel;

	public CloseViewAction(PolloFrame polloFrame, EditorPanel editorPanel)
	{
		super("Close view");
		this.polloFrame = polloFrame;
		this.editorPanel = editorPanel;
	}

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			if (editorPanel.close())
			{
				polloFrame.removeEditorPanel(editorPanel);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
