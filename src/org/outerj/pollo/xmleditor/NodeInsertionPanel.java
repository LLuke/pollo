package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.DisplaySpecification.ElementSpec;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.Schema;
import org.outerj.pollo.xmleditor.util.QuickSort;
import org.outerj.pollo.xmleditor.util.DomUtils;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Iterator;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NodeInsertionPanel extends JPanel
{
	protected XmlEditorPanel xmlEditorPanel;
	protected Schema schema;
	protected DisplaySpecification displaySpec;
	protected Object [] emptyArray = new Object[0];

	public NodeInsertionPanel(XmlEditorPanel xmlEditorPanel)
	{
		this.xmlEditorPanel = xmlEditorPanel;
		this.schema = xmlEditorPanel.getSchema(); // cache reference to schema
		XmlEditor xmlEditor = xmlEditorPanel.getXmlEditor();
		this.displaySpec = xmlEditor.getDisplaySpec();

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints listConstraints = new GridBagConstraints();
		listConstraints.fill = GridBagConstraints.BOTH;
		listConstraints.gridwidth = GridBagConstraints.REMAINDER;
		listConstraints.weightx = 1;
		listConstraints.weighty = 1;
		
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridwidth = GridBagConstraints.REMAINDER;

		JLabel label;
		Font labelFont = new Font("Default", 0, 10);
		label = new JLabel("Insert before:");
		label.setFont(labelFont);
		layout.setConstraints(label, labelConstraints);
		add(label);

		NodeInsertionList insertBeforeList = new NodeInsertionList(NodeInsertionList.MODE_INSERT_BEFORE);
		xmlEditor.addNodeClickedListener(insertBeforeList);
		insertBeforeList.setCellRenderer(new ElementSpecCellRenderer());
		JScrollPane scrollPane1 = new JScrollPane(insertBeforeList);
		layout.setConstraints(scrollPane1, listConstraints);
		add(scrollPane1);

		label = new JLabel("Insert after:");
		label.setFont(labelFont);
		layout.setConstraints(label, labelConstraints);
		add(label);

		NodeInsertionList insertAfterList = new NodeInsertionList(NodeInsertionList.MODE_INSERT_AFTER);
		xmlEditor.addNodeClickedListener(insertAfterList);
		insertAfterList.setCellRenderer(new ElementSpecCellRenderer());
		JScrollPane scrollPane2 = new JScrollPane(insertAfterList);
		layout.setConstraints(scrollPane2, listConstraints);
		add(scrollPane2);

		label = new JLabel("Append child:");
		label.setFont(labelFont);
		layout.setConstraints(label, labelConstraints);
		add(label);

		NodeInsertionList insertInsideList = new NodeInsertionList(NodeInsertionList.MODE_INSERT_INSIDE);
		xmlEditor.addNodeClickedListener(insertInsideList);
		insertInsideList.setCellRenderer(new ElementSpecCellRenderer());
		JScrollPane scrollPane3 = new JScrollPane(insertInsideList);
		layout.setConstraints(scrollPane3, listConstraints);
		add(scrollPane3);
	}

	public class NodeInsertionList extends JList implements NodeClickedListener
	{
		public static final int MODE_INSERT_BEFORE = 1;
		public static final int MODE_INSERT_AFTER  = 2;
		public static final int MODE_INSERT_INSIDE = 3;
		protected int mode;
		protected Node node;

		public NodeInsertionList(int mode)
		{
			this.mode = mode;
			this.addMouseListener(new DoubleClickListener());
		}

		public void nodeClicked(NodeClickedEvent nce)
		{
			this.node = nce.getNode();
			switch (mode)
			{
				case MODE_INSERT_BEFORE:
				case MODE_INSERT_AFTER:
					{
						if (node == xmlEditorPanel.getXmlEditor().getRootElement())
						{
							this.setListData(emptyArray);
						}
						else
						{
							Collection subElementsList = schema.getAllowedSubElements((Element)node.getParentNode());
							Iterator subElementsIt = subElementsList.iterator();
							ElementSpec [] data = new ElementSpec[subElementsList.size()];
							int i = 0;
							while (subElementsIt.hasNext())
							{
								Schema.SubElement subElement = (Schema.SubElement)subElementsIt.next();
								data[i] = displaySpec.getElementSpec(subElement.namespaceURI, subElement.localName);
								i++;
							}
							QuickSort quickSort = new QuickSort();
							quickSort.sort(data);
							this.setListData(data);
						}
						break;
					}
				case MODE_INSERT_INSIDE:
					{
						if (node instanceof Element)
						{
							Collection subElementsList = schema.getAllowedSubElements((Element)node);
							Iterator subElementsIt = subElementsList.iterator();
							ElementSpec [] data = new ElementSpec[subElementsList.size()];
							int i = 0;
							while (subElementsIt.hasNext())
							{
								Schema.SubElement subElement = (Schema.SubElement)subElementsIt.next();
								data[i] = displaySpec.getElementSpec(subElement.namespaceURI, subElement.localName);
								i++;
							}
							QuickSort quickSort = new QuickSort();
							quickSort.sort(data);
							this.setListData(data);
						}
						else
						{
							this.setListData(emptyArray);
						}
						break;
					}
			}
		}

		public class DoubleClickListener extends MouseAdapter
		{
			public void mousePressed(MouseEvent e)
			{
				// doubleclick detection does not work -- at least on linux
				//if (e.getClickCount() == 2)
				//{
				//}
				int index = locationToIndex(e.getPoint());
				if (index != -1)
				{
					XmlModel xmlModel = xmlEditorPanel.getXmlModel();
					Node selectedNode = xmlEditorPanel.getXmlEditor().getSelectedNode();

					// create the new element
					ElementSpec elementSpec = (ElementSpec)getModel().getElementAt(index);
					Element newElement;
					if (elementSpec.nsUri != null && elementSpec.nsUri.length() > 0)
					{
						// search for a namespace declaration
						Element namespaceSearchNode = null;
						switch (mode)
						{
							case MODE_INSERT_BEFORE:
							case MODE_INSERT_AFTER:
								namespaceSearchNode = (Element)selectedNode.getParentNode();
								break;
							case MODE_INSERT_INSIDE:
								namespaceSearchNode = (Element)selectedNode;

						}
						String prefix = xmlModel.findPrefixForNamespace(namespaceSearchNode, elementSpec.nsUri);
						if (prefix == null)
						{
							JOptionPane.showMessageDialog(getTopLevelAncestor(),
									"No prefix declaration found for namespace '" + elementSpec.nsUri + "'");
							return;
						}
						newElement = xmlModel.getDocument().createElementNS(elementSpec.nsUri,
								DomUtils.getQName(prefix, elementSpec.localName));
					}
					else
					{
						newElement = xmlModel.getDocument().createElementNS(null, elementSpec.localName);
					}

					switch (mode)
					{
						case MODE_INSERT_BEFORE:
								selectedNode.getParentNode().insertBefore(newElement, selectedNode);
								break;
						case MODE_INSERT_AFTER:
								Node nextSibling = selectedNode.getNextSibling();
								if (nextSibling != null)
									selectedNode.getParentNode().insertBefore(newElement, nextSibling);
								else
									selectedNode.getParentNode().appendChild(newElement);
								break;
						case MODE_INSERT_INSIDE:
								selectedNode.appendChild(newElement);
								break;
					}
				}
			}
		}

	}

	public class ElementSpecCellRenderer extends JLabel implements ListCellRenderer
	{
		public Component getListCellRendererComponent(
				JList list,
				Object value,            // value to display
				int index,               // cell index
				boolean isSelected,      // is the cell selected
				boolean cellHasFocus)    // the list and the cell have the focus
		{
			ElementSpec elementSpec = (ElementSpec)value;
			setText(elementSpec.localName);
			setIcon(elementSpec.icon);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			return this;
		}
	}
}
