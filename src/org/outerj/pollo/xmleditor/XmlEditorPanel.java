package org.outerj.pollo.xmleditor;

import org.outerj.pollo.DomConnected;
import org.outerj.pollo.xmleditor.action.ValidateAction;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.xmleditor.chardataeditor.CharDataPanel;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.util.FocusBorder;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 * An XmlEditorPanel consists of three parts: an XmlEditor (showing the actual XML content),
 * an AttributesPanel for editing attribute values, and a NodeInsertionPanel to insert new
 * elements.
 *
 * @author Bruno Dumon
 */
public class XmlEditorPanel extends JPanel implements DomConnected
{
	protected XmlEditor xmlEditor;
	protected XmlModel xmlModel;
	protected String xpathForRoot;
	protected ISchema schema;
	protected NodeInsertionPanel nodeInsertionPanel;
	protected NodeDetailsPanel nodeDetailsPanel;
	protected JSplitPane xmlEditorAndNodeInsertPanelSplit;
	protected JSplitPane xmlEditorAndValidationErrorsSplit;
	protected Container xpathAndXmlEditorContainer;
	protected ValidationErrorsPanel validationErrorsPanel;
	protected AttributesPanel attrPanel;
	protected ValidateAction validateAction = new ValidateAction(this);

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

		attrPanel = new AttributesPanel(model, schema, attrEditorPlugin);
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
		xpathAndXmlEditorContainer = new Container();
		xpathAndXmlEditorContainer.setLayout(new BorderLayout());
		xpathAndXmlEditorContainer.add(new QueryByXPathPanel(xmlEditor, attrPanel), BorderLayout.NORTH);
		xpathAndXmlEditorContainer.add(scrollPane, BorderLayout.CENTER);

		// create first split pane (xmlEditor - nodeInsertionPanel)
		xmlEditorAndNodeInsertPanelSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, xpathAndXmlEditorContainer, nodeInsertionPanel);
		xmlEditorAndNodeInsertPanelSplit.setResizeWeight(1); // xml content editor gets extra space
		xmlEditorAndNodeInsertPanelSplit.setDividerLocation(620);
		xmlEditorAndNodeInsertPanelSplit.setOneTouchExpandable(true);

		// create second splitpane (first split pane - attributesPanel)
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, xmlEditorAndNodeInsertPanelSplit, nodeDetailsPanel);
		splitPane2.setResizeWeight(1); // xml content editor gets extra space
		splitPane2.setDividerLocation(370);
		splitPane2.setOneTouchExpandable(true);
		add(splitPane2, BorderLayout.CENTER);

		NodePathBar nodePathBar = new NodePathBar(xmlEditor, attrPanel);
		add(nodePathBar, BorderLayout.SOUTH);

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

	public void showValidationErrorsPanel(Collection errors)
	{
		if (xmlEditorAndValidationErrorsSplit == null)
		{
			xmlEditorAndValidationErrorsSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			xmlEditorAndValidationErrorsSplit.setResizeWeight(1); // xml content editor gets extra space
			xmlEditorAndValidationErrorsSplit.setDividerLocation(0.7);
		}

		getValidationErrorsPanel(); // to be sure that the panel gets instantiated

		if (xmlEditorAndNodeInsertPanelSplit.getLeftComponent() != xmlEditorAndValidationErrorsSplit)
		{
			xmlEditorAndNodeInsertPanelSplit.remove(xpathAndXmlEditorContainer);
			xmlEditorAndValidationErrorsSplit.setTopComponent(xpathAndXmlEditorContainer);
			xmlEditorAndValidationErrorsSplit.setBottomComponent(validationErrorsPanel);
			xmlEditorAndNodeInsertPanelSplit.setLeftComponent(xmlEditorAndValidationErrorsSplit);
		}
		validationErrorsPanel.showErrors(errors);
	}

	public void hideValidationErrorsPanel()
	{
		xmlEditorAndNodeInsertPanelSplit.remove(xmlEditorAndValidationErrorsSplit);
		xmlEditorAndValidationErrorsSplit.remove(xpathAndXmlEditorContainer);
		xmlEditorAndNodeInsertPanelSplit.setLeftComponent(xpathAndXmlEditorContainer);
	}

	public ValidateAction getValidateAction()
	{
		return validateAction;
	}

	public ValidationErrorsPanel getValidationErrorsPanel()
	{
		if (validationErrorsPanel == null)
		{
			validationErrorsPanel = new ValidationErrorsPanel(this, attrPanel);
		}
		return validationErrorsPanel;
	}


	/**
	 * Removes event listeners.
	 */
	public void disconnectFromDom()
	{
		xmlEditor.disconnectFromDom();
		nodeInsertionPanel.disconnectFromDom();
		nodeDetailsPanel.disconnectFromDom();
	}

	public void reconnectToDom()
	{
		xmlEditor.reconnectToDom();
		nodeInsertionPanel.reconnectToDom();
		nodeDetailsPanel.reconnectToDom();
	}
}
