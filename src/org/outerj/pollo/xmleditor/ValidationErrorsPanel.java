package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.action.ValidateAction;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.xmleditor.schema.ValidationErrorInfo;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collection;

public class ValidationErrorsPanel extends JPanel implements ActionListener
{
	protected final XmlEditorPanel xmlEditorPanel;
	protected final XmlEditor xmlEditor;
	protected final AttributesPanel attributesPanel;
	protected final ValidateAction validateAction;

	protected final JList errorsList;

	public ValidationErrorsPanel(XmlEditorPanel xmlEditorPanel, AttributesPanel attributesPanel)
	{
		this.xmlEditorPanel = xmlEditorPanel;
		this.attributesPanel = attributesPanel;
		this.xmlEditor = xmlEditorPanel.getXmlEditor();
		this.validateAction = xmlEditorPanel.getValidateAction();

		// construct the gui
		setLayout(new BorderLayout());

		Box box = new Box(BoxLayout.X_AXIS);

		JLabel title = new JLabel("Validation Errors (double click to go to affected node)");
		box.add(title);
		box.add(Box.createHorizontalGlue());

		JButton revalidateButton = new JButton(IconManager.getIcon("org/outerj/pollo/resource/stock_refresh-16.png"));
		revalidateButton.setToolTipText("Revalidate document");
		revalidateButton.setActionCommand("revalidate");
		revalidateButton.addActionListener(this);
		revalidateButton.setBorderPainted(false);
		box.add(revalidateButton);

		JButton hideButton = new JButton(IconManager.getIcon("org/outerj/pollo/resource/stock_cancel.png"));
		hideButton.setToolTipText("Hide validation errors panel");
		hideButton.setActionCommand("hide-panel");
		hideButton.addActionListener(this);
		hideButton.setBorderPainted(false);
		box.add(hideButton);


		this.add(box, BorderLayout.NORTH);

		errorsList = new JList();
		MouseListener mouseListener = new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int index = errorsList.locationToIndex(e.getPoint());
					if (index == -1)
						return;
					ValidationErrorInfo errorInfo = (ValidationErrorInfo)errorsList.getModel().getElementAt(index);
					Node location = errorInfo.getLocation();
					if (location == null)
					{
						JOptionPane.showMessageDialog(getTopLevelAncestor()
								, "Pollo doesn't know where this error occured."
								, "Message", JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					View view = null;
					view = (View)xmlEditor.getRootView().findNode(location);

					if (view == null)
					{
						JOptionPane.showMessageDialog(getTopLevelAncestor()
								, "Could not find the node for this error in the view. Maybe you have removed it since the last validation?"
								, "Message", JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					view.assureVisibility(false);
					int startV = view.getVerticalPosition();
					int startH = view.getHorizontalPosition();
					view.markAsSelected(startH, startV);

					xmlEditor.scrollAlignBottom(startV, view.getHeight());

					if (errorInfo.getAttrLocalName() != null)
					{
						ValidationErrorsPanel.this.attributesPanel
								.highlightAttribute(errorInfo.getAttrNamespaceURI(), errorInfo.getAttrLocalName());
					}

				 }
			}
		};
		errorsList.addMouseListener(mouseListener);



		this.add(new JScrollPane(errorsList), BorderLayout.CENTER);
	}

	/**
	 * @param validationErrors a collection of ValidationErrorInfo instances
	 */
	public void showErrors(Collection validationErrors)
	{
		errorsList.removeAll();

		errorsList.setListData(validationErrors.toArray());
	}

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals("revalidate"))
		{
			validateAction.actionPerformed(null);
		}
		else if (event.getActionCommand().equals("hide-panel"))
		{
			xmlEditorPanel.hideValidationErrorsPanel();
		}
	}
}
