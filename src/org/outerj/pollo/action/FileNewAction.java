package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class FileNewAction extends AbstractAction
{
	public FileNewAction()
	{
		super("New...");
	}

	public void actionPerformed(ActionEvent e)
	{
		Pollo.getInstance().newFileWizard();
	}
}
