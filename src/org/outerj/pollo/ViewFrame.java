package org.outerj.pollo;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.View;
import org.outerj.pollo.xmleditor.model.XmlModelListener;
import org.outerj.pollo.dialog.AboutDialog;
import org.outerj.pollo.texteditor.JEditTextArea;
import org.outerj.pollo.texteditor.XMLTokenMarker;
import org.outerj.pollo.texteditor.ReadOnlyInputHandler;
import org.outerj.pollo.dialog.ErrorDialog;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.Box;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

/**
  Puts a ViewEngine (which is a JPanel) into a JFrame and adds a menubar.
 */
public class ViewFrame extends JFrame implements ActionListener, View, XmlModelListener, ChangeListener
{
	protected ViewEngine viewEngine;
	protected JEditTextArea textViewer;
	protected String frameTitle = "";

	public ViewFrame(ViewEngine viewEngine)
	{
		super();
		this.viewEngine = viewEngine;

		textViewer = new JEditTextArea();
		textViewer.setTokenMarker(new XMLTokenMarker());
		textViewer.setInputHandler(ReadOnlyInputHandler.getInstance());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Edit", viewEngine);
		tabbedPane.add("View source", textViewer);
		tabbedPane.addChangeListener(this);
		
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent event) { close(); }});

		viewEngine.getXmlModel().addListener(this);

		// set the window title
		fileNameChanged(viewEngine.getXmlModel());

		// create the menu bar
		createMenus();

		pack();
	}

	public void createMenus()
	{
		// create menu bar
		JMenuBar menuBar = new JMenuBar();

		// file menu
		JMenu fileMenu = new JMenu("File");

		JMenuItem openItem = new JMenuItem(Pollo.getInstance().getFileOpenAction());
		fileMenu.add(openItem);

		JMenuItem saveItem = new JMenuItem(viewEngine.getXmlModel().getSaveAction());
		fileMenu.add(saveItem);

		JMenuItem saveAsItem = new JMenuItem(viewEngine.getXmlModel().getSaveAsAction());
		fileMenu.add(saveAsItem);

		JMenuItem closeItem = new JMenuItem(viewEngine.getXmlModel().getCloseAction());
		fileMenu.add(closeItem);

		menuBar.add(fileMenu);

		// edit menu
		JMenu editMenu = new JMenu("Edit");

		JMenuItem undoItem = new JMenuItem(viewEngine.getXmlModel().getUndo().getUndoAction());
		editMenu.add(undoItem);

		menuBar.add(editMenu);

		// view menu
		JMenu viewMenu = new JMenu("View");

		JMenuItem newViewItem = new JMenuItem("New...");
		newViewItem.setActionCommand("new-view");
		newViewItem.addActionListener(this);
		viewMenu.add(newViewItem);

		JMenuItem closeViewItem = new JMenuItem("Close");
		closeViewItem.setActionCommand("close-view");
		closeViewItem.addActionListener(this);
		viewMenu.add(closeViewItem);

		menuBar.add(viewMenu);

		setJMenuBar(menuBar);

		// create help menu
		menuBar.add(Box.createHorizontalGlue());
		JMenu helpMenu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About...");
		aboutItem.setActionCommand("about");
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);
		menuBar.add(helpMenu);
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("new-view"))
		{
			Pollo.getInstance().createView(viewEngine.getXmlModel());
		}
		else if (event.getActionCommand().equals("close-view"))
		{
			close();
		}
		else if (event.getActionCommand().equals("about"))
		{
			AboutDialog aboutDialog = new AboutDialog(this);
			aboutDialog.show();
		}
	}

	public void close()
	{
		try
		{
			if (viewEngine.getXmlModel().closeView(this))
			{
				System.out.println("Frame closes, will do cleanup");
				viewEngine.dispose();
				System.out.println("cleanup done");
				stop();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/** Implementation of the ChangeListener interface. */
	public void stateChanged(ChangeEvent event)
	{
		JTabbedPane tabbedPane = (JTabbedPane)event.getSource();
		if (tabbedPane.getSelectedComponent() == textViewer)
		{
			try
			{
				String xml = viewEngine.getXmlModel().toXMLString();
				textViewer.setText(xml);
				textViewer.setCaretPosition(0);
			}
			catch (Exception e)
			{
				tabbedPane.setSelectedComponent(viewEngine);
				ErrorDialog errorDialog = new ErrorDialog(null, "Could not serialize the DOM tree to text.", e);
				errorDialog.show();
			}
		}
	}

	public void stop()
	{
		hide();
		dispose();
	}

	/** Implementation of the XmlModelListener interface. */
	public void fileNameChanged(XmlModel xmlModel)
	{
		// set window title
		File file = xmlModel.getFile();
		if (file == null)
		{
			frameTitle = "Untitled";
		}
		else
		{
			frameTitle = file.getName() + "  (" + file.getParentFile().getPath() + ")";
		}
		setTitle(frameTitle);
	}

	/** Implementation of the XmlModelListener interface. */
	public void lastViewClosed(XmlModel xmlModel) {}

	/** Implementation of the XmlModelListener interface. */
	public void fileChanged(XmlModel sourceXmlModel)
	{
		setTitle("*" + frameTitle);
	}

	/** Implementation of the XmlModelListener interface. */
	public void fileSaved(XmlModel sourceXmlModel)
	{
		setTitle(frameTitle);
	}

}
