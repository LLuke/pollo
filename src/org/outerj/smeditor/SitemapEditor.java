package org.outerj.smeditor;

import org.outerj.xmleditor.view.*;
import org.outerj.xmleditor.model.*;
import org.outerj.xmleditor.*;
import org.outerj.smeditor.compeditor.ComponentsEditor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;

import org.xml.sax.InputSource;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class SitemapEditor 
{
	public static final String COCOON_URI = "http://apache.org/cocoon/sitemap/1.0";

	JFrame frame;
	JMenuBar menuBar;

	// the following fields contain data related to the currently open
	// sitemap
	JTabbedPane tabbedPane;
	XmlEditor xmlEditor;
	JMenu editMenu, fileMenu;
	JMenuItem fileSave, fileClose;

	public static void main(String [] argv)
		throws Exception
	{
		SitemapEditor sitemapEditor = new SitemapEditor();
		sitemapEditor.run();
	}

	public SitemapEditor()
	{
		frame = new JFrame("Cocoon sitemap editor -- 'Proof of technology' release");
		frame.setSize(900, 600);
		frame.getContentPane().setLayout(new BorderLayout());

		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {System.exit(0);}
				});

		// create the menu bar
		menuBar = new JMenuBar();
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);

		fileMenu = new JMenu("File");
		fileMenu.add(new OpenSitemapAction(this, "Open sitemap..."));
		menuBar.add(fileMenu);
	}

	public void run()
	{
		frame.show();
	}

	public JFrame getSitemapEditorFrame()
	{
		return frame;
	}

	public XmlEditor getXmlEditor()
	{
		return xmlEditor;
	}

	public void setFile(String filename)
	{
		if (xmlEditor != null)
		{
			closeCurrentFile();
		}

		// create an xmlmodel
		XmlModel model = null;
		try
		{
			model = new XmlModel(filename);

			// create the xml editor
			xmlEditor = new XmlEditor(frame, model, "/map:sitemap/map:pipelines",
					"conf/sitemapspec.xml", "conf/sitemapschema.xml", true);

			// create the switch-bar on the left
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setPreferredSize(new Dimension(900, 600));
			tabbedPane.addTab("Pipelines", xmlEditor);
			tabbedPane.addTab("Generators",
					new ComponentsEditor(model.getNode("/map:sitemap/map:components/map:generators"), "generator",
						COCOON_URI, "src", true, model));
			tabbedPane.addTab("Transformers",
					new ComponentsEditor(model.getNode("/map:sitemap/map:components/map:transformers"), "transformer",
						COCOON_URI, "src", true, model));
			tabbedPane.addTab("Readers",
					new ComponentsEditor(model.getNode("/map:sitemap/map:components/map:readers"), "reader",
						COCOON_URI, "src", true, model));
			tabbedPane.addTab("Serializers",
					new ComponentsEditor(model.getNode("/map:sitemap/map:components/map:serializers"), "serializer",
						COCOON_URI, "src", true, model));
			tabbedPane.addTab("Selectors",
					new ComponentsEditor(model.getNode("/map:sitemap/map:components/map:selectors"), "selector",
						COCOON_URI, "src", false, model));
			tabbedPane.addTab("Matchers",
					new ComponentsEditor(model.getNode("/map:sitemap/map:components/map:matchers"), "matcher",
						COCOON_URI, "src", false, model));
			tabbedPane.addTab("Actions",
					new ComponentsEditor(model.getNode("/map:sitemap/map:components/map:actions"), "action",
						COCOON_URI, "src", false, model));

			XmlEditor actionsEditor = new XmlEditor(frame, model, "/map:sitemap/map:action-sets",
					"conf/sitemapspec.xml", "conf/sitemapschema.xml", true);
			tabbedPane.addTab("Action sets", actionsEditor);

			XmlEditor resourcesEditor = new XmlEditor(frame, model, "/map:sitemap/map:resources",
					"conf/sitemapspec.xml", "conf/sitemapschema.xml", true);
			tabbedPane.addTab("Resources", resourcesEditor);

			XmlEditor viewsEditor = new XmlEditor(frame, model, "/map:sitemap/map:views",
					"conf/sitemapspec.xml", "conf/sitemapschema.xml", true);
			tabbedPane.addTab("Views", viewsEditor);

			frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

			// adjust menubar
			editMenu = new JMenu("Edit");
			editMenu.add(model.getUndo().getUndoAction());
			menuBar.add(editMenu);

			fileSave = new JMenuItem(new FileSaveAction(this, "Save"));
			fileMenu.add(fileSave);
			fileClose = new JMenuItem(new FileCloseAction(this, "Close"));
			fileMenu.add(fileClose);

			frame.pack();
			frame.repaint();
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
			JOptionPane.showMessageDialog(frame, exc.toString(),
					"Exception occured", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	public void closeCurrentFile()
	{
		menuBar.remove(editMenu);
		fileMenu.remove(fileSave);
		fileMenu.remove(fileClose);
		xmlEditor = null;
		frame.getContentPane().remove(tabbedPane);
		tabbedPane = null;
		frame.repaint();
	}
}
