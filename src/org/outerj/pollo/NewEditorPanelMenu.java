package org.outerj.pollo;

import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.gui.EmptyIcon;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * A JMenu from which the user can create new EditorPanels.
 *
 * @author Bruno Dumon
 */
public class NewEditorPanelMenu extends JMenu
{
	PolloFrame polloFrame;

	public NewEditorPanelMenu(PolloFrame polloFrame)
	{
		super ("Create new view on file");
		ResourceManager resMgr = ResourceManager.getManager(NewEditorPanelMenu.class);
		resMgr.configureMenu( this );
		this.polloFrame = polloFrame;
	}

	public void setPopupMenuVisible(boolean visible)
	{
		if (visible)
		{
			removeAll();
			// create the items in the menu, one for each XmlModel
            List openFiles = Pollo.getInstance().getOpenFiles();
			Iterator it = openFiles.iterator();
			while (it.hasNext())
			{
				XmlModel xmlModel = (XmlModel)it.next();
				add(new NewEditorPanelAction(xmlModel));
			}
		}
		super.setPopupMenuVisible(visible);
	}

	public class NewEditorPanelAction extends AbstractAction
	{
		XmlModel xmlModel;

		public NewEditorPanelAction(XmlModel xmlModel)
		{
			this.xmlModel = xmlModel;

			// set the action name
			putValue(Action.NAME, xmlModel.getLongTitle());
		}

		public void actionPerformed(ActionEvent e)
		{
			EditorPanel editorPanel = Pollo.getInstance().createEditorPanel(xmlModel, polloFrame);
			if (editorPanel != null)
				polloFrame.addEditorPanel(editorPanel);
		}
	}

}
