package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.config.PolloConfiguration;
import org.outerj.pollo.Pollo;
import org.outerj.pollo.config.XPathQuery;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.List;

import org.jaxen.dom.XPath;
import org.jaxen.SimpleNamespaceContext;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;

public class QueryByXPathPanel extends JPanel implements ActionListener
{
	protected XmlEditor xmlEditor;
	protected AttributesPanel attributesPanel;

	protected JTextField xpathTextField;
	protected JButton nextButton, prevButton;
	protected JLabel progress;

	protected int currentResult;
	protected List resultList;

	public QueryByXPathPanel(XmlEditor xmlEditor, AttributesPanel attributesPanel)
	{
		this.xmlEditor = xmlEditor;
		this.attributesPanel = attributesPanel;

		// add actions to xmleditor
		ActionMap actionMap = xmlEditor.getActionMap();
		actionMap.put("next-xpath-result", new AbstractAction()
				{
					public void actionPerformed(ActionEvent event)
					{
						jump(true);
					}
				});
		actionMap.put("prev-xpath-result", new AbstractAction()
				{
					public void actionPerformed(ActionEvent event)
					{
						jump(false);
					}
				});

		// construct the gui
		setLayout(new BorderLayout());

		Box box = new Box(BoxLayout.X_AXIS);

		JLabel label = new JLabel("XPath query:");
		box.add(label);

		xpathTextField = new EnterTextField();
		Dimension dimension = xpathTextField.getPreferredSize();
		dimension.width = Integer.MAX_VALUE;
		xpathTextField.setMaximumSize(dimension);
		box.add(xpathTextField);

		JButton executeButton = new JButton("Execute");
		executeButton.setActionCommand("execute");
		executeButton.addActionListener(this);
		box.add(executeButton);

		JButton insertExampleButton = new JButton("?");
		insertExampleButton.setToolTipText("Insert example query");
		insertExampleButton.setActionCommand("insert-example");
		insertExampleButton.addActionListener(this);
		box.add(insertExampleButton);

		prevButton = new JButton("<");
		prevButton.setActionCommand("prevResult");
		prevButton.addActionListener(this);
		prevButton.setEnabled(false);
		box.add(prevButton);

		progress = new JLabel("");
		box.add(progress);

		nextButton = new JButton(">");
		nextButton.setActionCommand("nextResult");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);
		box.add(nextButton);

		this.add(box, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("execute"))
		{
			XPath xpath;
			try
			{
				xpath = new XPath(xpathTextField.getText());
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(getTopLevelAncestor()
						, "Could not parse XPath expression: " + e.getMessage()
						, "Error", JOptionPane.ERROR_MESSAGE);
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);
				progress.setText("");
				resultList = null;
				return;
			}

			try
			{
				// as context for resolving namespace prefixes, the root node displayed
				// in the XmlEditor widget is used
				SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
				namespaceContext.addElementNamespaces(xpath.getNavigator(), xmlEditor.getRootElement() instanceof Document ? xmlEditor.getXmlModel().getDocument().getDocumentElement() : xmlEditor.getRootElement());
				xpath.setNamespaceContext(namespaceContext);

				resultList = xpath.selectNodes(xmlEditor.getRootElement());
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(getTopLevelAncestor()
						, "Could not execute XPath expression: " + e.getMessage()
						, "Error", JOptionPane.ERROR_MESSAGE);
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);
				progress.setText("");
				resultList = null;
				return;
			}

			if (resultList == null || resultList.size() == 0)
			{
				JOptionPane.showMessageDialog(getTopLevelAncestor()
						, "This XPath query returned no result"
						, "XPath", JOptionPane.INFORMATION_MESSAGE);
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);
				progress.setText("");
				resultList = null;
				return;
			}

			if(!(resultList.get(0) instanceof Node))
			{
				JOptionPane.showMessageDialog(getTopLevelAncestor()
						, "Result of the query: " + resultList.get(0).toString()
						, "XPath", JOptionPane.INFORMATION_MESSAGE);
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);
				progress.setText("");
				resultList = null;
				return;
			}

			currentResult = -1;
			prevButton.setEnabled(true);
			nextButton.setEnabled(true);
			jump(true);
			xmlEditor.requestFocus();
		}
		else if (event.getActionCommand().equals("prevResult"))
		{
			jump(false);
			xmlEditor.requestFocus();
		}
		else if (event.getActionCommand().equals("nextResult"))
		{
			jump(true);
			xmlEditor.requestFocus();
		}
		else if (event.getActionCommand().equals("insert-example"))
		{
			chooseExample();
		}
	}

	/**
	 * @param next if true jump to the next node in the result list, if
	 * false jump to the previous node in the result list.
	 */
	public void jump(boolean next)
	{
		if (resultList == null)
			return;

		if (next)
		{
			if (currentResult < resultList.size() - 1)
				currentResult++;
		}
		else
		{
			if (currentResult > 0)
				currentResult--;
		}

		progress.setText((currentResult + 1) + "/" + resultList.size());
		Object object = resultList.get(currentResult);
		if (!(object instanceof Node))
		{
			JOptionPane.showMessageDialog(getTopLevelAncestor()
					, "The current item in the list is not a node: " + object.toString()
					, "XPath", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Node node = (Node)object;
		if (!xmlEditor.isNodeTypeSupported(node.getNodeType()) && !(node instanceof Attr))
		{
			JOptionPane.showMessageDialog(getTopLevelAncestor()
					, "The current item in the list is not a node I can jump to: " + object.toString()
					, "XPath", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		View view;
		if (node instanceof Attr)
			view = (View)xmlEditor.getRootView().findNode(((Attr)node).getOwnerElement());
		else
			view = (View)xmlEditor.getRootView().findNode(node);

		if (view == null)
		{
			JOptionPane.showMessageDialog(getTopLevelAncestor()
					, "The current item in the list could not be found in the view. Maybe you deleted it since executing the query?\n\n"
					+ node.toString()
					, "XPath", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		view.assureVisibility(false);
		int startV = view.getVerticalPosition();
		int startH = view.getHorizontalPosition();
		view.markAsSelected(startH, startV);
		
		if (next)
			xmlEditor.scrollAlignBottom(startV, view.getHeight());
		else
			xmlEditor.scrollAlignTop(startV, view.getHeight());

		if (node instanceof Attr)
		{
			Attr attr = (Attr)node;
			boolean found = attributesPanel.highlightAttribute(attr.getNamespaceURI(), attr.getLocalName());

			if (!found)
			{
				JOptionPane.showMessageDialog(getTopLevelAncestor()
						, "The current item in the list is an attribute that could not be found: \n\n"
						+ node.toString()
						, "XPath", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
	}


	public void chooseExample()
	{
		PolloConfiguration configuration = Pollo.getInstance().getConfiguration();
		Object[] examples = configuration.getXPathQueries().toArray();
		Object selected = examples.length > 0 ? examples[0] : null;
		XPathQuery query = (XPathQuery)JOptionPane.showInputDialog(getTopLevelAncestor(), 
				"Choose an example", "Sample XPath queries",
				JOptionPane.QUESTION_MESSAGE, null,
				examples, selected);

		if (query != null)
		{
			xpathTextField.setText(query.getExpression());
		}
	}

	public class EnterTextField extends JTextField
	{
		public void processKeyEvent(KeyEvent event)
		{
			if (event.getKeyCode() == KeyEvent.VK_ENTER)
			{
				ActionEvent e = new ActionEvent(xpathTextField, 0, "execute");
				QueryByXPathPanel.this.actionPerformed(e);
			}
			else
			{
				super.processKeyEvent(event);
			}
		}
	}
}
