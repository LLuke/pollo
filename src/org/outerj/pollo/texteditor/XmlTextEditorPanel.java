package org.outerj.pollo.texteditor;

import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import org.xml.sax.SAXParseException;

public class XmlTextEditorPanel extends JPanel
{
	protected XmlTextEditor xmlTextEditor;
	protected CheckPanel checkPanel;

	public XmlTextEditorPanel(XmlModel xmlModel, ISchema schema)
	{
		xmlTextEditor = new XmlTextEditor();
		xmlTextEditor.setDocument(xmlModel.getTextDocument());
		xmlTextEditor.addExtraKeyBindings();

		setLayout(new BorderLayout());
		add(xmlTextEditor, BorderLayout.CENTER);

		checkPanel = new CheckPanel(xmlModel, xmlTextEditor);
		add(checkPanel, BorderLayout.SOUTH);
	}

	public void jumpToBeginning()
	{
		xmlTextEditor.setCaretPosition(0);
	}

	public void showParseException(SAXParseException e)
	{
		checkPanel.showParseException(e);
	}

	public XmlTextEditor getEditor()
	{
		return xmlTextEditor;
	}

	public XmlTextDocument getDocument()
	{
		return xmlTextEditor.getXmlTextDocument();
	}
}
