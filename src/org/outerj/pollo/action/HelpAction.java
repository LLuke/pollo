package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.gui.EmptyIcon;
import org.outerj.pollo.util.ResourceManager;

import javax.help.HelpBroker;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class HelpAction extends AbstractAction
{
	protected static HelpBroker helpBroker;
	protected PolloFrame polloFrame;

	public HelpAction(PolloFrame polloFrame)
	{
		super("Help", EmptyIcon.getInstance());
		ResourceManager resMgr = ResourceManager.getManager(HelpAction.class);
        resMgr.configureAction(this);

		this.polloFrame = polloFrame;
	}



	public void actionPerformed(ActionEvent e)
	{
        JOptionPane.showMessageDialog(polloFrame, "Help is available from Pollo's website at http://pollo.sf.net");
	}
}
