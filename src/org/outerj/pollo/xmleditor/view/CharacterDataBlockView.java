package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.NodeClickedEvent;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlTransferable;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;


/**
 * Implements an abstract base class for 'block views' (block as opposed to inline) for
 * character data nodes (textnodes, commentnodes, cdata nodes).
 *
 * @author Bruno Dumon
 */
public abstract class CharacterDataBlockView extends BlockView
{
	protected static FontMetrics fontMetrics;
	protected static Font font;
	protected static int lineHeight = -1;

	// text clipping related constants
	protected static final int NOT_CALCULATED    = -1;
	protected static final int NO_CLIPPING       = -2;

	// rendering related constants
	protected static final int LEFT_TEXT_MARGIN  = 13;
	protected static final int RIGHT_TEXT_MARGIN = 5;
	protected static final int COLLAPSE_SIGN_TOP_POSITION  = 2;
	protected static final int COLLAPSE_SIGN_LEFT_POSITION = 3;

	protected Node characterData; //CharacterData characterData;

	protected int numberOfLines;
	protected int [] lineInfo;
		// the lineInfo structure contains three int's for each line:
		// the offset (in the data array), length, and the clippinglength
	protected char [] data;

	/**
	 * @param characterData should be a node of type CharacterData or ProcessingInstruction
	 */
	public CharacterDataBlockView(View parentView, Node characterData, XmlEditor xmlEditor)
	{
		super(parentView, xmlEditor);
		this.characterData = characterData;

		// we keep a copy of the characterData's data in a char array, because thats easier and faster to work with
		if (characterData instanceof CharacterData)
			data = ((CharacterData)characterData).getData().toCharArray();
		else if (characterData instanceof ProcessingInstruction)
			data = ((ProcessingInstruction)characterData).getData().toCharArray();

		// register this view as an eventlistener for changes to the character data 
		((EventTarget)characterData).addEventListener("DOMCharacterDataModified", this, false);
	}

	public abstract void drawFrame(Graphics2D g, int startH, int startV);

	public void paint(Graphics gr, int startH, int startV)
	{
		Graphics2D g = (Graphics2D)gr;
		g.setFont(font);

		drawFrame(g, startH, startV);

		g.setColor(Color.black);

		// draw the collapse sign
		drawCollapseSign(g, isCollapsed, startH + COLLAPSE_SIGN_LEFT_POSITION, startV + COLLAPSE_SIGN_TOP_POSITION);

		// draw the characterdata text
		int verticalOffset = startV + getHeader() + fontMetrics.getAscent();
		// if the view is collapsed and there is a header, draw no lines, otheriwse draw 1 line
		int doHowMuchLines = isCollapsed() ? (getHeader() != 0 ? 0 : 1) : numberOfLines;
		for (int i = 0; i < doHowMuchLines; i++)
		{
			if (lineInfo[(i*3)+2] == NOT_CALCULATED) // clipping not calculated
			{
				lineInfo[(i*3)+2] = clipText(data, lineInfo[(i*3)], lineInfo[(i*3)+1],
						width - LEFT_TEXT_MARGIN - RIGHT_TEXT_MARGIN);
			}

			if (lineInfo[(i*3)+2] == NO_CLIPPING) // draw all text
			{
				g.drawChars(data, lineInfo[(i*3)], lineInfo[(i*3)+1], startH + LEFT_TEXT_MARGIN, verticalOffset);
			}
			else // draw part of the text and three dots after it
			{
				g.drawChars(data, lineInfo[(i*3)], lineInfo[(i*3)+2], startH + LEFT_TEXT_MARGIN, verticalOffset);
				g.drawString("...", startH + LEFT_TEXT_MARGIN
						+ fontMetrics.charsWidth(data, lineInfo[(i*3)], lineInfo[(i*3)+2]), verticalOffset);
			}
			verticalOffset += lineHeight;
		}
	}

	/**
	  Layouts this view (and it's children) to fit to
	  the given width.

	  @param startV is relative to the parent, thus it is 0 for the first child

	  */
	public void layout(int width)
	{
		// initialize variables
		if (font == null)
			font = new Font("Monospaced", 0, 12);
		if (fontMetrics == null)
		{
			Graphics graphics = xmlEditor.getGraphics();
			graphics.setFont(font);
		   	fontMetrics = graphics.getFontMetrics();
		}
		if (lineHeight == -1)
			lineHeight = fontMetrics.getHeight();

		// fill the lineInfo structure
		numberOfLines = countNumberOfLines(data);
		lineInfo = new int[numberOfLines * 3];

		int pos = 0;
		for (int i = 0; i < numberOfLines; i++)
		{
			lineInfo[(i*3)] = pos; // first field: starting position of the line (in the data array)
			pos = searchNextLineBreak(data, pos);
			lineInfo[(i*3)+1] = pos - lineInfo[(i*3)]; // second field: length of the line (number of chars)
			lineInfo[(i*3)+2] = NOT_CALCULATED; // clipping length, will be calculated in paint method

			pos +=1;
		}

		this.width = width;
	}




	public void heightChanged(int amount)
	{
		if (parentView != null)
			parentView.heightChanged(amount);
		else
		{
			resetSize();
		}
	}

	public int widthChanged(int amount)
	{
		this.width = this.width + amount;

		// mark the clipping lenght as not calculated, will be recalculated as necessary
		// in the paint method
		for (int i = 0; i < numberOfLines; i++)
		{
			lineInfo[(i*3)+2] = NOT_CALCULATED;
		}

		return getHeight();
	}


	public void addChildView(View childView)
	{
		throw new RuntimeException("This is not supported on CharacterDataBlockView");
	}

	// mouse events

	public void mousePressed(MouseEvent e, int startH, int startV)
	{
		// if clicked on the collapse/expand button
		if ((e.getY() > startV + COLLAPSE_SIGN_TOP_POSITION) && (e.getY() < startV + COLLAPSE_SIGN_TOP_POSITION + 10)
				&& (e.getX() > startH + COLLAPSE_SIGN_LEFT_POSITION) && (e.getX() < startH + COLLAPSE_SIGN_LEFT_POSITION + 10))
		{
			if (isCollapsed())
				expand();
			else
				collapse();
		}
		else
		{
			NodeClickedEvent nce = new NodeClickedEvent(characterData, e);
			xmlEditor.fireNodeClickedEvent(nce);
			
			// make that the current element is indicated
			markAsSelected(startH, startV);
		}
	}


	public int getHeight()
	{
		if (!isCollapsed)
			return getHeader() + (numberOfLines * lineHeight) + getFooter();
		else
			return fontMetrics.getHeight();
	}


	public void handleEvent(Event e)
	{
		try
		{
			if (e.getType().equalsIgnoreCase("DOMCharacterDataModified"))
			{
				if (characterData instanceof CharacterData)
					data = ((CharacterData)characterData).getData().toCharArray();
				else if (characterData instanceof ProcessingInstruction)
					data = ((ProcessingInstruction)characterData).getData().toCharArray();
				int oldHeight = getHeight();
				layout(width);
				int newHeight = getHeight();
				applyHeightChange(newHeight - oldHeight);
			}
			else
			{
				System.out.println("WARNING: unprocessed dom event:" + e.getType());
			}
		}
		catch (Exception exc)
		{
			// this try-catch is necessary because if an exception occurs in this code,
			// it is catched somewhere in the xerces code and we wouldn't see it or know
			// what's going on.
			exc.printStackTrace();
		}
	}

	public Node getNode()
	{
		return characterData;
	}


	public void removeEventListeners()
	{
		((EventTarget)characterData).removeEventListener("DOMCharacterDataModified", this, false);
	}


	public void dragGestureRecognized(DragGestureEvent event, int startH, int startV)
	{
		DocumentFragment documentFragment = xmlEditor.getXmlModel().getDocument().createDocumentFragment();
		documentFragment.appendChild(characterData.cloneNode(true));
		xmlEditor.setDraggingNode(characterData, event.getDragAction() == DnDConstants.ACTION_MOVE ? true : false);
		if(event.getDragAction() == DnDConstants.ACTION_COPY)
		{
			xmlEditor.getDragSource().startDrag(event, DragSource.DefaultCopyDrop,
					new XmlTransferable(documentFragment), xmlEditor);
		}
		else
		{
			xmlEditor.getDragSource().startDrag(event, DragSource.DefaultMoveDrop,
					new XmlTransferable(documentFragment), xmlEditor);
		}
	}

	public void dragOver(DropTargetDragEvent event, int startH, int startV)
	{
		// dropping is not allowed on a characterData node
		xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
		event.rejectDrag();
	}


	/**
	 * Given a maximum allowed width (in pixels), this method calculates how much text
	 * will fit on one line, assuming that if it doesn't fit, three dots are appended (...).
	 */
	protected int clipText(char [] text, final int offset, final int length, int maxwidth)
	{
		if (fontMetrics.charsWidth(text, offset, length) < maxwidth)
			return NO_CLIPPING;

		final String dots = "...";
		int totalWidth = fontMetrics.stringWidth(dots);

		int i = 0;
		for (; i < length; i++)
		{
			totalWidth += fontMetrics.charWidth(text[offset + i]);
			if (totalWidth > maxwidth)
				break;
		}

		return i;
	}

	/**
	 * Counts the number of lines in the text array.
	 */
	public int countNumberOfLines(char [] text)
	{
		int linecount = 0;

		for (int i = 0; i < text.length; i++)
		{
			if (text[i] == '\n')
			{
				linecount++;
			}
		}
		// note: do +1 because after the last line there is no \n
		return linecount + 1;
	}

	/**
	 * Finds the position of the last line break, or if the end of the array is reached,
	 * returns that position.
	 */
	public int searchNextLineBreak(char [] data, int offset)
	{
		int linebreak = offset;

		while ((linebreak < data.length) && data[linebreak] != '\n')
		{
			linebreak++;
		}

		return linebreak;
	}

	public boolean isCollapsable()
	{
		return true;
	}

	/**
	 * Allows subclasses to specify a region below the text.
	 */
	protected int getFooter()
	{
		return 0;
	}

	/**
	 * Allows subclasses to specify a region above the text.
	 */
	protected int getHeader()
	{
		return 0;
	}
}
