package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.InvalidXmlException;
import org.outerj.pollo.xmleditor.model.Schema;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Frame;


import org.w3c.dom.Element;


/**
 * An XmlEditorPanel consists of three parts: an XmlEditor (showing the actual XML content),
 * an AttributesPanel for editing attribute values, and a NodeInsertionPanel to insert new
 * elements.
 *
 * @author Bruno Dumon
 */
public class XmlEditorPanel extends JPanel
{
	protected XmlEditor xmlEditor;
	protected XmlModel xmlModel;
	protected String xpathForRoot;
	protected Schema schema;

	public XmlEditorPanel(XmlModel model, String xpathForRoot, String displaySpecFile, String schemaFile)
		throws Exception
	{
		this.xpathForRoot = xpathForRoot;

		setLayout(new BorderLayout());
		this.schema = Schema.getInstance(schemaFile);

		// create the xml content editor component
		xmlEditor = new XmlEditor(xpathForRoot, displaySpecFile, schema);
		JScrollPane scrollPane = new JScrollPane(xmlEditor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);

		// create the attribute pane
		AttributesPanel attrPanel = new AttributesPanel(model, schema);
		xmlEditor.getSelectionInfo().addListener(attrPanel);

		// create the panel from which the user can select new nodes to insert
		NodeInsertionPanel nodeInsertionPanel = new NodeInsertionPanel(this);

		// create first split pane (xmlEditor - elementDragPanel)
		JSplitPane splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, nodeInsertionPanel);
		splitPane1.setResizeWeight(1); // xml content editor gets extra space
		splitPane1.setDividerLocation(-1);

		// create second splitpane (first split pane - attributesPanel)
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane1, attrPanel);
		splitPane2.setResizeWeight(1); // xml content editor gets extra space
		splitPane2.setDividerLocation(-1);
		add(splitPane2, BorderLayout.CENTER);

		setXmlModel(model);
	}

	public void setXmlModel(XmlModel xmlModel)
	{
		this.xmlModel = xmlModel;
		xmlEditor.setXmlModel(xmlModel);
	}

	public XmlModel getXmlModel()
	{
		return xmlModel;
	}

	public XmlEditor getXmlEditor()
	{
		return xmlEditor;
	}

	public Schema getSchema()
	{
		return schema;
	}
}
