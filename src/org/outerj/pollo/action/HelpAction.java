package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.gui.ErrorDialog;
import org.outerj.pollo.gui.EmptyIcon;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;


public class HelpAction extends AbstractAction
{
	protected static HelpBroker helpBroker;
	protected PolloFrame polloFrame;

	public HelpAction(PolloFrame polloFrame)
	{
		super("Help", EmptyIcon.getInstance());
		this.polloFrame = polloFrame;
	}

	public void actionPerformed(ActionEvent e)
	{
		if (helpBroker == null)
		{
			HelpSet helpSet = null;
			try
			{
				URL hsURL = HelpSet.findHelpSet(null, "pollohelp/jhelpset.hs");
				helpSet = new HelpSet(null, hsURL);
			}
			catch (Exception ee)
			{
				ErrorDialog errorDialog = new ErrorDialog(polloFrame, "Could not load helpfile.", ee);
				errorDialog.show();
				return;
			}

			helpBroker = helpSet.createHelpBroker();
		}
		helpBroker.setDisplayed(true);
	}
}
