package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction
{
	public ExitAction()
	{
		super("Exit");
	}

	public void actionPerformed(ActionEvent e)
	{
		Pollo.getInstance().exit();
	}
}
