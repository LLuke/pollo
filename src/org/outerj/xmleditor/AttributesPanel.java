package org.outerj.xmleditor;

import org.outerj.xmleditor.model.XmlModel;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.table.TableColumn;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;

import org.w3c.dom.Element;

/**
  A Panel containing a JTable to edit attribute values, together with
  buttons to add/delete attributes and to apply changes.
  <p>
  The JTable uses an {@link org.outerj.xmleditor.AttributesTableModel AttributesTableModel}
  to show the attributes.

  @author Bruno Dumon
 */
public class AttributesPanel extends JPanel implements ActionListener, ElementClickedListener, EventListener
{
	protected JTable attributesTable;
	protected AttributesTableModel attributesTableModel;
	protected JButton applyButton;
	protected JButton addAttrButton;
	protected JButton deleteAttrButton;
	protected XmlModel xmlModel;
	protected Schema schema;

	public AttributesPanel(XmlModel xmlModel, Schema schema)
	{
		this.xmlModel = xmlModel;
		this.schema = schema;

		// construct the interface components
		attributesTableModel = new AttributesTableModel(schema);
		attributesTable = new AttributesTable(attributesTableModel, schema);

		TableColumn column = null;
		column = attributesTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(40);
		column = attributesTable.getColumnModel().getColumn(1);
		column.setPreferredWidth(300);

		JScrollPane scrollPane = new JScrollPane(attributesTable);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		applyButton = new JButton("Apply changes");
		applyButton.setActionCommand("apply");
		applyButton.addActionListener(this);

		addAttrButton = new JButton("Add attribute...");
		addAttrButton.setActionCommand("add");
		addAttrButton.addActionListener(this);

		deleteAttrButton = new JButton("Delete attribute");
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
		gridbag.setConstraints(applyButton, gbc);

		rightPanel.add(addAttrButton);
		rightPanel.add(deleteAttrButton);
		rightPanel.add(applyButton);

		setEnabled(false);
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("add"))
		{
			String newAttrName = JOptionPane.showInputDialog(getContainingFrame(),
					"Please enter the (optionally qualified) attribute name:", "New attribute",
					JOptionPane.QUESTION_MESSAGE);

			if (newAttrName == null || newAttrName.trim().equals(""))
			{
				return;
			}
			if (!org.apache.xerces.dom.DocumentImpl.isXMLName(newAttrName))
			{
				JOptionPane.showMessageDialog(getContainingFrame(),
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
					JOptionPane.showMessageDialog(getContainingFrame(),
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
		else if (e.getActionCommand().equals("apply"))
		{
			TableCellEditor cellEditor = attributesTable.getCellEditor();
			if (cellEditor != null)
			{
				cellEditor.stopCellEditing();
			}
			attributesTableModel.applyChanges();
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

	public void elementClicked(ElementClickedEvent e)
	{
		TableCellEditor cellEditor = attributesTable.getCellEditor();
		if (cellEditor != null)
		{
			cellEditor.stopCellEditing();
		}

		if (attributesTableModel.isChanged())
		{
			if (JOptionPane.showConfirmDialog(getContainingFrame(),
					"You changed attributes without clicking the apply button. Apply those Changes now?",
					"Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				attributesTableModel.applyChanges();
			}
		}

		// The purpose of the DomNodeRemoved listener is so that we can disable the attributes
		// editor in case the element would be removed
		Element oldElement = attributesTableModel.getElement();
		if (oldElement != null)
			((EventTarget)oldElement).removeEventListener("DOMNodeRemoved", this, false);

		attributesTableModel.setElement(e.getElement());

		((EventTarget)e.getElement()).addEventListener("DOMNodeRemoved", this, false);
		setEnabled(true);
	}



	/**
	  Extension of JTable. Purpose is to be able to provide other cell editors.
	 */
	public class AttributesTable extends JTable
	{
		protected Schema schema;

		public AttributesTable(AttributesTableModel model, Schema schema)
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
			String [] values = schema.getPossibleAttributeValues(model.getElement(), taei.uri, taei.name);
			if (values != null)
			{
				JComboBox combo = new JComboBox(values);
				combo.setEditable(true);
				return new DefaultCellEditor(combo);
			}
			return super.getCellEditor(row, column);
		}
	}


	/**
	  Handler for DOM events. Since we subscribed only to DOMRemoveNode events,
	  we only get an event when the element is removed of which we are currently
	  showing the attributes, and thus we should disable the attributespanel when
	  we get an event.
	 */
	public void handleEvent(Event e)
	{
		try
		{
			setEnabled(false);
			attributesTableModel.setElement(null);
		}
		catch (Exception exc)
		{
			System.out.println("Error in AttributesPanel.handleEvent: " + e);
		}
	}

	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		attributesTable.setEnabled(enabled);
		applyButton.setEnabled(enabled);
		addAttrButton.setEnabled(enabled);
		deleteAttrButton.setEnabled(enabled);
	}

	protected JFrame getContainingFrame()
	{
		Component parent = this.getParent();

		while (parent != null && !(parent instanceof JFrame))
		{
			parent = parent.getParent();
		}

		return (JFrame)parent;
	}
}
