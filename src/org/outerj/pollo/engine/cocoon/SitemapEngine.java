package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.xmleditor.view.*;
import org.outerj.pollo.xmleditor.model.*;
import org.outerj.pollo.xmleditor.*;
import org.outerj.pollo.ViewEngine;

import org.outerj.pollo.engine.cocoon.compeditor.ComponentsEditor;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.awt.Dimension;

public class SitemapEngine extends ViewEngine
{
	public static final String COCOON_URI = "http://apache.org/cocoon/sitemap/1.0";

	public SitemapEngine(XmlModel xmlModel, String engineName)
		throws Exception
	{
		super(xmlModel);

		setSize(900, 600);
		setLayout(new BorderLayout());

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setPreferredSize(new Dimension(900, 600));
		tabbedPane.addTab("Pipelines", new XmlEditorPanel(xmlModel, "/map:sitemap/map:pipelines",
					"conf/sitemapspec.xml", "conf/sitemapschema.xml"));
		tabbedPane.addTab("Generators",
				new ComponentsEditor(xmlModel.getNode("/map:sitemap/map:components/map:generators"), "generator",
					COCOON_URI, "src", true, xmlModel));
		tabbedPane.addTab("Transformers",
				new ComponentsEditor(xmlModel.getNode("/map:sitemap/map:components/map:transformers"), "transformer",
					COCOON_URI, "src", true, xmlModel));
		tabbedPane.addTab("Readers",
				new ComponentsEditor(xmlModel.getNode("/map:sitemap/map:components/map:readers"), "reader",
					COCOON_URI, "src", true, xmlModel));
		tabbedPane.addTab("Serializers",
				new ComponentsEditor(xmlModel.getNode("/map:sitemap/map:components/map:serializers"), "serializer",
					COCOON_URI, "src", true, xmlModel));
		tabbedPane.addTab("Selectors",
				new ComponentsEditor(xmlModel.getNode("/map:sitemap/map:components/map:selectors"), "selector",
					COCOON_URI, "src", false, xmlModel));
		tabbedPane.addTab("Matchers",
				new ComponentsEditor(xmlModel.getNode("/map:sitemap/map:components/map:matchers"), "matcher",
					COCOON_URI, "src", false, xmlModel));
		tabbedPane.addTab("Actions",
				new ComponentsEditor(xmlModel.getNode("/map:sitemap/map:components/map:actions"), "action",
					COCOON_URI, "src", false, xmlModel));

		XmlEditorPanel actionsEditor = new XmlEditorPanel(xmlModel, "/map:sitemap/map:action-sets",
				"conf/sitemapspec.xml", "conf/sitemapschema.xml");
		tabbedPane.addTab("Action sets", actionsEditor);

		XmlEditorPanel resourcesEditor = new XmlEditorPanel(xmlModel, "/map:sitemap/map:resources",
				"conf/sitemapspec.xml", "conf/sitemapschema.xml");
		tabbedPane.addTab("Resources", resourcesEditor);

		XmlEditorPanel viewsEditor = new XmlEditorPanel(xmlModel, "/map:sitemap/map:views",
				"conf/sitemapspec.xml", "conf/sitemapschema.xml");
		tabbedPane.addTab("Views", viewsEditor);

		add(tabbedPane, BorderLayout.CENTER);
	}
}
