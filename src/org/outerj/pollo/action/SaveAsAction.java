package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.dialog.ErrorDialog;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SaveAsAction extends AbstractAction
{
	private XmlModel xmlModel;
	protected PolloFrame polloFrame;

	public SaveAsAction(XmlModel model, PolloFrame polloFrame)
	{
		super("Save As...");
		this.xmlModel = xmlModel;
		this.polloFrame = polloFrame;
	}

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			xmlModel.saveAs(polloFrame);
		}
		catch (Exception e)
		{
			ErrorDialog errorDialog = new ErrorDialog(polloFrame, "Error saving document.", e);
			errorDialog.show();
		}
	}
}
