package org.outerj.pollo;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.XmlModelListener;

import org.outerj.pollo.dialog.WelcomeDialog;
import org.outerj.pollo.dialog.ErrorDialog;
import org.outerj.pollo.dialog.ViewTypesDialog;
import org.outerj.pollo.dialog.MessageWindow;

import org.outerj.pollo.action.FileOpenAction;
import org.outerj.pollo.action.FileNewAction;
import org.outerj.pollo.action.ExitAction;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import java.util.ArrayList;
import java.util.Properties;
import java.io.File;

public class Pollo implements XmlModelListener
{
	protected static Pollo  instance       = null;
	protected        Action fileOpenAction = null;
	protected        Action fileNewAction  = null;
	protected        Action exitAction     = null;

	protected Properties properties;
	protected ArrayList openFiles = new ArrayList();

	public static void main(String [] args)
		throws Exception
	{
		Pollo.getInstance().run();
	}

	/** Constructor is private, use the getInstance method instead. */
	private Pollo()
	{
	}

	public void run()
		throws Exception
	{
		UIManager.setLookAndFeel(new com.incors.plaf.kunststoff.KunststoffLookAndFeel());
		WelcomeDialog welcomeDialog = new WelcomeDialog();
		welcomeDialog.show();
	}

	public void openFile(final File file)
	{
		XmlModel xmlModel;

		//final MessageWindow parsingMessage = new MessageWindow("Parsing the file, please wait...");
		//parsingMessage.show();

		try
		{
			xmlModel = new XmlModel(file);
		}
		catch (Exception e)
		{
			//parsingMessage.hide();
			ErrorDialog errorDialog = new ErrorDialog(null, "Could not read or parse this file.", e);
			errorDialog.show();
			return;
		}

		//parsingMessage.hide();

		if (createView(xmlModel))
		{
			xmlModel.addListener(this);
			openFiles.add(xmlModel);
		}
	}

	public boolean createView(XmlModel xmlModel)
	{
		// let the user select the viewtype to create
		ViewTypesDialog viewTypesDialog = new ViewTypesDialog();
		if (viewTypesDialog.showDialog())
		{
			String engineName = viewTypesDialog.getSelectedViewTypeName();
			if (engineName == null)
				return false;
			ViewFrame viewFrame;
			try
			{
				viewFrame = ViewFactory.createViewFrame(xmlModel, engineName);
			}
			catch (Exception e2)
			{
				ErrorDialog errorDialog = new ErrorDialog(null, "Could not create the view.", e2);
				errorDialog.show();
				return false;
			}
			xmlModel.registerView(viewFrame);
			viewFrame.show();
			return true;
		}
		else
			return false;
	}

	public void exit()
	{
		// note: since the openFiles list is changed in the loop,
		// it is not possible to use an iterator.
		do
		{
			XmlModel xmlModel;
			try
			{
				xmlModel = (XmlModel)openFiles.get(0);
			}
			catch (IndexOutOfBoundsException e)
			{
				break;
			}

			boolean ok = false;
			try
			{
				ok = xmlModel.closeAllViews(); 
			}
			catch (Exception e)
			{
				ErrorDialog errorDialog = new ErrorDialog(null, "An error occured.", e);
				errorDialog.show();
			}

			if (!ok)
			{
				// user selected cancel so don't quit
				return;
			}
		}
		while (true);

		System.exit(0);
	}

	/** Callback function from XmlModelListener. */
	public void lastViewClosed(XmlModel xmlModel)
	{
		openFiles.remove(xmlModel);
	}

	public void newFileWizard()
	{
		JOptionPane.showMessageDialog(null, "Not yet implemented");
	}

	public String getProperty(String name)
	{
		return properties.getProperty(name);
	}

	public void loadProperties()
	{
		// read default properties
		Properties defaultProperties = new Properties();
		try
		{
			defaultProperties.load(instance.getClass().
					getResource("/org/outerj/pollo/pollo.properties").openStream());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.properties = new Properties(defaultProperties);

		// FIXME load user properties
	}


	// -------------------------------------------------------------------
	// factory methods
	// -------------------------------------------------------------------

	public static synchronized Pollo getInstance()
	{
		if (instance == null)
		{
			instance = new Pollo();
			instance.loadProperties();
		}
		return instance;
	}

	public Action getFileOpenAction()
	{
		if (fileOpenAction == null)
		{
			fileOpenAction = new FileOpenAction();
		}
		return fileOpenAction;
	}

	public Action getFileNewAction()
	{
		if (fileNewAction == null)
		{
			fileNewAction = new FileNewAction();
		}
		return fileNewAction;
	}

	public Action getExitAction()
	{
		if (exitAction == null)
		{
			exitAction = new ExitAction();
		}
		return exitAction;
	}
}
