package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.dialog.AboutDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class AboutAction extends AbstractAction
{
	protected PolloFrame polloFrame;

	public AboutAction(PolloFrame polloFrame)
	{
		super("About Pollo...");
		this.polloFrame = polloFrame;
	}

	public void actionPerformed(ActionEvent e)
	{
		AboutDialog aboutdialog = new AboutDialog(polloFrame);
		aboutdialog.show();
	}
}
