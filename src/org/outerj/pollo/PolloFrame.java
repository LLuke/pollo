package org.outerj.pollo;

import org.outerj.pollo.action.*;
import org.outerj.pollo.gui.RecentlyOpenedFilesMenu;
import org.outerj.pollo.xmleditor.IconManager;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * PolloFrame is a top-level frame containing a number of
 * EditorPanel. Each EditorPanel is a view on an open file.
 *
 * There could be multiple PolloFrame's, each containing one or more
 * EditorPanels. Multiple EditorPanels may be showing the same file
 * (XmlModel instance).
 *
 * @author Bruno Dumon
 */
public class PolloFrame extends JFrame implements EditorPanelListener, ChangeListener
{
	/** The menu bar that is shown when there are no EditorPanels */
	protected JMenuBar noEditorPanelsMenuBar;

	/** The pollo instance to which this frame belongs. */
	protected Pollo pollo = Pollo.getInstance();

	/** A tabbed pane containing the editorpanel instances */
	protected DnDTabbedPane editorPanelTabs;

	/** The currently visible toolbar */
	protected JToolBar currentToolBar;

	protected Action fileOpenAction;
	protected Action fileNewAction;
	protected Action exitAction;
	protected Action helpAction;
	protected Action aboutAction;

	public PolloFrame()
	{
		super("Pollo");
        setIconImage(IconManager.getIcon("org/outerj/pollo/resource/pollo_icon.gif").getImage());

		editorPanelTabs = new DnDTabbedPane();
		editorPanelTabs.addChangeListener(this);

		// no border and dark background
		editorPanelTabs.setBorder(new EmptyBorder(0, 0, 0, 0));
		editorPanelTabs.setBackground(new Color(153, 153, 153));
		editorPanelTabs.setOpaque(true);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(editorPanelTabs, BorderLayout.CENTER);

		// initialize actions
		fileOpenAction = new FileOpenAction(this);
		fileNewAction = new FileNewAction(this);
		helpAction = new HelpAction(this);
		aboutAction = new AboutAction(this);
		exitAction = new ExitAction(this);

		// create and display menu bar
		createNoEditorPanelsMenuBar();
		setJMenuBar(noEditorPanelsMenuBar);

		// don't close the window automatically
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// add a window listener
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				close();
			}
		});

		setSize(pollo.getConfiguration().getWindowWidth(), pollo.getConfiguration().getWindowHeight());
	}

	/**
	 * This method creates the menu bar that is shown when there are
	 * no EditorPanels open in this frame.
	 */
	protected void createNoEditorPanelsMenuBar()
	{
		noEditorPanelsMenuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		noEditorPanelsMenuBar.add(fileMenu);
		fileMenu.add(getFileNewAction());
		fileMenu.addSeparator();
		fileMenu.add(getFileOpenAction());
		fileMenu.add(new RecentlyOpenedFilesMenu(this));
		fileMenu.addSeparator();
		fileMenu.add(getExitAction());

		// view menu
		JMenu viewMenu = new JMenu("View");
		noEditorPanelsMenuBar.add(viewMenu);
		viewMenu.add(Pollo.getInstance().getNewPolloFrameAction());
		viewMenu.add(new NewEditorPanelMenu(this));

		// help menu
		noEditorPanelsMenuBar.add(Box.createHorizontalGlue());
		JMenu helpMenu = new JMenu("Help");
		noEditorPanelsMenuBar.add(helpMenu);

		helpMenu.add(getHelpAction());
		helpMenu.add(getAboutAction());
	}

	public void addEditorPanel(EditorPanel editorPanel)
	{
		editorPanelTabs.add(editorPanel.getTitle(), editorPanel);
		editorPanel.addListener(this);
		editorPanelTabs.setSelectedComponent(editorPanel);
	}

	/**
	 * This only removed the EditorPanel from this PolloFrame, it
	 * does not actually close and clean up the EditorPanel. To do
	 * that, use the close() method of EditorPanel.
	 */
	public void removeEditorPanel(EditorPanel editorPanel)
	{
		editorPanelTabs.remove(editorPanel);
	}

	public Action getFileOpenAction()
	{
		return fileOpenAction;
	}

	public Action getFileNewAction()
	{
		return fileNewAction;
	}

	public Action getExitAction()
	{
		return exitAction;
	}

	public Action getAboutAction()
	{
		return aboutAction;
	}

	public Action getHelpAction()
	{
		return helpAction;
	}


	/**
	 * Implementation of EditorPanelListener interface.
	 */
	public void editorPanelMenuChanged(EditorPanel source)
	{
		if (editorPanelTabs.getSelectedComponent() == source)
		{
			setJMenuBar(source.getMenuBar());
		}
	}

	/**
	 * Implementation of EditorPanelListener interface.
	 */
	public void editorPanelToolBarChanged(EditorPanel source)
	{
		if (editorPanelTabs.getSelectedComponent() == source)
		{
			if (currentToolBar != null)
				getContentPane().remove(currentToolBar);
			currentToolBar = source.getToolBar();
			if (currentToolBar != null)
				getContentPane().add(currentToolBar, BorderLayout.NORTH);
			getRootPane().revalidate();
		}
	}

	/**
	 * Implementation of EditorPanelListener interface.
	 */
	public void editorPanelClosing(EditorPanel source)
	{
		editorPanelTabs.remove(source);
		stateChanged(null);
	}

	public void editorPanelTitleChanged(EditorPanel source)
	{
		editorPanelTabs.setTitleAt(editorPanelTabs.indexOfComponent(source),
				source.getTitle());

		if (getCurrentEditorPanel() == source)
		{
			setFrameTitle();
		}
	}

	/**
	 * Called when another tab is selected.
	 */
	public void stateChanged(ChangeEvent e)
	{
		EditorPanel currentEditorPanel = getCurrentEditorPanel();
		if (currentEditorPanel != null)
		{
			setJMenuBar(currentEditorPanel.getMenuBar());
			JToolBar toolBar = currentEditorPanel.getToolBar();
			if (currentToolBar != null)
				getContentPane().remove(currentToolBar);
			currentToolBar = toolBar;
			if (currentToolBar != null)
				getContentPane().add(currentToolBar, BorderLayout.NORTH);
		}
		else
		{
			setJMenuBar(noEditorPanelsMenuBar);
			if (currentToolBar != null)
			{
				getContentPane().remove(currentToolBar);
				currentToolBar = null;
			}
		}
		setFrameTitle();
		getRootPane().revalidate(); // this is needed after changing the toolbar
	}

	public void setFrameTitle()
	{
		EditorPanel currentEditorPanel = getCurrentEditorPanel();
		if (currentEditorPanel != null)
		{
			String modifiedStar = currentEditorPanel.getXmlModel().isModified() ? "*" : "";
			setTitle("Pollo - " + modifiedStar + currentEditorPanel.getXmlModel().getLongTitle());
		}
		else
		{
			setTitle("Pollo");
		}
	}

	/**
	 * Returns the currently active EditorPanel.
	 */
	public EditorPanel getCurrentEditorPanel()
	{
		return (EditorPanel)editorPanelTabs.getSelectedComponent();
	}

	public void close()
	{
		try
		{
			// close all the EditorPanels

			// make a copy of the list of editorpanels
			ArrayList editorPanelsList = new ArrayList();
			for (int i = 0; i < editorPanelTabs.getTabCount(); i++)
			{
				editorPanelsList.add(editorPanelTabs.getComponent(i));
			}

			// try to close the editor panels. This can be canceled by the user
			// when asking him to save a changed file and he/she selects cancel.
			boolean allEditorPanelsClosed = true;
			for (int i = 0; i < editorPanelsList.size(); i++)
			{
				EditorPanel editorPanel = (EditorPanel)editorPanelsList.get(i);
				if (!editorPanel.close())
				{
					allEditorPanelsClosed = false;
					break;
				}
				else
				{
					editorPanelTabs.remove(editorPanel);
				}
			}

			// if all EditorPanels were closed, we can close the window.
			if (allEditorPanelsClosed)
			{
				hide();
				dispose();
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

    public class DnDTabbedPane extends JTabbedPane implements java.awt.dnd.DropTargetListener
    {
        public DnDTabbedPane()
        {
            super();
            java.awt.dnd.DropTarget dropTarget = new java.awt.dnd.DropTarget(this, this);
        }

        public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
        {
        }

        public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent)
        {
        }

        public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
        {
			// the line below works only from java 1.4
			//int index = indexAtLocation((int)dropTargetDragEvent.getLocation().getX(), (int)dropTargetDragEvent.getLocation().getY());

			int index =  getUI().tabForCoordinate(this, (int)dropTargetDragEvent.getLocation().getX(), (int)dropTargetDragEvent.getLocation().getY());
            if(index >= 0 && index<getTabCount() && index != getSelectedIndex())
            {
                setSelectedIndex(index);
            }
        }

        public void drop(java.awt.dnd.DropTargetDropEvent dropTargetDropEvent)
        {
        }

        public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
        {
        }

    }
}
