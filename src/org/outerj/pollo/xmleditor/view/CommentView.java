package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.w3c.dom.Comment;

import java.awt.*;


/**
  Implements a block view (block as opposed to inline) for Comment nodes.
 */
public class CommentView extends CharacterDataBlockView
{
    protected static Color backgroundColor = new Color(255, 249, 186); // some kind of yellow

    // rendering related constants
    protected static final int FLAP_SIZE = 6;

    public CommentView(View parentView, Comment comment, XmlEditor xmlEditor)
    {
        super(parentView, comment, xmlEditor);
    }

    public void drawFrame(Graphics2D g, int startH, int startV)
    {
        Polygon frame = new Polygon();
        frame.addPoint(startH, startV);
        frame.addPoint(startH + width - FLAP_SIZE, startV);
        frame.addPoint(startH + width, startV + FLAP_SIZE);
        frame.addPoint(startH + width, startV + getHeight());
        frame.addPoint(startH, startV + getHeight());

        g.setColor(backgroundColor);
        g.fill(frame);

        g.setColor(Color.black);

        if (xmlEditor.getSelectedNode() == characterData)
            g.setStroke(BlockView.STROKE_HEAVY);
        else
            g.setStroke(BlockView.STROKE_LIGHT);

        g.draw(frame);
        g.drawLine(startH + width - FLAP_SIZE, startV, startH + width - FLAP_SIZE, startV + FLAP_SIZE);
        g.drawLine(startH + width - FLAP_SIZE, startV + FLAP_SIZE, startH + width, startV + FLAP_SIZE);

        g.setStroke(BlockView.STROKE_LIGHT);
    }

}
