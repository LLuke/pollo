package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.xmleditor.attreditor.AttributesTableModel;
import org.outerj.pollo.xmleditor.util.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A JLabel that displays the XPath-path to the currently selected node.
 * To achieve this goal, it registers itself as selection event listener
 * on the XmlEditor and on the JTable containing the attributes.
 *
 * @author Bruno Dumon
 */
public class NodePathBar extends JLabel implements SelectionListener, ListSelectionListener
{
	protected JTable attributesTable;
	protected AttributesTableModel attrTableModel;

	public NodePathBar(XmlEditor xmlEditor, AttributesPanel attrPanel)
	{
		super("Welcome to Pollo!");

		xmlEditor.getSelectionInfo().addListener(this);
		attrPanel.getAttributesTable().getSelectionModel().addListSelectionListener(this);

		attributesTable = attrPanel.getAttributesTable();
		attrTableModel = attrPanel.getAttributesTableModel();
	}

	public void nodeUnselected(Node node)
	{
		setText("No node selected.");
	}

	public void nodeSelected(Node node)
	{
		setText(getPath(node));
	}

	public String getPath(Node node)
	{
		if (node.getNodeType() ==  Node.DOCUMENT_NODE)
			return "/";

		StringBuffer path = new StringBuffer();

		switch (node.getNodeType())
		{
			case Node.COMMENT_NODE:
				path.append("/comment()");
				break;
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				path.append("/text()");
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				path.append("/processing-instruction()");
				break;
			case Node.ELEMENT_NODE:
				path.append("/" + DomUtils.getQName((Element)node));
				break;
		}

		Node parent = node.getParentNode();
		while(parent.getNodeType() == Node.ELEMENT_NODE)
		{
			path.insert(0, "/" + DomUtils.getQName((Element)parent));
			parent = parent.getParentNode();
		}

		return path.toString();
	}

	public void valueChanged(ListSelectionEvent event)
	{
		int row = attributesTable.getSelectedRow();
		if (row != -1)
		{
			String path = getPath(attrTableModel.getElement());
			path += "/@" + attrTableModel.getValueAt(row, 0);
			setText(path);
		}
	}
}
