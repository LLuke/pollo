package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Node;

import java.awt.event.ActionEvent;

public class ToggleExpandCollapseAction extends AbstractNodeAction
{
    protected static final ResourceManager resourceManager = ResourceManager.getManager(ToggleExpandCollapseAction.class);

    public ToggleExpandCollapseAction(XmlEditor xmlEditor)
    {
        super(xmlEditor);
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        // Note: this action is normally bound to the space key. This gave problems combined with
        // the CharDataPanel: when pressing space in the textarea over there, this action was also
        // executed, meaning that the event was apparently not consumed there.
        if (!xmlEditor.hasFocus())
            return;
        View view = xmlEditor.getSelectionInfo().getSelectedNodeView();
        if (view.getNode().getNodeType() != Node.DOCUMENT_NODE)
        {
            if (view.isCollapsed())
                view.expand();
            else
                view.collapse();
        }
    }
}