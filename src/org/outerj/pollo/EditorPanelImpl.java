package org.outerj.pollo;

import org.outerj.pollo.action.CloseAction;
import org.outerj.pollo.action.SaveAction;
import org.outerj.pollo.action.SaveAsAction;
import org.outerj.pollo.config.ViewTypeConf;
import org.outerj.pollo.dialog.ErrorDialog;
import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.texteditor.XmlTextEditorPanel;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlEditorPanel;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.model.View;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.XmlModelListener;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.xml.sax.SAXParseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class EditorPanelImpl extends EditorPanel implements View, ChangeListener,
		XmlModelListener
{
	protected XmlModel xmlModel;
	protected PolloFrame polloFrame;
	protected XmlEditorPanel xmlEditorPanel;
	protected XmlTextEditorPanel xmlTextEditorPanel;
	protected JTabbedPane modeSwitchPane;
	protected ArrayList listeners = new ArrayList();
	protected String title;
	protected JMenuBar domModeMenuBar;
	protected JMenuBar textModeMenuBar;
	protected boolean ignoreModeChange = false;
	protected IActionPlugin actionPlugin;
	protected SaveAction saveAction;
	protected SaveAsAction saveAsAction;
	protected CloseAction closeAction;

	public EditorPanelImpl(XmlModel xmlModel, ViewTypeConf viewTypeConf, PolloFrame polloFrame)
		throws Exception
	{
		this.xmlModel = xmlModel;
		this.polloFrame = polloFrame;

		IDisplaySpecification idisplayspecification = viewTypeConf.createDisplaySpecChain();
		ISchema ischema = viewTypeConf.createSchemaChain();
		IAttributeEditorPlugin iattributeeditorplugin = viewTypeConf.createAttrEditorPluginChain(xmlModel, ischema);
		actionPlugin = viewTypeConf.createActionPlugins();

		xmlEditorPanel = new XmlEditorPanel(xmlModel, null, idisplayspecification, ischema, iattributeeditorplugin);
		xmlTextEditorPanel = new XmlTextEditorPanel(xmlModel, ischema);


		modeSwitchPane = new JTabbedPane();
		modeSwitchPane.add("Tree view", xmlEditorPanel);
		modeSwitchPane.add("Text view", xmlTextEditorPanel);
		modeSwitchPane.addChangeListener(this);

		// no borders
		modeSwitchPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		xmlTextEditorPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		xmlEditorPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setBorder(new EmptyBorder(0, 0, 0, 0));

		// switch to the right tab
		if (xmlModel.isInTextMode())
			modeSwitchPane.setSelectedComponent(xmlTextEditorPanel);

		// initialize actions
		saveAction = new SaveAction(xmlModel, polloFrame);
		if (!xmlModel.isModified())
			saveAction.setEnabled(false);
		saveAsAction = new SaveAsAction(xmlModel, polloFrame);
		closeAction = new CloseAction(xmlModel, polloFrame);

		// add the component to the panel
		this.setLayout(new BorderLayout());
		add(modeSwitchPane, BorderLayout.CENTER);
		xmlModel.addListener(this);

		title = xmlModel.getFileName();

		createMenus();
	}

	protected void createMenus()
	{
		domModeMenuBar = new JMenuBar();
		textModeMenuBar = new JMenuBar();

		// create the file menu
		JMenu domFileMenu = new JMenu("File");
		JMenu textFileMenu = new JMenu("File");
		domFileMenu.add(polloFrame.getFileNewAction());
		textFileMenu.add(polloFrame.getFileNewAction());
		domFileMenu.add(polloFrame.getFileOpenAction());
		textFileMenu.add(polloFrame.getFileOpenAction());
		domFileMenu.add(saveAction);
		textFileMenu.add(saveAction);
		domFileMenu.add(saveAsAction);
		textFileMenu.add(saveAsAction);
		domFileMenu.add(closeAction);
		textFileMenu.add(closeAction);
		domFileMenu.add(polloFrame.getExitAction());
		textFileMenu.add(polloFrame.getExitAction());

		domModeMenuBar.add(domFileMenu);
		textModeMenuBar.add(textFileMenu);

		// create edit menu for the dom menu bar
		JMenu domEditMenu = new JMenu("Edit");
		JMenuItem domUndo = new JMenuItem(xmlModel.getUndo().getUndoAction());
		domEditMenu.add(domUndo);
		domEditMenu.addSeparator();

		XmlEditor xmleditor = xmlEditorPanel.getXmlEditor();
		domEditMenu.add(new JMenuItem(xmleditor.getCopyAction()));
		domEditMenu.add(new JMenuItem(xmleditor.getCutAction()));
		JMenu domEditPasteMenu = new JMenu("Paste");
		domEditPasteMenu.add(new JMenuItem(xmleditor.getPasteBeforeAction()));
		domEditPasteMenu.add(new JMenuItem(xmleditor.getPasteAfterAction()));
		domEditPasteMenu.add(xmleditor.getPasteInsideAction());
		domEditMenu.add(domEditPasteMenu);

		domEditMenu.addSeparator();
		domEditMenu.add(xmleditor.getCommentOutAction());
		domEditMenu.add(xmleditor.getUncommentAction());
		domModeMenuBar.add(domEditMenu);

		// Insert menu for the dom menu bar
		JMenu domInsertMenu = new JMenu("Insert");
		JMenu domTextMenu = new JMenu("Text Node");
		domTextMenu.add(xmleditor.getInsertTextBeforeAction());
		domTextMenu.add(xmleditor.getInsertTextAfterAction());
		domTextMenu.add(xmleditor.getInsertTextInsideAction());
		domInsertMenu.add(domTextMenu);
		JMenu domCommentMenu = new JMenu("Comment Node");
		domCommentMenu.add(xmleditor.getInsertCommentBeforeAction());
		domCommentMenu.add(xmleditor.getInsertCommentAfterAction());
		domCommentMenu.add(xmleditor.getInsertCommentInsideAction());
		domInsertMenu.add(domCommentMenu);
		JMenu domCDataMenu = new JMenu("CDATA section");
		domCDataMenu.add(xmleditor.getInsertCDataBeforeAction());
		domCDataMenu.add(xmleditor.getInsertCDataAfterAction());
		domCDataMenu.add(xmleditor.getInsertCDataInsideAction());
		domInsertMenu.add(domCDataMenu);
		JMenu domPiMenu = new JMenu("Processing Instruction");
		domPiMenu.add(xmleditor.getInsertPIBeforeAction());
		domPiMenu.add(xmleditor.getInsertPIAfterAction());
		domPiMenu.add(xmleditor.getInsertPIInsideAction());
		domInsertMenu.add(domPiMenu);
		domModeMenuBar.add(domInsertMenu);

		// tree menu for the dom menu bar
		JMenu domTreeMenu = new JMenu("Tree");
		domTreeMenu.add(xmleditor.getCollapseAction());
		domTreeMenu.add(xmleditor.getExpandAction());
		domTreeMenu.addSeparator();
		domTreeMenu.add(xmleditor.getCollapseAllAction());
		domTreeMenu.add(xmleditor.getExpandAllAction());
		domModeMenuBar.add(domTreeMenu);

		// schema menu for the dom menu bar
		JMenu schemaMenu = new JMenu("Schema");
		schemaMenu.add(xmlEditorPanel.getValidateAction());
		schemaMenu.add(new org.outerj.pollo.xmleditor.action.DisplayContentModelAction(xmleditor));
		domModeMenuBar.add(schemaMenu);

		// edit menu for the text menu bar
		JMenu editMenu = new JMenu("Edit");
		editMenu.add(xmlTextEditorPanel.getDocument().getUndoAction());
		editMenu.add(xmlTextEditorPanel.getDocument().getRedoAction());
		editMenu.addSeparator();
		editMenu.add(xmlTextEditorPanel.getEditor().getCutAction());
		editMenu.add(xmlTextEditorPanel.getEditor().getCopyAction());
		editMenu.add(xmlTextEditorPanel.getEditor().getPasteAction());
		textModeMenuBar.add(editMenu);

		// create the action plugin menu (for dom mode)
		JMenu actionPluginMenu = new ActionPluginMenu("Plugin actions");
		domModeMenuBar.add(actionPluginMenu);

		// view menu for dom and text menu bar
		JMenu domViewMenu = new JMenu("View");
		JMenu textViewMenu = new JMenu("View");
		domViewMenu.add(Pollo.getInstance().getNewPolloFrameAction());
		textViewMenu.add(Pollo.getInstance().getNewPolloFrameAction());
		domViewMenu.add(new NewEditorPanelMenu(polloFrame));
		textViewMenu.add(new NewEditorPanelMenu(polloFrame));
		domModeMenuBar.add(domViewMenu);
		textModeMenuBar.add(textViewMenu);

		// help menu for the dom and text mode menu bar
		domModeMenuBar.add(Box.createHorizontalGlue());
		textModeMenuBar.add(Box.createHorizontalGlue());

		JMenu domHelpMenu = new JMenu("Help");
		JMenu textHelpMenu = new JMenu("Help");

		domHelpMenu.add(polloFrame.getHelpAction());
		textHelpMenu.add(polloFrame.getHelpAction());
		domHelpMenu.add(polloFrame.getAboutAction());
		textHelpMenu.add(polloFrame.getAboutAction());

		domModeMenuBar.add(domHelpMenu);
		textModeMenuBar.add(textHelpMenu);

	}

	public XmlEditorPanel getXmlEditorPanel()
	{
		return xmlEditorPanel;
	}

	public JMenuBar getMenuBar()
	{
		if (xmlModel.isInParsedMode())
			return domModeMenuBar;
		else if (xmlModel.isInTextMode())
			return textModeMenuBar;
		else
			throw new Error("[EditorPanelImpl] XmlModel is neither in parsed nor in text mode.");
	}

	public XmlModel getXmlModel()
	{
		return xmlModel;
	}

	/**
	 * Stop is called by the XmlModel when it wants to close all
	 * views on an XmlModel.
	 */
	public void stop()
	{
		fireClosingEvent();
		xmlEditorPanel.disconnectFromDom();
	}

	/**
	 * Close is a request from the containing PolloFrame, therefore we
	 * dont' fire a closing event.
	 */
	public boolean close()
	{
		try
		{
			if (xmlModel.closeView(this))
			{
				xmlEditorPanel.disconnectFromDom();
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void addListener(EditorPanelListener listener)
	{
		listeners.add(listener);
	}

	public void fireMenuBarChangedEvent()
	{
		Iterator listenerIt = listeners.iterator();
		while (listenerIt.hasNext())
		{
			((EditorPanelListener)listenerIt.next()).editorPanelMenuChanged(this);
		}
	}

	public void fireClosingEvent()
	{
		Iterator listenerIt = listeners.iterator();
		while (listenerIt.hasNext())
			((EditorPanelListener)listenerIt.next()).editorPanelClosing(this);
	}

	public void fireTitleChangedEvent()
	{
		Iterator listenerIt = listeners.iterator();
		while (listenerIt.hasNext())
			((EditorPanelListener)listenerIt.next()).editorPanelTitleChanged(this);
	}

	public void stateChanged(ChangeEvent e)
	{
		if (ignoreModeChange)
		{
			ignoreModeChange = false;
			return;
		}
		if (modeSwitchPane.getSelectedComponent() == xmlTextEditorPanel)
		{
			try
			{
				xmlModel.switchToTextMode();
			}
			catch (Exception exception)
			{
				modeSwitchPane.setSelectedComponent(xmlEditorPanel);
				ErrorDialog errordialog = new ErrorDialog(polloFrame, "Could not serialize the DOM tree to text.", exception);
				errordialog.show();
			}
		}
		else if (modeSwitchPane.getSelectedComponent() == xmlEditorPanel)
		{
			try
			{
				xmlModel.switchToParsedMode();
			}
			catch (SAXParseException saxparseexception)
			{
				modeSwitchPane.setSelectedComponent(xmlTextEditorPanel);
				xmlTextEditorPanel.showParseException(saxparseexception);
				JOptionPane.showMessageDialog(this, "The document contains well formedness errors.");
			}
			catch (Exception exception1)
			{
				modeSwitchPane.setSelectedComponent(xmlTextEditorPanel);
				ErrorDialog errordialog1 = new ErrorDialog(polloFrame, "Could not parse the text to a DOM tree.", exception1);
				errordialog1.show();
			}
		}
	}

	public void lastViewClosed(XmlModel sourceXmlModel)
	{
	}

	public void fileNameChanged(XmlModel sourceXmlModel)
	{
		title = xmlModel.getFileName();
		fireTitleChangedEvent();
	}

	public void fileChanged(XmlModel sourceXmlModel)
	{
		title = "*" + title;
		fireTitleChangedEvent();
		saveAction.setEnabled(true);
	}

	public void fileSaved(XmlModel sourceXmlModel)
	{
		title = xmlModel.getFileName();
		fireTitleChangedEvent();
		saveAction.setEnabled(false);
	}

	public void switchToTextMode(XmlModel sourceXmlModel)
	{
		xmlTextEditorPanel.jumpToBeginning();
		modeSwitchPane.setSelectedComponent(xmlTextEditorPanel);
		fireMenuBarChangedEvent();
	}

	public void switchToParsedMode(XmlModel sourceXmlModel)
	{
		xmlEditorPanel.reconnectToDom();
		modeSwitchPane.setSelectedComponent(xmlEditorPanel);
		fireMenuBarChangedEvent();
	}

	public String getTitle()
	{
		return title;
	}

	public Component getParentForDialogs()
	{
		return this.getTopLevelAncestor();
	}

	protected class ActionPluginMenu extends JMenu
	{
		public ActionPluginMenu(String name)
		{
			super(name);
		}

		public void setPopupMenuVisible(boolean visible)
		{
			if (visible)
			{
				removeAll();
				actionPlugin.addActionsToPluginMenu(this, xmlEditorPanel.getXmlEditor().getSelectedNode(),
					xmlEditorPanel.getXmlModel(), polloFrame);
				if (getMenuComponentCount() == 0)
				{
					JMenuItem menuItem = new JMenuItem("No plugin actions available");
					menuItem.setEnabled(false);
					add(menuItem);
				}
			}
			super.setPopupMenuVisible(visible);
		}
	}
}
