package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.CharacterDataEditDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.Frame;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

/**
 * A Swing Action that shows a dialog to edit the contents
 * of the currently selected character node.
 *
 * @author Bruno Dumon
 */
public class EditCharacterDataAction extends AbstractAction
{
	protected XmlEditor xmlEditor;
	protected CharacterData characterData;

	public EditCharacterDataAction(XmlEditor xmlEditor, CharacterData characterData)
	{
		super("Edit text...");
		this.xmlEditor = xmlEditor;
		this.characterData = characterData;
	}

	public void actionPerformed(ActionEvent e)
	{
		// FIXME: we shouldn't create a new dialog here each time. Maybe use singleton pattern
		Object owner = xmlEditor.getTopLevelAncestor();
		CharacterDataEditDialog ced = new CharacterDataEditDialog((Frame)(owner instanceof Frame ? owner : null));
		String result = ced.showWithData(characterData.getData());
		if (result != null)
			characterData.setData(result);
	}
}
