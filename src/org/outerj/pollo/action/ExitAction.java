package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.gui.EmptyIcon;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction
{
	protected PolloFrame polloFrame;

	public ExitAction(PolloFrame polloFrame)
	{
		super("Exit", EmptyIcon.getInstance());
		this.polloFrame = polloFrame;
	}

	public void actionPerformed(ActionEvent e)
	{
		Pollo.getInstance().exit(polloFrame);
	}
}
