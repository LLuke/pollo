package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.dialog.ErrorDialog;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SaveAction extends AbstractAction
{
	protected XmlModel model;
	protected PolloFrame polloFrame;

	public SaveAction(XmlModel model, PolloFrame polloFrame)
	{
		super("Save");
		this.model = model;
		this.polloFrame = polloFrame;
	}

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			model.save(polloFrame);
		}
		catch (Exception e)
		{
			ErrorDialog errorDialog = new ErrorDialog(polloFrame, "Error saving document.", e);
			errorDialog.show();
		}
	}
}
