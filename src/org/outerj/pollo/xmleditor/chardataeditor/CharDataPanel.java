package org.outerj.pollo.xmleditor.chardataeditor;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.util.FocusBorder;
import org.outerj.pollo.xmleditor.Cleanable;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.w3c.dom.Node;
import org.w3c.dom.CharacterData;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;

public class CharDataPanel extends JPanel implements SelectionListener, EventListener, Cleanable
{
	protected XmlModel xmlModel;
	protected JTextArea charDataTextArea;
	protected JScrollPane scrollPane;
	protected CharacterData currentNode;
	protected int nodetype;

	/**
	 * @param nodetype must be one of the following constants defined in
	 * org.w3c.dom.Node : CDATA_SECTION_NODE, TEXT_NODE, COMMENT_NODE.
	 */
	public CharDataPanel(XmlModel xmlModel, int nodetype)
	{
		this.xmlModel = xmlModel;
		this.nodetype = nodetype;

		this.setLayout(new BorderLayout());

		charDataTextArea = new JTextArea();
		charDataTextArea.setFont(new Font("Monospaced", 0, 12));

		scrollPane = new JScrollPane(charDataTextArea);
		this.add(scrollPane, BorderLayout.CENTER);

		charDataTextArea.addFocusListener(new FocusBorder(scrollPane));

		JButton applyButton = new JButton("Apply changes");
		applyButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent event)
					{
						saveChanges();
					}
				});

		Box buttons = new Box(BoxLayout.X_AXIS);
		buttons.add(Box.createGlue());
		buttons.add(applyButton);

		this.add(buttons, BorderLayout.SOUTH);
	}

	/**
	 * Implementation of the SelectionListener interface.
	 */
	public void nodeSelected(Node node)
	{
		saveChanges();

		if (node.getNodeType() == nodetype)
		{
			CharacterData charData = (CharacterData)node;
			charDataTextArea.setText(charData.getData());
			charDataTextArea.setCaretPosition(0);

			currentNode = charData;
			((EventTarget)charData).addEventListener("DOMCharacterDataModified", this, false);

			setEnabled(true);
		}
		else
		{
			setEnabled(false);
		}
	}

	/**
	 * Implementation of the SelectionListener interface.
	 */
	public void nodeUnselected(Node node)
	{
		saveChanges();
		setEnabled(false);

		if (currentNode != null)
		{
			((EventTarget)currentNode).removeEventListener("DOMCharacterDataModified", this, false);
			currentNode = null;
		}
	}

	public void saveChanges()
	{
		if (currentNode != null)
		{
			if (!currentNode.getData().equals(charDataTextArea.getText()))
			{
				currentNode.setData(charDataTextArea.getText());
			}
		}
	}

	/**
	 * Implementation of the org.w3c.dom.event.EventListener inteface
	 */
	public void handleEvent(Event e)
	{
		try
		{
			if (e.getType().equalsIgnoreCase("DOMCharacterDataModified"))
			{
				charDataTextArea.setText(currentNode.getData());
			}
			else
			{
				System.out.println("WARNING: unprocessed dom event:" + e.getType());
			}
		}
		catch (Exception exc)
		{
			// this try-catch is necessary because if an exception occurs in this code,
			// it is catched somewhere in the xerces code and we wouldn't see it or know
			// what's going on.
			exc.printStackTrace();
		}
	}

	public void requestFocus()
	{
		charDataTextArea.requestFocus();
	}

	public void cleanup()
	{
		if (currentNode != null)
		{
			((EventTarget)currentNode).removeEventListener("DOMCharacterDataModified", this, false);
			currentNode = null;
		}
	}
}
