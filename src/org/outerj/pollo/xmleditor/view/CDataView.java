package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;

import java.awt.*;
import java.awt.font.*;

import org.w3c.dom.CDATASection;


/**
  A view for CDATA nodes.
 */
public class CDataView extends CharacterDataBlockView
{
	protected static Color backgroundColor = new Color(255, 255, 255);

	public CDataView(View parentView, CDATASection cdata, XmlEditor xmlEditor)
	{
		super(parentView, cdata, xmlEditor);
	}

	public void drawFrame(Graphics2D g, int startH, int startV)
	{
		Rectangle frame = new Rectangle(startH, startV, width, getHeight());

		g.setColor(backgroundColor);
		g.fill(frame);

		g.setColor(new Color(68, 68, 255)); // blue border, to distinguish from text nodes.

		if (xmlEditor.getSelectedNode() == characterData)
			g.setStroke(BlockView.STROKE_HEAVY);
		else
			g.setStroke(BlockView.STROKE_LIGHT);

		g.draw(frame);
		g.setStroke(BlockView.STROKE_LIGHT);
		g.setColor(Color.black);
	}

}
