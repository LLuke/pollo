package org.outerj.pollo;

import org.outerj.pollo.action.CloseAction;
import org.outerj.pollo.action.SaveAction;
import org.outerj.pollo.action.SaveAsAction;
import org.outerj.pollo.action.CloseViewAction;
import org.outerj.pollo.config.ViewTypeConf;
import org.outerj.pollo.gui.ErrorDialog;
import org.outerj.pollo.gui.ToolButton;
import org.outerj.pollo.gui.PopupToolButton;
import org.outerj.pollo.gui.RecentlyOpenedFilesMenu;
import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.texteditor.XmlTextEditorPanel;
import org.outerj.pollo.texteditor.XmlTextEditor;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlEditorPanel;
import org.outerj.pollo.xmleditor.IconManager;
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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class EditorPanelImpl extends EditorPanel implements View, XmlModelListener
{
	protected XmlModel xmlModel;
	protected PolloFrame polloFrame;
	protected XmlEditorPanel xmlEditorPanel;
	protected XmlTextEditorPanel xmlTextEditorPanel;
	protected JComponent currentModePanel; // reference to either xmlEditorPanel or xmlTextEditorPanel
	protected ArrayList listeners = new ArrayList();
	protected String title;
	protected JMenuBar domModeMenuBar;
	protected JMenuBar textModeMenuBar;
	protected JToolBar domModeToolBar;
	protected JToolBar textModeToolBar;
	protected ModeSwitchDropDown textToolBarSwitch = new ModeSwitchDropDown();
	protected ModeSwitchDropDown domToolBarSwitch = new ModeSwitchDropDown();
	protected IActionPlugin actionPlugin;
	protected SaveAction saveAction;
	protected SaveAsAction saveAsAction;
	protected CloseAction closeAction;
	protected CloseViewAction closeViewAction;

	public EditorPanelImpl(XmlModel xmlModel, ViewTypeConf viewTypeConf, PolloFrame polloFrame)
		throws Exception
	{
		this.xmlModel = xmlModel;
		this.polloFrame = polloFrame;

		IDisplaySpecification idisplayspecification = viewTypeConf.createDisplaySpecChain();
		ISchema ischema = viewTypeConf.createSchemaChain();
		IAttributeEditorPlugin iattributeeditorplugin = viewTypeConf.createAttrEditorPluginChain(xmlModel, ischema);
		actionPlugin = viewTypeConf.createActionPlugins(this, polloFrame);

		xmlEditorPanel = new XmlEditorPanel(xmlModel, null, idisplayspecification, ischema, iattributeeditorplugin);
		xmlTextEditorPanel = new XmlTextEditorPanel(xmlModel, ischema);


		// no borders
		xmlTextEditorPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		xmlEditorPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.setBorder(new EmptyBorder(0, 0, 0, 0));

		// determine the start mode
		if (xmlModel.isInTextMode())
			currentModePanel = xmlTextEditorPanel;
		else
			currentModePanel = xmlEditorPanel;

		// initialize actions
		saveAction = new SaveAction(xmlModel, polloFrame);
		if (!xmlModel.isModified())
			saveAction.setEnabled(false);
		saveAsAction = new SaveAsAction(xmlModel, polloFrame);
		closeAction = new CloseAction(xmlModel, polloFrame);
		closeViewAction = new CloseViewAction(polloFrame, this);

		// add the component to the panel
		this.setLayout(new BorderLayout());
		add(currentModePanel, BorderLayout.CENTER);

		xmlModel.addListener(this);

		title = xmlModel.getFileName();
		if (xmlModel.isModified())
			title = "*" + title;

		createMenus();
		createToolBars();
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
		domFileMenu.add(new RecentlyOpenedFilesMenu(polloFrame));
		textFileMenu.add(new RecentlyOpenedFilesMenu(polloFrame));
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
		domModeMenuBar.add(domViewMenu);
		textModeMenuBar.add(textViewMenu);
		domViewMenu.add(closeViewAction);
		textViewMenu.add(closeViewAction);
		domViewMenu.add(new NewEditorPanelMenu(polloFrame));
		textViewMenu.add(new NewEditorPanelMenu(polloFrame));
		domViewMenu.add(Pollo.getInstance().getNewPolloFrameAction());
		textViewMenu.add(Pollo.getInstance().getNewPolloFrameAction());

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

	protected void createToolBars()
	{
		domModeToolBar = new JToolBar();
		domModeToolBar.setFloatable(false);
		domModeToolBar.add(new ToolButton(polloFrame.getFileOpenAction()));
		domModeToolBar.add(new ToolButton(polloFrame.getFileNewAction()));

		domModeToolBar.addSeparator();
		domModeToolBar.add(new ToolButton(xmlModel.getUndo().getUndoAction()));

		domModeToolBar.addSeparator();
		XmlEditor xmlEditor = xmlEditorPanel.getXmlEditor();
		domModeToolBar.add(new ToolButton(xmlEditor.getCutAction()));
		domModeToolBar.add(new ToolButton(xmlEditor.getCopyAction()));

		PopupToolButton domPasteButton = new PopupToolButton("Paste:", "Paste", IconManager.getIcon("org/outerj/pollo/resource/stock_paste-16.png"));
		domPasteButton.addAction(xmlEditor.getPasteBeforeAction());
		domPasteButton.addAction(xmlEditor.getPasteInsideAction());
		domPasteButton.addAction(xmlEditor.getPasteAfterAction());
		domModeToolBar.add(domPasteButton);

		domModeToolBar.addSeparator();
		PopupToolButton domTextButton = new PopupToolButton("Insert text:", "Insert Text Node", IconManager.getIcon("org/outerj/pollo/resource/stock_font-16.png"));
		domTextButton.addAction(xmlEditor.getInsertTextBeforeAction());
		domTextButton.addAction(xmlEditor.getInsertTextInsideAction());
		domTextButton.addAction(xmlEditor.getInsertTextAfterAction());
		domModeToolBar.add(domTextButton);

		PopupToolButton domCommentButton = new PopupToolButton("Insert comment:", "Insert Comment Node", IconManager.getIcon("org/outerj/pollo/resource/comment-16.png"));
		domCommentButton.addAction(xmlEditor.getInsertCommentBeforeAction());
		domCommentButton.addAction(xmlEditor.getInsertCommentInsideAction());
		domCommentButton.addAction(xmlEditor.getInsertCommentAfterAction());
		domModeToolBar.add(domCommentButton);

		domModeToolBar.addSeparator();
		domModeToolBar.add(domToolBarSwitch);

		textModeToolBar = new JToolBar();
		textModeToolBar.setFloatable(false);

		textModeToolBar.add(new ToolButton(polloFrame.getFileOpenAction()));
		textModeToolBar.add(new ToolButton(polloFrame.getFileNewAction()));

		textModeToolBar.addSeparator();
		XmlTextEditor xmlTextEditor = xmlTextEditorPanel.getEditor();
		textModeToolBar.add(xmlTextEditor.getCutAction());
		textModeToolBar.add(xmlTextEditor.getCopyAction());
		textModeToolBar.add(xmlTextEditor.getPasteAction());

		textModeToolBar.addSeparator();
		textModeToolBar.add(textToolBarSwitch);
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

	public JToolBar getToolBar()
	{
		if (xmlModel.isInParsedMode())
		{
			domToolBarSwitch.setToTreeMode();
			return domModeToolBar;
		}
		else if (xmlModel.isInTextMode())
		{
			textToolBarSwitch.setToTextMode();
			return textModeToolBar;
		}
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

	public void fireToolBarChangedEvent()
	{
		Iterator listenerIt = listeners.iterator();
		while (listenerIt.hasNext())
		{
			((EditorPanelListener)listenerIt.next()).editorPanelToolBarChanged(this);
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
		remove(currentModePanel);
		currentModePanel = xmlTextEditorPanel;
		add(currentModePanel, BorderLayout.CENTER);
		fireMenuBarChangedEvent();
		fireToolBarChangedEvent();
		getRootPane().repaint();
	}

	public void switchToParsedMode(XmlModel sourceXmlModel)
	{
		xmlEditorPanel.reconnectToDom();
		remove(currentModePanel);
		currentModePanel = xmlEditorPanel;
		add(currentModePanel, BorderLayout.CENTER);
		fireMenuBarChangedEvent();
		fireToolBarChangedEvent();
		getRootPane().repaint();
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
				actionPlugin.addActionsToPluginMenu(this, xmlEditorPanel.getXmlEditor().getSelectedNode());
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

	public class ModeSwitchDropDown extends JComboBox
	{
		public ModeSwitchDropDown()
		{
            addItem("Tree view");
			addItem("Text view");

			// set maximum size to preferred size, otherwise the dropdown will take
			// up all available width
			Dimension dim = getPreferredSize();
			setMaximumSize(dim);

			setRequestFocusEnabled(false);

			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (getSelectedItem().equals("Tree view"))
					{
						if (currentModePanel != xmlEditorPanel)
						{
							try
							{
								xmlModel.switchToParsedMode();
							}
							catch (SAXParseException saxparseexception)
							{
								xmlTextEditorPanel.showParseException(saxparseexception);
								JOptionPane.showMessageDialog(polloFrame, "The document contains well formedness errors.");
							}
							catch (Exception exception1)
							{
								ErrorDialog errordialog1 = new ErrorDialog(polloFrame, "Could not parse the text to a DOM tree.", exception1);
								errordialog1.show();
							}
						}
					}
					else if (getSelectedItem().equals("Text view"))
					{
						if (currentModePanel != xmlTextEditorPanel)
						{
							try
							{
								xmlModel.switchToTextMode();
							}
							catch (Exception exception)
							{
								ErrorDialog errordialog = new ErrorDialog(polloFrame, "Could not serialize the DOM tree to text.", exception);
								errordialog.show();
							}
						}
					}
				}
			});

		}

		public void setToTextMode()
		{
			setSelectedIndex(1);
		}

		public void setToTreeMode()
		{
			setSelectedIndex(0);
		}

	}
}
