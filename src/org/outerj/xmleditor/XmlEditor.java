package org.outerj.xmleditor;

import org.outerj.xmleditor.model.XmlModel;
import org.outerj.xmleditor.model.InvalidXmlException;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Frame;


import org.w3c.dom.Element;


/**
  An XmlEditor consists of three parts: an XmlContentEditor (showing the actual XML content),
  an AttributesPanel for editing attribute values, and an ElementDragPanel from which elements
  can be dragged to insert into the tree.
 */
public class XmlEditor extends JPanel
{
	protected XmlContentEditor xmlContentEditor;
	protected XmlModel xmlModel;
	protected Frame frame;
	protected String xpathForRoot;
	protected Schema schema;

	public XmlEditor(Frame frame, XmlModel model, String xpathForRoot, String displaySpecFile,
			String schemaFile, boolean onlyShowElements)
		throws Exception
	{
		this.xpathForRoot = xpathForRoot;

		setLayout(new BorderLayout());
		this.schema = Schema.getInstance(schemaFile);

		// create the xml content editor component
		xmlContentEditor = new XmlContentEditor(xpathForRoot, displaySpecFile, schema, onlyShowElements);
		JScrollPane scrollPane = new JScrollPane(xmlContentEditor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);

		// create the attribute pane
		AttributesPanel attrPanel = new AttributesPanel(model, schema);
		xmlContentEditor.addElementClickedListener(attrPanel);

		// create the list of elements that the user can drag
		ElementDragPanel elementDragPanel = new ElementDragPanel(this);
		JScrollPane elementDragPanelScrollPane = new JScrollPane(elementDragPanel);

		// create first split pane (xmlContentEditor - elementDragPanel)
		JSplitPane splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, elementDragPanelScrollPane);
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
		xmlContentEditor.setXmlModel(xmlModel);
	}

	public XmlModel getXmlModel()
	{
		return xmlModel;
	}

	public XmlContentEditor getXmlContentEditor()
	{
		return xmlContentEditor;
	}


	/**
	  Necessary for showing dialogs.
	 */
	public Frame getContainingFrame()
	{
		return frame;
	}

	public Schema getSchema()
	{
		return schema;
	}
}
