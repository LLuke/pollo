package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.xmleditor.view.*;
import org.outerj.pollo.xmleditor.model.*;
import org.outerj.pollo.xmleditor.*;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.config.ViewTypeConf;
import org.outerj.pollo.ViewEngine;

import org.outerj.pollo.engine.cocoon.compeditor.ComponentsEditor;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.awt.Dimension;

public class SitemapEngine extends ViewEngine
{
	public static final String COCOON_URI = "http://apache.org/cocoon/sitemap/1.0";

	protected XmlEditorPanel xmlEditorPanel;
	protected XmlEditorPanel actionsEditor;
	protected XmlEditorPanel resourcesEditor;
	protected XmlEditorPanel viewsEditor;

	public SitemapEngine(XmlModel xmlModel, ViewTypeConf viewTypeConf)
		throws Exception
	{
		super(xmlModel);

		setSize(900, 600);
		setLayout(new BorderLayout());

		IDisplaySpecification displaySpec = viewTypeConf.createDisplaySpecChain();
		ISchema schema = viewTypeConf.createSchemaChain();
		IAttributeEditorPlugin attrEditorPlugin = viewTypeConf.createAttrEditorPluginChain(xmlModel, schema);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setPreferredSize(new Dimension(900, 600));
		xmlEditorPanel = new XmlEditorPanel(xmlModel, "/map:sitemap/map:pipelines",
				displaySpec, schema, attrEditorPlugin);
		tabbedPane.addTab("Pipelines", xmlEditorPanel);
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

		actionsEditor = new XmlEditorPanel(xmlModel, "/map:sitemap/map:action-sets",
				displaySpec, schema, attrEditorPlugin);
		tabbedPane.addTab("Action sets", actionsEditor);

		resourcesEditor = new XmlEditorPanel(xmlModel, "/map:sitemap/map:resources",
				displaySpec, schema, attrEditorPlugin);
		tabbedPane.addTab("Resources", resourcesEditor);

		viewsEditor = new XmlEditorPanel(xmlModel, "/map:sitemap/map:views",
				displaySpec, schema, attrEditorPlugin);
		tabbedPane.addTab("Views", viewsEditor);

		add(tabbedPane, BorderLayout.CENTER);
	}

	public void cleanup()
	{
		xmlEditorPanel.cleanup();
		actionsEditor.cleanup();
		resourcesEditor.cleanup();
		viewsEditor.cleanup();
	}
}
