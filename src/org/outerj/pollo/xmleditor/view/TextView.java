package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.w3c.dom.Text;

import java.awt.*;


/**
  A View for Text nodes.
 */
public class TextView extends CharacterDataBlockView
{
    protected static Color backgroundColor = Color.white;

    public TextView(View parentView, Text text, XmlEditor xmlEditor)
    {
        super(parentView, text, xmlEditor);
    }

    public void drawFrame(Graphics2D g, int startH, int startV)
    {
        Rectangle frame = new Rectangle(startH, startV, width, getHeight());

        g.setColor(backgroundColor);
        g.fill(frame);

        g.setColor(Color.black);
        if (xmlEditor.getSelectedNode() == characterData)
            g.setStroke(BlockView.STROKE_HEAVY);
        else
            g.setStroke(BlockView.STROKE_LIGHT);

        g.draw(frame);
        g.setStroke(BlockView.STROKE_LIGHT);
    }

}
