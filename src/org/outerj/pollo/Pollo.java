package org.outerj.pollo;

import org.outerj.pollo.action.NewPolloFrameAction;
import org.outerj.pollo.config.PolloConfiguration;
import org.outerj.pollo.config.PolloConfigurationFactory;
import org.outerj.pollo.config.TemplateConfItem;
import org.outerj.pollo.config.ViewTypeConf;
import org.outerj.pollo.gui.ErrorDialog;
import org.outerj.pollo.gui.ViewTypesDialog;
import org.outerj.pollo.template.ITemplate;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.XmlModelListener;
import org.outerj.pollo.plaf.PolloLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class Pollo implements XmlModelListener
{
	protected static Pollo  instance       = null;
	protected        Action newPolloFrameAction = null;

	protected PolloConfiguration configuration;
	protected ArrayList openFiles = new ArrayList();
	protected ArrayList openFrames = new ArrayList();

	public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
			org.outerj.pollo.xmleditor.AppenderDefinitions.MAIN);

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
		try
		{
			configuration = PolloConfigurationFactory.loadConfiguration();
		}
		catch (Exception e)
		{
			ErrorDialog errorDialog = new ErrorDialog(null, "Error while reading the configuration file.", e);
			errorDialog.show();
			System.exit(1);
		}

		// initialize actions
		newPolloFrameAction = new NewPolloFrameAction();

		UIManager.setLookAndFeel(new PolloLookAndFeel());

		// show a PolloFrame
		PolloFrame polloFrame = new PolloFrame();
		manageFrame(polloFrame);
		polloFrame.show();
	}

	/**
	 * Opens a file and creates an EditorPanel showing the file.
	 *
	 * @param file the file to open
	 * @param polloFrame the PolloFrame to which the created EditorPanel should
	 *                   be added
	 */
	public void openFile(final File file, final PolloFrame polloFrame)
	{
		// check if the file isn't open yet
		Iterator openFileIt = openFiles.iterator();
		while (openFileIt.hasNext())
		{
			File openFile = ((XmlModel)openFileIt.next()).getFile();
			if (openFile != null && openFile.getAbsolutePath().equals(file.getAbsolutePath()))
			{
				JOptionPane.showMessageDialog(polloFrame, "You have this file already open.", "Pollo", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}

		// create the XmlModel
		XmlModel xmlModel;

		try
		{
			xmlModel = new XmlModel();
			xmlModel.readFromResource(file);
		}
		catch (Exception e)
		{
			ErrorDialog errorDialog = new ErrorDialog(polloFrame, "Could not read this file.", e);
			errorDialog.show();
			return;
		}

		EditorPanel editorPanel = createEditorPanel(xmlModel, polloFrame);
		if (editorPanel != null)
		{
			xmlModel.addListener(this);
			openFiles.add(xmlModel);
			polloFrame.addEditorPanel(editorPanel);
			getConfiguration().addRecenltyOpenedFile(file.getAbsolutePath());
		}
	}

	/**
	 * Returns a list of XmlModel objects.
	 */
	public java.util.List getOpenFiles()
	{
		return openFiles;
	}

	public EditorPanel createEditorPanel(XmlModel xmlModel, PolloFrame polloFrame)
	{
		// let the user select the viewtype to create
		ViewTypesDialog viewTypesDialog = new ViewTypesDialog(polloFrame);
		if (viewTypesDialog.showDialog())
		{
			ViewTypeConf viewTypeConf = viewTypesDialog.getSelectedViewTypeConf();
			if (viewTypeConf == null)
				return null;
			EditorPanel editorPanel;
			try
			{
				editorPanel = new EditorPanelImpl(xmlModel, viewTypeConf, polloFrame);
			}
			catch (Exception e2)
			{
				ErrorDialog errorDialog = new ErrorDialog(polloFrame, "Could not create the view.", e2);
				errorDialog.show();
				return null;
			}
			xmlModel.registerView(editorPanel);
			return editorPanel;
		}
		else
			return null;
	}

	public void manageFrame(PolloFrame polloFrame)
	{
		this.openFrames.add(polloFrame);
		polloFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e)
			{
				Window window = e.getWindow();
				openFrames.remove(window);
				if (openFrames.isEmpty())
				{
					storeConfiguration();
					System.exit(0);
				}
			}
		});
	}

	public void exit(Frame parent)
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
				ok = xmlModel.closeAllViews(parent);
			}
			catch (Exception e)
			{
				ErrorDialog errorDialog = new ErrorDialog(parent, "An error occured.", e);
				errorDialog.show();
			}

			if (!ok)
			{
				// user selected cancel so don't quit
				return;
			}
		}
		while (true);

		storeConfiguration();
		System.exit(0);
	}

	protected void storeConfiguration()
	{
		try
		{
			configuration.store();
		}
		catch (Exception e)
		{
			ErrorDialog errorDialog = new ErrorDialog(null, "Could not store the user preferences.", e);
			errorDialog.show();
		}
	}

	/** Callback function from XmlModelListener. */
	public void lastViewClosed(XmlModel xmlModel)
	{
		openFiles.remove(xmlModel);
	}

	public void newFileWizard(PolloFrame polloFrame)
	{
		Object[] templates = configuration.getTemplates().toArray();
		Object selected = templates.length > 0 ? templates[0] : null;
		TemplateConfItem templateConfItem = (TemplateConfItem)JOptionPane.showInputDialog(polloFrame,
				"Choose a template", "New XML file", JOptionPane.INFORMATION_MESSAGE, null,
				templates, selected);

		if (templateConfItem != null)
		{
			XmlModel xmlModel = null;
			try
			{
				ITemplate template = templateConfItem.createTemplate();
				xmlModel = template.createNewDocument();
			}
			catch (Exception e)
			{
				ErrorDialog errorDialog = new ErrorDialog(polloFrame, "Error during template creation.", e);
				errorDialog.show();
				return;
			}

			EditorPanel editorPanel = createEditorPanel(xmlModel, polloFrame);
			if (editorPanel != null)
			{
				xmlModel.addListener(this);
				openFiles.add(xmlModel);
				polloFrame.addEditorPanel(editorPanel);
			}
		}

	}

	public PolloConfiguration getConfiguration()
	{
		return configuration;
	}


	// -------------------------------------------------------------------
	// factory methods
	// -------------------------------------------------------------------

	public static synchronized Pollo getInstance()
	{
		if (instance == null)
		{
			instance = new Pollo();
		}
		return instance;
	}

	protected Action getNewPolloFrameAction()
	{
		return newPolloFrameAction;
	}

	/** Implementation of the XmlModelListener interface. */
	public void fileNameChanged(XmlModel sourceXmlModel) {}

	/** Implementation of the XmlModelListener interface. */
	public void fileChanged(XmlModel sourceXmlModel) {}

	/** Implementation of the XmlModelListener interface. */
	public void fileSaved(XmlModel sourceXmlModel) {}

	/** Implementation of the XmlModelListener interface. */
	public void switchToTextMode(XmlModel sourceXmlModel) {}

	/** Implementation of the XmlModelListener interface. */
	public void switchToParsedMode(XmlModel sourceXmlModel) {}
}
