package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.dialog.ErrorDialog;
import org.outerj.pollo.xmleditor.model.Undo;
import org.outerj.pollo.xmleditor.XmlEditor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Document;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

/**
 * A Swing Action to comment out the selected node. This serializes the selected
 * node to XML, removes the node, and creates a new comment node containing the XML.
 *
 * @author Bruno Dumon
 */
public class CommentOutAction extends AbstractAction
{
	protected XmlEditor xmlEditor;

	public CommentOutAction(XmlEditor xmlEditor)
	{
		super("Comment out");

		this.xmlEditor = xmlEditor;
	}

	public void actionPerformed(ActionEvent event)
	{
		Node selectedNode = xmlEditor.getSelectedNode();

		if (selectedNode instanceof Document)
		{
			JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
					"You cannot comment out the XML document itself.");	
			return;
		}
		StringWriter commentWriter = new StringWriter();

		try
		{
			OutputFormat outputFormat = new OutputFormat();
			outputFormat.setIndenting(true);
			outputFormat.setIndent(2);
			outputFormat.setOmitXMLDeclaration(true);

			XMLSerializer serializer = new XMLSerializer(commentWriter, outputFormat);
			serializer.serialize((Element)selectedNode);

			/* JAXP
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("omit-xml-declaration", "yes");
			transformer.transform(new DOMSource(selectedNode), new StreamResult(commentWriter));
			*/
		}
		catch (Exception e)
		{
			ErrorDialog errorDialog = new ErrorDialog((Frame)xmlEditor.getTopLevelAncestor(),
					"Could not serialize the selected node.", e);
			errorDialog.show();
			return;
		}

		Node parent = selectedNode.getParentNode();

		Node newNode = xmlEditor.getXmlModel().getDocument().createComment(commentWriter.toString());

		Undo undo = xmlEditor.getXmlModel().getUndo();
		undo.startUndoTransaction("Comment out node of the node <" + selectedNode.getLocalName() + ">");
		parent.insertBefore(newNode, selectedNode);
		parent.removeChild(selectedNode);
		undo.endUndoTransaction();

	}
}
