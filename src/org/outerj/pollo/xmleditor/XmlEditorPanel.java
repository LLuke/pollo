package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.InvalidXmlException;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.xmleditor.chardataeditor.CharDataPanel;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.util.FocusBorder;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.Container;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


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
	protected ISchema schema;
	protected NodeInsertionPanel nodeInsertionPanel;
	protected NodeDetailsPanel nodeDetailsPanel;

	public XmlEditorPanel(XmlModel model, String xpathForRoot, IDisplaySpecification displaySpec,
			ISchema schema, IAttributeEditorPlugin attrEditorPlugin)
		throws Exception
	{
		this.xpathForRoot = xpathForRoot;

		setLayout(new BorderLayout());
		this.schema = schema;

		// create the xml content editor component
		xmlEditor = new XmlEditor(xpathForRoot, displaySpec, schema);
		JScrollPane scrollPane = new JScrollPane(xmlEditor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		xmlEditor.addFocusListener(new FocusBorder(scrollPane));

		// create the details pane
		nodeDetailsPanel = new NodeDetailsPanel();
		xmlEditor.getSelectionInfo().addListener(nodeDetailsPanel);

		AttributesPanel attrPanel = new AttributesPanel(model, schema, attrEditorPlugin);
		xmlEditor.getSelectionInfo().addListener(attrPanel);
		nodeDetailsPanel.add(Node.ELEMENT_NODE, attrPanel);

		CharDataPanel charDataPanel1 = new CharDataPanel(model, Node.CDATA_SECTION_NODE);
		xmlEditor.getSelectionInfo().addListener(charDataPanel1);
		nodeDetailsPanel.add(Node.CDATA_SECTION_NODE, charDataPanel1);

		CharDataPanel charDataPanel2 = new CharDataPanel(model, Node.TEXT_NODE);
		xmlEditor.getSelectionInfo().addListener(charDataPanel2);
		nodeDetailsPanel.add(Node.TEXT_NODE, charDataPanel2);

		CharDataPanel charDataPanel3 = new CharDataPanel(model, Node.COMMENT_NODE);
		xmlEditor.getSelectionInfo().addListener(charDataPanel3);
		nodeDetailsPanel.add(Node.COMMENT_NODE, charDataPanel3);

		CharDataPanel charDataPanel4 = new CharDataPanel(model, Node.PROCESSING_INSTRUCTION_NODE);
		xmlEditor.getSelectionInfo().addListener(charDataPanel4);
		nodeDetailsPanel.add(Node.PROCESSING_INSTRUCTION_NODE, charDataPanel4);

		// create the panel from which the user can select new nodes to insert
		nodeInsertionPanel = new NodeInsertionPanel(this);

		// bind some keyevents of the xml editor
		ActionMap editorActionMap = xmlEditor.getActionMap();
		editorActionMap.put("insert-node-after", new AbstractAction()
				{
					public void actionPerformed(ActionEvent e)
					{
						nodeInsertionPanel.activateInsertAfter();
					}
				});
		editorActionMap.put("insert-node-before", new AbstractAction()
				{
					public void actionPerformed(ActionEvent e)
					{
						nodeInsertionPanel.activateInsertBefore();
					}
				});
		editorActionMap.put("insert-node-inside", new AbstractAction()
				{
					public void actionPerformed(ActionEvent e)
					{
						nodeInsertionPanel.activateInsertInside();
					}
				});
		editorActionMap.put("edit-details", new AbstractAction()
				{
					public void actionPerformed(ActionEvent e)
					{
						nodeDetailsPanel.requestFocus();
					}
				});

		// Create the container containing the QueryByXPath panel and the XmlEditor component
		Container container = new Container();
		container.setLayout(new BorderLayout());
		container.add(new QueryByXPathPanel(xmlEditor, attrPanel), BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);

		// create first split pane (xmlEditor - nodeInsertionPanel)
		JSplitPane splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, container, nodeInsertionPanel);
		splitPane1.setResizeWeight(1); // xml content editor gets extra space
		splitPane1.setDividerLocation(-1);

		// create second splitpane (first split pane - attributesPanel)
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane1, nodeDetailsPanel);
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

	public ISchema getSchema()
	{
		return schema;
	}

	/**
	 * Removes event listeners.
	 */
	public void dispose()
	{
		xmlEditor.dispose();
		nodeInsertionPanel.dispose();
		nodeDetailsPanel.dispose();
	}
}
