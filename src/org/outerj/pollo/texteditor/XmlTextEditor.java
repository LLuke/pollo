package org.outerj.pollo.texteditor;

import org.outerj.pollo.texteditor.action.CopyAction;
import org.outerj.pollo.texteditor.action.CutAction;
import org.outerj.pollo.texteditor.action.PasteAction;

import java.awt.*;

public class XmlTextEditor extends JEditTextArea
{
    protected CopyAction copyAction;
    protected CutAction cutAction;
    protected PasteAction pasteAction;

    public XmlTextEditor()
    {
        super();

        copyAction = new CopyAction(this);
        cutAction = new CutAction(this);
        pasteAction = new PasteAction(this);

        setFont(new Font("Monospaced", 0, 12));
    }

    /**
     * Should be called after the right document has been set.
     */
    public void addExtraKeyBindings()
    {
        InputHandler inputHandler = getInputHandler();
        inputHandler.addKeyBinding("C+c", copyAction);
        inputHandler.addKeyBinding("C+x", cutAction);
        inputHandler.addKeyBinding("C+v", pasteAction);
        inputHandler.addKeyBinding("C+z", getXmlTextDocument().getUndoAction());
    }

    public XmlTextDocument getXmlTextDocument()
    {
        return (XmlTextDocument)getDocument();
    }

    public CopyAction getCopyAction()
    {
        return copyAction;
    }

    public CutAction getCutAction()
    {
        return cutAction;
    }

    public PasteAction getPasteAction()
    {
        return pasteAction;
    }
}
