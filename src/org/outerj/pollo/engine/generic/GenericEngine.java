package org.outerj.pollo.engine.generic;

import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.plugin.AttrEditorPluginChain;
import org.outerj.pollo.config.ViewTypeConf;
import org.outerj.pollo.Pollo;
import org.outerj.pollo.ViewEngine;
import org.outerj.pollo.xmleditor.view.*;
import org.outerj.pollo.xmleditor.model.*;
import org.outerj.pollo.xmleditor.*;

import java.util.HashMap;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A generic ViewEngine. It displays the file using one XmlEditorPanel.
 *
 * @author Bruno Dumon
 */
public class GenericEngine extends ViewEngine
{
	XmlEditorPanel xmlEditorPanel;

	public GenericEngine(XmlModel xmlModel, ViewTypeConf viewTypeConf)
		throws Exception
	{
		super(xmlModel);

		setPreferredSize(new Dimension(900, 600));
		setLayout(new BorderLayout());

		String root        = null; // "/*";
		IDisplaySpecification displaySpecChain = viewTypeConf.createDisplaySpecChain();
		ISchema schemaChain = viewTypeConf.createSchemaChain();
		IAttributeEditorPlugin attrEditorPluginChain =
			viewTypeConf.createAttrEditorPluginChain(xmlModel, schemaChain);

		// create the xml editor
		xmlEditorPanel = new XmlEditorPanel(xmlModel, root, displaySpecChain, schemaChain,
				attrEditorPluginChain);

		add(xmlEditorPanel, BorderLayout.CENTER);
	}

	public void dispose()
	{
		xmlEditorPanel.dispose();
	}
}
