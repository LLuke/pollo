package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.dialog.ErrorDialog;
import org.outerj.pollo.xmleditor.model.PolloDOMParser;
import org.outerj.pollo.xmleditor.model.Undo;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Comment;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class UncommentAction extends AbstractAction
{
	protected XmlEditor xmlEditor;
	protected int behaviour;

	public UncommentAction(XmlEditor xmlEditor)
	{
		super("Uncomment");

		this.xmlEditor = xmlEditor;
	}

	public void actionPerformed(ActionEvent event)
	{
		Node selectedNode = xmlEditor.getSelectedNode();
		
		if (selectedNode.getParentNode()  instanceof Document)
		{
			JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
					"Sorry, uncomment is not supported at this place.");	
			return;
		}

		String data = ((Comment)selectedNode).getData();

		// put the data inside a wrapper tag. This wrapper tag declares all the namespaces known
		// at this location in the document.
		StringBuffer wrapperTag = new StringBuffer();
		wrapperTag.append("<wrapper ");
		HashMap namespaceDeclarations = xmlEditor.getXmlModel().findNamespaceDeclarations((Element)selectedNode.getParentNode());
		Iterator it = namespaceDeclarations.entrySet().iterator();
		while (it.hasNext())
		{
			Entry entry = (Entry)it.next();
			wrapperTag.append("xmlns:");
			wrapperTag.append(entry.getKey());
			wrapperTag.append("='");
			// FIXME this value should probably be checked for problematic characters (such as &)
			wrapperTag.append(entry.getValue());
			wrapperTag.append("' ");
		}
		wrapperTag.append(">");

		StringReader commentReader = new StringReader(wrapperTag.toString() + data + "</wrapper>");
		Document parsedComment = null;

		try
		{
			/* JAXP
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			parsedComment = documentBuilder.parse(new InputSource(commentReader));
			*/
			PolloDOMParser parser = new PolloDOMParser();
			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion",false);
			parser.setFeature("http://xml.org/sax/features/namespaces", true);
			parser.parse(new InputSource(commentReader));
			parsedComment = parser.getDocument();
		}
		catch (Exception e)
		{
			ErrorDialog errorDialog = new ErrorDialog((Frame)xmlEditor.getTopLevelAncestor(),
					"Could not parse the contents of this comment.", e);
			errorDialog.show();
			return;
		}

		Element parent = (Element)selectedNode.getParentNode();

		Undo undo = xmlEditor.getXmlModel().getUndo();
		undo.startUndoTransaction("Uncomment");

		NodeList newNodes = parsedComment.getDocumentElement().getChildNodes();
		Document document = xmlEditor.getXmlModel().getDocument();
		for (int i = 0; i < newNodes.getLength(); i++)
		{
			// currently only element and comment nodes are supported.
			if (XmlEditor.isNodeTypeSupported(newNodes.item(i).getNodeType()))
			{
				Node newNode = document.importNode(newNodes.item(i), true);
				parent.insertBefore(newNode, selectedNode);
			}
		}

		parent.removeChild(selectedNode);
		undo.endUndoTransaction();
	}
}
