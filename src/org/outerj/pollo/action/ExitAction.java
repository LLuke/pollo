package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction
{
	protected PolloFrame polloFrame;

	public ExitAction(PolloFrame polloFrame)
	{
		super("Exit");
		this.polloFrame = polloFrame;
	}

	public void actionPerformed(ActionEvent e)
	{
		Pollo.getInstance().exit(polloFrame);
	}
}
