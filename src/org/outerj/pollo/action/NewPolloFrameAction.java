package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Create a new PolloFrame in which views (EditorPanel's) on XmlModels
 * can be created.
 */
public class NewPolloFrameAction extends AbstractAction
{
	public NewPolloFrameAction()
	{
		super("New frame");
	}

	public void actionPerformed(ActionEvent e)
	{
		PolloFrame polloFrame = new PolloFrame();
		Pollo.getInstance().manageFrame(polloFrame);
		polloFrame.show();
	}
}
