package org.outerj.pollo;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.View;
import org.outerj.pollo.xmleditor.model.XmlModelListener;
import org.outerj.pollo.dialog.AboutDialog;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;
import javax.swing.Box;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

/**
  Puts a ViewEngine (which is a JPanel) into a JFrame and adds a menubar.
 */
public class ViewFrame extends JFrame implements ActionListener, View, XmlModelListener
{
	protected ViewEngine viewEngine;

	public ViewFrame(ViewEngine viewEngine)
	{
		super();
		this.viewEngine = viewEngine;
		setContentPane(viewEngine);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent event) { close(); }});

		viewEngine.getXmlModel().addListener(this);
		fileNameChanged(viewEngine.getXmlModel());

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

		pack();
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
				viewEngine.cleanup();
				System.out.println("cleanup done");
				stop();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void stop()
	{
		hide();
		dispose();
	}

	/**
	 * Implementation of the XmlModelListener interface.
	 */
	public void fileNameChanged(XmlModel xmlModel)
	{
		// set window title
		File file = xmlModel.getFile();
		if (file == null)
		{
			setTitle("Untitled");
		}
		else
		{
			setTitle(file.getName() + "  (" + file.getParentFile().getPath() + ")");
		}
	}

	/**
	 * Implementation of the XmlModelListener interface.
	 */
	public void lastViewClosed(XmlModel xmlModel)
	{
	}
}
