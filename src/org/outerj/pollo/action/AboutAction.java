package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.gui.AboutDialog;
import org.outerj.pollo.gui.EmptyIcon;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class AboutAction extends AbstractAction
{
	protected PolloFrame polloFrame;

	public AboutAction(PolloFrame polloFrame)
	{
		super("About Pollo...", EmptyIcon.getInstance());
		this.polloFrame = polloFrame;
	}

	public void actionPerformed(ActionEvent e)
	{
		AboutDialog aboutdialog = new AboutDialog(polloFrame);
		aboutdialog.show();
	}
}
