package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;

import java.awt.*;
import java.awt.font.*;

import org.w3c.dom.ProcessingInstruction;


/**
 * A View for processing instructions.
 */
public class PIView extends CharacterDataBlockView
{
	protected static Color backgroundColor = new Color(255, 180, 180);
	protected String title;

	public PIView(View parentView, ProcessingInstruction pi, XmlEditor xmlEditor)
	{
		super(parentView, pi, xmlEditor);
		title = new String("Processing Instruction target: " + pi.getTarget());
	}

	public void drawFrame(Graphics2D g, int startH, int startV)
	{
		Polygon frame = new Polygon();
		frame.addPoint(startH, startV + 2);
		frame.addPoint(startH + 2, startV);
		frame.addPoint(startH + width - 2, startV);
		frame.addPoint(startH + width, startV + 2);
		frame.addPoint(startH + width, startV + getHeight() - 2);
		frame.addPoint(startH + width - 2, startV + getHeight());
		frame.addPoint(startH + 2, startV + getHeight());
		frame.addPoint(startH, startV + getHeight() - 2);

		g.setColor(backgroundColor);
		g.fill(frame);

		g.setColor(Color.black);
		if (xmlEditor.getSelectedNode() == characterData)
			g.setStroke(BlockView.STROKE_HEAVY);
		else
			g.setStroke(BlockView.STROKE_LIGHT);

		g.draw(frame);
		g.setStroke(BlockView.STROKE_LIGHT);

		// draw the PI target
		g.drawString(title, startH + LEFT_TEXT_MARGIN, startV + fontMetrics.getAscent());

		if (!isCollapsed())
		{
			int linepos = startV + fontMetrics.getHeight();
			g.drawLine(startH, linepos, startH + width, linepos);
		}
	}

	public int getHeader()
	{
		return fontMetrics.getHeight();
	}

	public int getFooter()
	{
		return 2;
	}
}
