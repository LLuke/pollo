package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import org.w3c.dom.Node;

/**
 * An action that selects the previous node. Usefull to
 * connect to the arrow up key event.
 *
 * @author Bruno Dumon
 */
public class SelectPreviousNodeAction extends AbstractAction
{
	protected XmlEditor xmlEditor;

	public SelectPreviousNodeAction(XmlEditor xmlEditor)
	{
		super("Select previous node");
		this.xmlEditor = xmlEditor;
	}

	public void actionPerformed(ActionEvent e)
	{
		View selectedView = xmlEditor.getSelectionInfo().getSelectedNodeView();
		if (selectedView != null)
		{
			View prevView = selectedView.getPrevious(true);
			int startV = prevView.getVerticalPosition();
			int startH = prevView.getHorizontalPosition();
			prevView.markAsSelected(startH, startV);
			xmlEditor.scrollAlignTop(startV, prevView.getHeight());
		}
	}
}