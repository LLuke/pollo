package org.outerj.smeditor.compeditor;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;

import org.w3c.dom.Element;
import org.outerj.xmleditor.model.XmlModel;

public class ComponentsEditor extends JPanel implements ActionListener
{
	protected JTable componentsTable;
	protected ComponentsTableModel componentsTableModel;
	protected Element componentsEl;
	protected String componentsName;
	protected String componentsURI;
	protected XmlModel xmlModel;

	public ComponentsEditor(Element componentsEl, String componentsName, String componentsURI,
			String classColumn, boolean labels, XmlModel xmlModel)
	{
		this.componentsEl = componentsEl;
		this.componentsName = componentsName;
		this.componentsURI = componentsURI;
		this.xmlModel = xmlModel;

		//
		// create the table containing the components listing
		//

		componentsTableModel = new ComponentsTableModel(componentsEl, componentsName, componentsURI,
				classColumn, labels, xmlModel);
		componentsTable = new JTable(componentsTableModel);

		// set column widths
		TableColumn column = null;
		column = componentsTable.getColumnModel().getColumn(0);
		column.setMaxWidth(30);
		column = componentsTable.getColumnModel().getColumn(1);
		column.setPreferredWidth(30);
		if (labels)
		{
			column = componentsTable.getColumnModel().getColumn(2);
			column.setPreferredWidth(30);
			column = componentsTable.getColumnModel().getColumn(3);
			column.setPreferredWidth(300);
		}
		else
		{
			column = componentsTable.getColumnModel().getColumn(2);
			column.setPreferredWidth(300);
		}

		componentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		componentsTable.setSelectionMode(0); //JList.SINGLE_SELECTION);

		JScrollPane scrollPane = new JScrollPane(componentsTable);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		//
		// create action buttons
		//
		JPanel actionButtonsPanel = new JPanel();
		actionButtonsPanel.setLayout(new BoxLayout(actionButtonsPanel, BoxLayout.X_AXIS));

		JButton makeDefaultButton = new JButton("Make default");
		makeDefaultButton.setActionCommand("makedefault");
		makeDefaultButton.addActionListener(this);

		JButton addNewButton = new JButton("Add new...");
		addNewButton.setActionCommand("addnew");
		addNewButton.addActionListener(this);

		JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(this);

		actionButtonsPanel.add(makeDefaultButton);
		actionButtonsPanel.add(addNewButton);
		actionButtonsPanel.add(removeButton);

		add(actionButtonsPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("makedefault"))
		{
			int row = componentsTable.getSelectedRow();
			if (row == -1)
			{
				System.out.println("No row selected.");
				return;
			}
			String name = componentsTableModel.getElementOnRow(row).getAttribute("name");
			componentsEl.setAttribute("default", name);
		}
		else if (event.getActionCommand().equals("addnew"))
		{
			String name = JOptionPane.showInputDialog("Please enter the name for the new " + componentsName);
			if (name != null && name.trim().length() > 0)
			{
				Element newCompEl = xmlModel.getDocument().createElementNS(componentsURI, componentsName);
				newCompEl.setPrefix(xmlModel.findPrefixForNamespace(componentsEl, componentsURI));
				newCompEl.setAttribute("name", name);
				componentsEl.appendChild(newCompEl);
			}
		}
		else if (event.getActionCommand().equals("remove"))
		{
			// FIXME first check if this type of component is still used somewhere
			// FIXME check if this component is the default one
			int row = componentsTable.getSelectedRow();
			if (row == -1)
			{
				System.out.println("No row selected.");
				return;
			}
			Element selectedEl = componentsTableModel.getElementOnRow(row);
			Element parent = (Element)selectedEl.getParentNode();
			parent.removeChild(selectedEl);
		}
	}
}
