package org.outerj.pollo.xmleditor.attreditor;

import org.outerj.pollo.DomConnected;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.util.FocusBorder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Panel containing a JTable to edit attribute values, together with
 * buttons to add/delete attributes.
 * <p>
 * The JTable uses an {@link org.outerj.pollo.xmleditor.AttributesTableModel AttributesTableModel}
 * to show the attributes.
 *
 * @author Bruno Dumon
 */
public class AttributesPanel extends JPanel implements ActionListener, SelectionListener, DomConnected
{
	protected JTable attributesTable;
	protected AttributesTableModel attributesTableModel;
	protected AttributeTableCellRenderer attributeTableCellRenderer = new AttributeTableCellRenderer();
	protected JButton addAttrButton;
	protected JButton deleteAttrButton;
	protected XmlModel xmlModel;
	protected ISchema schema;
	protected IAttributeEditorPlugin attrEditorPlugin;

	public AttributesPanel(XmlModel xmlModel, ISchema schema, IAttributeEditorPlugin attrEditorPlugin)
	{
		this.xmlModel = xmlModel;
		this.schema = schema;
		this.attrEditorPlugin = attrEditorPlugin;

		// construct the interface components
		attributesTableModel = new AttributesTableModel(schema, xmlModel);
		attributesTable = new AttributesTable(attributesTableModel, schema);

		attributesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn column = null;
		column = attributesTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(40);
		column = attributesTable.getColumnModel().getColumn(1);
		column.setPreferredWidth(300);

		JScrollPane scrollPane = new JScrollPane(attributesTable);
		attributesTable.addFocusListener(new FocusBorder(scrollPane));

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		addAttrButton = new JButton("Add attribute...");
		addAttrButton.setActionCommand("add");
		addAttrButton.addActionListener(this);

		deleteAttrButton = new JButton("Clear value");
		deleteAttrButton.setActionCommand("delete");
		deleteAttrButton.addActionListener(this);

		JPanel rightPanel = new JPanel();
		add(rightPanel, BorderLayout.EAST);
		GridBagLayout gridbag = new GridBagLayout();
		rightPanel.setLayout(gridbag);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		gridbag.setConstraints(addAttrButton, gbc);
		gridbag.setConstraints(deleteAttrButton, gbc);

		rightPanel.add(addAttrButton);
		rightPanel.add(deleteAttrButton);

		setEnabled(false);
	}

	public void actionPerformed(ActionEvent e)
	{
		TableCellEditor cellEditor = attributesTable.getCellEditor();
		if (cellEditor != null)
		{
			cellEditor.stopCellEditing();
		}

		if (e.getActionCommand().equals("add"))
		{
			String newAttrName = JOptionPane.showInputDialog(getTopLevelAncestor(),
					"Please enter the (optionally qualified) attribute name:", "New attribute",
					JOptionPane.QUESTION_MESSAGE);

			if (newAttrName == null || newAttrName.trim().equals(""))
			{
				return;
			}
			if (!org.apache.xerces.dom.DocumentImpl.isXMLName(newAttrName))
			{
				JOptionPane.showMessageDialog(getTopLevelAncestor(),
					"That is not a valid XML attribute name.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			int prefixpos = newAttrName.indexOf(":");
			String prefix = null;
			String localName = null;
			String ns = null;
			if (prefixpos != -1)
			{
				prefix = newAttrName.substring(0, prefixpos);
				localName = newAttrName.substring(prefixpos + 1, newAttrName.length());
				ns = xmlModel.findNamespaceForPrefix(attributesTableModel.getElement(), prefix);
				if (ns == null && !prefix.equals("xmlns"))
				{
					JOptionPane.showMessageDialog(getTopLevelAncestor(),
							"No namespace declaration found for namespace prefix " + prefix
							+ ". Attribute will not be added.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else
			{
				// oooops... namespace defaulting does not apply to attributes !!
				// ns = xmlModel.findDefaultNamespace(attributesTableModel.getElement());
				localName = newAttrName;
			}

			attributesTableModel.addAttribute(ns, prefix, localName);
		}
		else if (e.getActionCommand().equals("delete"))
		{
			int row = attributesTable.getSelectedRow();
			if (row == -1)
			{
				System.out.println("no attribute selected");
			}
			else
			{
				attributesTableModel.deleteAttribute(row);
			}
		}
	}

	/**
	 * Implementation of the SelectionListener interface.
	 */
	public void nodeSelected(Node node)
	{
		TableCellEditor cellEditor = attributesTable.getCellEditor();
		if (cellEditor != null)
		{
			cellEditor.stopCellEditing();
		}

		if (node instanceof Element)
		{
			attributesTableModel.setElement((Element)node);
			setEnabled(true);
		}
		else
		{
			// if it's not an element node
			attributesTableModel.setElement(null);
			setEnabled(false);
		}

		attributesTable.clearSelection();
	}

	/**
	 * Implementation of the SelectionListener interface.
	 */
	public void nodeUnselected(Node node)
	{
		setEnabled(false);
		attributesTableModel.setElement(null);
		attributesTable.clearSelection();
	}


	/**
	 * Extension of JTable. Purpose is to be able to provide other cell editors.
	 */
	public class AttributesTable extends JTable
	{
		protected ISchema schema;

		public AttributesTable(AttributesTableModel model, ISchema schema)
		{
			super(model);
			this.schema = schema;
		}

		
		public TableCellEditor getCellEditor(int row, int column)
		{
			// if the schema has a list of values for this attribute, show that,
			// or otherwise show the default cell editor.
			AttributesTableModel model = (AttributesTableModel)getModel();
			AttributesTableModel.TempAttrEditInfo taei = model.getTempAttrEditInfo(row);
			TableCellEditor editor = attrEditorPlugin.getAttributeEditor(model.getElement(), taei.uri, taei.name);
			if (editor != null)
				return editor;
			else
				return super.getCellEditor(row, column);

			/*
			String [] values = schema.getPossibleAttributeValues(model.getElement(), taei.uri, taei.name);
			if (values != null)
			{
				JComboBox combo = new JComboBox(values);
				combo.setEditable(true);
				return new DefaultCellEditor(combo);
			}
			*/
		}

		public TableCellRenderer getCellRenderer(int row, int column)
		{
			return attributeTableCellRenderer;
		}

	}


	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		attributesTable.setEnabled(enabled);
		addAttrButton.setEnabled(enabled);
		deleteAttrButton.setEnabled(enabled);
	}

	public void requestFocus()
	{
		attributesTable.requestFocus();
	}

	public boolean highlightAttribute(String namespaceURI, String localName)
	{
		int maxAttrs = attributesTableModel.getRowCount();
		for (int i = 0; i < maxAttrs; i++)
		{
			AttributesTableModel.TempAttrEditInfo taei = attributesTableModel.getTempAttrEditInfo(i);
			if (((taei.uri == null && namespaceURI == null)
						|| (namespaceURI != null && namespaceURI.equals(taei.uri)))
					&& taei.name.equals(localName))
			{
				attributesTable.changeSelection(i, 0, false, false);
				return true;
			}
		}
		return false;
	}

	public JTable getAttributesTable()
	{
		return attributesTable;
	}

	public AttributesTableModel getAttributesTableModel()
	{
		return attributesTableModel;
	}

	public void disconnectFromDom()
	{
		attributesTableModel.disconnectFromDom();
	}

	public void reconnectToDom()
	{
		attributesTableModel.reconnectToDom();
	}
}
