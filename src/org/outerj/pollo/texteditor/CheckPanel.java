package org.outerj.pollo.texteditor;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.dialog.ErrorDialog;

import org.apache.xerces.parsers.SAXParser;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.text.Segment;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.io.CharArrayReader;

import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;

/**
 * Panel that shows well-formdness check errors.
 */
public class CheckPanel extends JPanel implements ActionListener
{
	protected XmlModel xmlModel;
	protected JLabel messageLabel;
	protected XmlTextEditor textArea;

	protected static final String INFO_MSG = "Press the button to check the document for well-formedness errors.";

	public CheckPanel(XmlModel xmlModel, XmlTextEditor textArea)
	{
		this.xmlModel = xmlModel;
		this.textArea = textArea;

		setLayout(new BorderLayout());

		messageLabel = new JLabel(INFO_MSG);
		Dimension dimension = messageLabel.getPreferredSize();
		dimension.width = Integer.MAX_VALUE;
		messageLabel.setMaximumSize(dimension);
		add(messageLabel, BorderLayout.CENTER);

		JButton checkButton = new JButton("Check well-formedness");
		checkButton.setActionCommand("check-wf");
		checkButton.addActionListener(this);
		checkButton.setRequestFocusEnabled(false);
		add(checkButton, BorderLayout.EAST);
	}

	public void showParseException(SAXParseException exception)
	{
		int line = exception.getLineNumber();
		int col = exception.getColumnNumber();

		textArea.setCaretPosition(textArea.getLineStartOffset(line - 1) + col - 1);
		messageLabel.setText(exception.getMessage());
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("check-wf"))
		{
			try
			{
				Segment seg = new Segment();
				xmlModel.getTextDocument().getText(0, xmlModel.getTextDocument().getLength(), seg);
				SAXParser saxParser = new SAXParser();
				saxParser.setFeature("http://xml.org/sax/features/namespaces", true);
				saxParser.parse(new InputSource(new CharArrayReader(seg.array, seg.offset, seg.count)));
			}
			catch (SAXParseException e)
			{
				showParseException(e);
				return;
			}
			catch (Exception e)
			{
				ErrorDialog errorDialog = new ErrorDialog(null, "An unexpected exception occured during the well-formedness check.", e);
				errorDialog.show();
			}
			messageLabel.setText("The document is well-formed.");
		}
	}
}
