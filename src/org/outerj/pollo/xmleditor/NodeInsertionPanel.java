package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.displayspec.ElementSpec;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.schema.ElementSchema;
import org.outerj.pollo.xmleditor.util.QuickSort;
import org.outerj.pollo.xmleditor.util.DomUtils;
import org.outerj.pollo.xmleditor.util.FocusBorder;
import org.outerj.pollo.DomConnected;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import java.awt.BorderLayout;

import java.util.Iterator;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NodeInsertionPanel extends JPanel implements DomConnected
{
	protected XmlEditorPanel xmlEditorPanel;
	protected ISchema schema;
	protected IDisplaySpecification displaySpec;
	protected Object [] emptyArray = new Object[0];
	protected InsertUnlistedElement insertUnlistedElement = new InsertUnlistedElement();
	protected QuickSort quickSort = new QuickSort();

	protected NodeInsertionList insertBeforeList;
	protected NodeInsertionList insertAfterList;
	protected NodeInsertionList insertInsideList;

	public NodeInsertionPanel(XmlEditorPanel xmlEditorPanel)
	{
		this.xmlEditorPanel = xmlEditorPanel;
		this.schema = xmlEditorPanel.getSchema(); // cache reference to schema
		XmlEditor xmlEditor = xmlEditorPanel.getXmlEditor();
		this.displaySpec = xmlEditor.getDisplaySpec();

		setLayout(new BorderLayout());
		Box box = new Box(BoxLayout.Y_AXIS);
		Dimension dimension;

		JLabel label;
		Font labelFont = new Font("Default", 0, 10);

		label = new JLabel("Insert before:");
		label.setFont(labelFont);
		box.add(label);

		insertBeforeList = new NodeInsertionList(NodeInsertionList.MODE_INSERT_BEFORE);
		xmlEditor.getSelectionInfo().addListener(insertBeforeList);
		insertBeforeList.setCellRenderer(new ElementSpecCellRenderer());
		JScrollPane scrollPane1 = new JScrollPane(insertBeforeList);
		insertBeforeList.addFocusListener(new FocusBorder(scrollPane1));
		dimension = insertBeforeList.getPreferredSize();
		dimension.width = Integer.MAX_VALUE;
		insertBeforeList.setMaximumSize(dimension);
		box.add(scrollPane1);

		label = new JLabel("Insert after:");
		label.setFont(labelFont);
		box.add(label);

		insertAfterList = new NodeInsertionList(NodeInsertionList.MODE_INSERT_AFTER);
		xmlEditor.getSelectionInfo().addListener(insertAfterList);
		insertAfterList.setCellRenderer(new ElementSpecCellRenderer());
		JScrollPane scrollPane2 = new JScrollPane(insertAfterList);
		insertAfterList.addFocusListener(new FocusBorder(scrollPane2));
		dimension = insertAfterList.getPreferredSize();
		dimension.width = Integer.MAX_VALUE;
		insertAfterList.setMaximumSize(dimension);
		box.add(scrollPane2);

		label = new JLabel("Append child:");
		label.setFont(labelFont);
		box.add(label);

		insertInsideList = new NodeInsertionList(NodeInsertionList.MODE_INSERT_INSIDE);
		xmlEditor.getSelectionInfo().addListener(insertInsideList);
		insertInsideList.setCellRenderer(new ElementSpecCellRenderer());
		JScrollPane scrollPane3 = new JScrollPane(insertInsideList);
		insertInsideList.addFocusListener(new FocusBorder(scrollPane3));
		dimension = insertInsideList.getPreferredSize();
		dimension.width = Integer.MAX_VALUE;
		insertInsideList.setMaximumSize(dimension);
		box.add(scrollPane3);

		add(box, BorderLayout.CENTER);
	}

	public void activateInsertBefore()
	{
		if (insertBeforeList.getSelectedIndex() == -1)
		{
			insertBeforeList.setSelectedIndex(0);
		}
		insertBeforeList.requestFocus();
	}

	public void activateInsertAfter()
	{
		if (insertAfterList.getSelectedIndex() == -1)
		{
			insertAfterList.setSelectedIndex(0);
		}
		insertAfterList.requestFocus();
	}

	public void activateInsertInside()
	{
		if (insertInsideList.getSelectedIndex() == -1)
		{
			insertInsideList.setSelectedIndex(0);
		}
		insertInsideList.requestFocus();
	}

	public class NodeInsertionList extends JList implements SelectionListener
	{
		public static final int MODE_INSERT_BEFORE = 1;
		public static final int MODE_INSERT_AFTER  = 2;
		public static final int MODE_INSERT_INSIDE = 3;
		protected int mode;
		protected Node node;
		protected InsertNodeAction insertNodeAction = new InsertNodeAction();

		public NodeInsertionList(int mode)
		{
			this.mode = mode;
			this.addMouseListener(new DoubleClickListener());

			InputMap inputMap = getInputMap();
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "selected");
			inputMap.put(KeyStroke.getKeyStroke('j'), "select-next"); // vi
			inputMap.put(KeyStroke.getKeyStroke('k'), "select-previous"); // vi

			ActionMap actionMap = getActionMap();
			actionMap.put("selected", insertNodeAction);
			actionMap.put("select-next", new AbstractAction()
					{
						public void actionPerformed(ActionEvent event)
						{
							int index = getSelectedIndex();
							if (index < getModel().getSize() - 1)
							{
								setSelectedIndex(index + 1);
								ensureIndexIsVisible(index + 1);
							}
						}
					});
			actionMap.put("select-previous", new AbstractAction()
					{
						public void actionPerformed(ActionEvent event)
						{
							int index = getSelectedIndex();
							if (index > 0)
							{
								setSelectedIndex(index - 1);
								ensureIndexIsVisible(index - 1);
							}
						}
					});
		}

		public void nodeSelected(Node node)
		{
			setEnabled(true);
			this.node = node;
			switch (mode)
			{
				case MODE_INSERT_BEFORE:
				case MODE_INSERT_AFTER:
					{
						if (node == xmlEditorPanel.getXmlEditor().getRootElement() || node.getParentNode() instanceof Document)
						{
							this.setListData(emptyArray);
						}
						else
						{
							Collection subElementsList = schema.getAllowedSubElements((Element)node.getParentNode());
							Iterator subElementsIt = subElementsList.iterator();
							Object [] data = new Object[subElementsList.size() + 1];
							int i = 1;
							while (subElementsIt.hasNext())
							{
								ElementSchema.SubElement subElement = (ElementSchema.SubElement)subElementsIt.next();
								data[i] = displaySpec.getElementSpec(subElement.namespaceURI, subElement.localName);
								i++;
							}
							data[0] = insertUnlistedElement;
							quickSort.sortPartial(data, 1);
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
							Object [] data = new Object[subElementsList.size() + 1];
							int i = 1;
							while (subElementsIt.hasNext())
							{
								ElementSchema.SubElement subElement = (ElementSchema.SubElement)subElementsIt.next();
								data[i] = displaySpec.getElementSpec(subElement.namespaceURI, subElement.localName);
								i++;
							}
							data[0] = insertUnlistedElement;
							quickSort.sortPartial(data, 1);
							this.setListData(data);
						}
						else if (node instanceof Document)
						{
							Object data[] = { insertUnlistedElement };
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

		public void nodeUnselected(Node node)
		{
			setEnabled(false);
		}

		public class DoubleClickListener extends MouseAdapter
		{
			public void mouseClicked(MouseEvent e)
			{
				if (!isEnabled())
					return;

				if (e.getClickCount() == 2)
				{
					int index = locationToIndex(e.getPoint());

					if (index == -1)
						return;

					insertNodeAction.actionPerformed(null);
				}
			}
		}

		public class InsertNodeAction extends AbstractAction
		{
			public void actionPerformed(ActionEvent event)
			{
				XmlModel xmlModel = xmlEditorPanel.getXmlModel();
				Node selectedNode = xmlEditorPanel.getXmlEditor().getSelectedNode();

				Object selectedItem = getModel().getElementAt(getSelectedIndex());
				String localName = null;
				String namespaceURI = null;
				String prefix = null;

				if (selectedItem instanceof ElementSpec)
				{
					ElementSpec elementSpec = (ElementSpec)getModel().getElementAt(getSelectedIndex());
					localName = elementSpec.localName;
					namespaceURI = elementSpec.nsUri;
				}
				else if (selectedItem instanceof InsertUnlistedElement)
				{
					// ask user for the element name
					String elementName = JOptionPane.showInputDialog(getTopLevelAncestor(),
							"Please enter the element name (optionally qualified with a namespace prefix):", "New element", JOptionPane.QUESTION_MESSAGE);
					if (elementName == null || elementName.trim().equals(""))
					{
						return;
					}
					if (!org.apache.xerces.dom.DocumentImpl.isXMLName(elementName))
					{
						JOptionPane.showMessageDialog(getTopLevelAncestor(),
								"That is not a valid XML element name.",
								"Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int prefixpos = elementName.indexOf(":");
					if (prefixpos != -1)
					{
						prefix = elementName.substring(0, prefixpos);
						localName = elementName.substring(prefixpos + 1, elementName.length());
					}
					else
					{
						localName = elementName;
					}
				}

				// determine the element from which to start searching for namespace declarations
				Element namespaceSearchNode = null;
				if (!(selectedNode instanceof Document))
				{
					switch (mode)
					{
						case MODE_INSERT_BEFORE:
						case MODE_INSERT_AFTER:
							namespaceSearchNode = (Element)selectedNode.getParentNode();
							break;
						case MODE_INSERT_INSIDE:
							namespaceSearchNode = (Element)selectedNode;
					}
				}

				Element newElement;
				if (selectedItem instanceof ElementSpec && namespaceURI != null && namespaceURI.length() > 0)
				{
					// the user selected an element from the list, now search for a prefix
					// that matches its namespace
					prefix = xmlModel.findPrefixForNamespace(namespaceSearchNode, namespaceURI);
					if (prefix == null)
					{
						JOptionPane.showMessageDialog(getTopLevelAncestor(),
								"No prefix declaration found for namespace '" + namespaceURI + "'");
						return;
					}
					newElement = xmlModel.getDocument().createElementNS(namespaceURI,
							DomUtils.getQName(prefix, localName));
				}
				else if (selectedItem instanceof InsertUnlistedElement)
				{
					// in case the user entered the element name, we need to search for
					// a namespace associated with the prefix he entered.
					if (prefix != null)
					{
						namespaceURI = xmlModel.findNamespaceForPrefix(namespaceSearchNode, prefix);
						if (namespaceURI == null && !prefix.equals("xmlns"))
						{
							JOptionPane.showMessageDialog(getTopLevelAncestor(),
									"No namespace declaration found for namespace prefix " + prefix
									+ ". Element will not be added.",
									"Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					else
					{
						namespaceURI = xmlModel.findDefaultNamespace(namespaceSearchNode);
					}
					newElement = xmlModel.getDocument().createElementNS(namespaceURI,
							DomUtils.getQName(prefix, localName));
				}
				else
				{
					// the user selected an element without a namespace
					newElement = xmlModel.getDocument().createElementNS(null, localName);
				}

				// insert the newly created node at the apropriate place
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


				xmlEditorPanel.getXmlEditor().requestFocus();
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
			if (value instanceof ElementSpec)
			{
				ElementSpec elementSpec = (ElementSpec)value;
				setText(elementSpec.localName);
				setIcon(elementSpec.icon);
				setFont(list.getFont());
			}
			else if (value instanceof InsertUnlistedElement)
			{
				setText(value.toString());
				setIcon(null);
				Font font = list.getFont().deriveFont(Font.ITALIC);
				setFont(font);
			}
			else
			{
				System.out.println("[NodeInsertionPanel] Unexpected value: " + value);
			}

			setOpaque(true);
			if (isSelected)
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			return this;
		}
	}

	public class InsertUnlistedElement
	{
		public String toString()
		{
			return "Insert unlisted element...";
		}
	}

	public void disconnectFromDom()
	{
	}

	public void reconnectToDom()
	{
		insertBeforeList.setEnabled(false);
		insertAfterList.setEnabled(false);
		insertInsideList.setEnabled(false);
	}
}
