package org.outerj.pollo.engine.generic;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.ViewEngine;
import org.outerj.pollo.xmleditor.view.*;
import org.outerj.pollo.xmleditor.model.*;
import org.outerj.pollo.xmleditor.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A generic ViewEngine. It displays the file using one XmlEditorPanel.
 * The schema and displayspecification to use are read from properties,
 * as is the root element to display.
 *
 * The properties are:
 * viewtype.*name*.viewengine.schema
 * viewtype.*name*.viewengine.displayspec
 * viewtype.*name*.viewengine.rootelement
 *
 * @author Bruno Dumon
 */
public class GenericEngine extends ViewEngine
{
	XmlEditorPanel xmlEditorPanel;

	public GenericEngine(XmlModel xmlModel, String engineName)
		throws Exception
	{
		super(xmlModel);

		setPreferredSize(new Dimension(900, 600));
		setLayout(new BorderLayout());

		Pollo pollo = Pollo.getInstance();

		String schema      = pollo.getProperty("viewtype." + engineName + ".viewengine.schema");
		String displaySpec = pollo.getProperty("viewtype." + engineName + ".viewengine.displayspec");
		String root        = pollo.getProperty("viewtype." + engineName + ".viewengine.rootelement");

		// create the xml editor
		xmlEditorPanel = new XmlEditorPanel(xmlModel, root, displaySpec, schema);

		add(xmlEditorPanel, BorderLayout.CENTER);
	}
}
