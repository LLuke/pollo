package org.outerj.xmleditor.view;

import org.outerj.xmleditor.DisplaySpecification;
import org.outerj.xmleditor.XmlContentEditor;
import org.outerj.xmleditor.ElementClickedEvent;
import org.outerj.xmleditor.DisplaySpecification.ElementSpec;
import org.outerj.xmleditor.DisplaySpecification.AttributeSpec;
import org.outerj.xmleditor.XmlTransferable;
import org.outerj.xmleditor.CommandTransferable;

import java.awt.*;
import java.awt.font.*;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DnDConstants;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.ArrayList;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.dnd.DragGestureEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;


public class StructuralView implements View
{
	protected ElementSpec elementSpec;
	protected TextLayout elementNameLayout;
	protected int titleHeight = 10;
	protected int width;
	protected int contentHeight;
	protected int collapseSignTop;
	protected boolean isCollapsed = false;
	protected View parentView;
	protected XmlContentEditor xmlContentEditor;
	protected Element element;
	protected Graphics2D graphics;
	protected Shape elementShape;
	protected ArrayList childViewList = new ArrayList(10);

	// everything for the attributes
	protected ArrayList attrViewInfoList = new ArrayList();
	protected Font attributeValueFont;

	protected static final int SPACING_VERTICAL = 5;
	protected static final int PADDING_RIGHT = 4;
	protected static final int BORDER_WIDTH = 6; //4;
	protected static final int SPACING_HORIZONTAL = BORDER_WIDTH + 7;
	protected static final int EMPTY_CONTENT_HEIGHT = 10;
	protected static final int COLLAPS_ICON_WIDTH = 15;
	protected static final int END_MARKER_HEIGHT = 10;
	protected static final int ATTR_SPACING = 9;
	protected static final int ATTR_NAME_VALUE_SPACING = 2;

	public StructuralView(View parentView, Element element, XmlContentEditor xmlContentEditor)
	{
		this.parentView = parentView;
		this.xmlContentEditor = xmlContentEditor;
		this.element = element;
		this.graphics = (Graphics2D)xmlContentEditor.getGraphics();
		if (xmlContentEditor.antialias())
		{
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		// register this view as an eventlistener for changes to the element
		((EventTarget)element).addEventListener("DOMAttrModified", this, false);
		((EventTarget)element).addEventListener("DOMNodeInserted", this, false);
		((EventTarget)element).addEventListener("DOMNodeRemoved", this, false);

		// get all the displayspec info we want (suppose for now that this info
		// doesn't change at runtime -- which will normally not be necessary)
		DisplaySpecification displaySpec = xmlContentEditor.getDisplaySpec();
		String uri = element.getNamespaceURI();
		String name = element.getLocalName();
		this.elementSpec = displaySpec.getElementSpec(uri, name);

		int numberOfAttrs = elementSpec.attributesToShow.size();
		AttrViewInfo attrViewInfo;
		for (int i = 0; i < numberOfAttrs; i++)
		{
			attrViewInfo = new AttrViewInfo();
			attrViewInfo.attributeSpec = (AttributeSpec)elementSpec.attributesToShow.get(i);
			attrViewInfoList.add(attrViewInfo);
		}
		this.attributeValueFont = displaySpec.getDefaultAttributeValueFont();

		// make the textlayout for the element name
		String prefix = element.getPrefix();
		String qname = element.getLocalName();
		if (prefix != null && prefix.length() > 0)
			qname = prefix + ":" + qname;
		this.elementNameLayout = new TextLayout(qname, elementSpec.font, graphics.getFontRenderContext());
	}

	public void paint(Graphics gr, int startHorizontal, int startVertical)
	{
		Graphics2D g = (Graphics2D)gr;

		// make the shape
		if (childViewList.size()  > 0)
		{
			Polygon poly = new Polygon();
			elementShape = poly;
			// starting at the top left point
			poly.addPoint(0, 0);
			poly.addPoint(width - PADDING_RIGHT, 0);
			poly.addPoint(width - PADDING_RIGHT, titleHeight);
			if (!isCollapsed)
			{
				poly.addPoint(BORDER_WIDTH, titleHeight);
				poly.addPoint(BORDER_WIDTH, titleHeight + contentHeight);
				poly.addPoint(BORDER_WIDTH + 4, titleHeight + contentHeight);
				poly.addPoint(0, titleHeight + contentHeight + END_MARKER_HEIGHT);
			}
			else
			{
				poly.addPoint(BORDER_WIDTH + 4, titleHeight);
				poly.addPoint(0, titleHeight + END_MARKER_HEIGHT);
			}

		}
		else
		{
			// just draw a rectangle
			elementShape = new Rectangle(0, 0, width - PADDING_RIGHT, titleHeight);
		}

		if (elementShape instanceof Polygon)
			((Polygon)elementShape).translate(startHorizontal, startVertical);
		else if (elementShape instanceof Rectangle)
			((Rectangle)elementShape).translate(startHorizontal, startVertical);

		g.setColor(elementSpec.backgroundColor);
		g.setStroke(new BasicStroke(1));
		g.fill(elementShape);

		if (xmlContentEditor.getSelectedView() == this)
			g.setColor(Color.red);
		else
			g.setColor(new Color(68, 68, 68));

		g.draw(elementShape);
		g.setColor(Color.black);
	
		if (childViewList.size()  > 0)
		{
			// draw + or - sign
			g.drawRect(startHorizontal + BORDER_WIDTH, startVertical + collapseSignTop, 8, 8);
			if (isCollapsed)
			{
				// draw '+' sign
				g.drawLine(startHorizontal + BORDER_WIDTH + 2, startVertical + collapseSignTop + 4,
						startHorizontal + BORDER_WIDTH + 6, startVertical + collapseSignTop + 4);

				g.drawLine(startHorizontal + BORDER_WIDTH + 4, startVertical + collapseSignTop + 2,
						startHorizontal + BORDER_WIDTH + 4, startVertical + collapseSignTop + 6);
			}
			else
			{
				// draw '-' sign
				g.drawLine(startHorizontal + BORDER_WIDTH + 2, startVertical + collapseSignTop + 4,
						startHorizontal + BORDER_WIDTH + 6, startVertical + collapseSignTop + 4);
			}


			// now draw the children, but only those that need updating
			Iterator childrenIt = childViewList.iterator();

			int totalHeight = titleHeight;
			int clipStartVertical = (int)g.getClipBounds().getY();
			int clipEndVertical = clipStartVertical + (int)g.getClipBounds().getHeight();
			if (!isCollapsed)
			{
				int childVertPos = startVertical + titleHeight + SPACING_VERTICAL;
				while (childrenIt.hasNext())
				{
					View view = (View)childrenIt.next();
					if (view.needsRepainting(childVertPos, clipStartVertical, clipEndVertical))
						view.paint(g, startHorizontal + SPACING_HORIZONTAL, childVertPos);
					childVertPos += view.getHeight() + SPACING_VERTICAL;
				}
			}
		}

		// draw the element name
		elementNameLayout.draw(g, startHorizontal + 20, startVertical + (titleHeight - 2));

		// draw the attributes
		int baseline = startVertical + (titleHeight - 2);
		AttrViewInfo attrViewInfo;
		int numberOfAttrs = attrViewInfoList.size();
		for (int i = 0; i < numberOfAttrs; i++)
		{
			attrViewInfo = (AttrViewInfo)attrViewInfoList.get(i);
			if (attrViewInfo.visible)
			{
				attrViewInfo.nameLayout.draw(g, startHorizontal + attrViewInfo.namePos, baseline);
				attrViewInfo.valueLayout.draw(g, startHorizontal + attrViewInfo.valuePos, baseline);
			}
		}
	}

	/**
	  Layouts this view (and it's children) to fit to
	  the given width.

	  @param startVertical is relative to the parent, thus it is 0 for the first child

	  @return the required height.
	  */
	public int layout(int width)
	{
		// init
		Rectangle2D elementNameBounds = elementNameLayout.getBounds();
		this.titleHeight = (int)(elementNameBounds.getHeight() + 4);
		this.width = width;

		// layout the attributes
		layoutAttributes();

		collapseSignTop = (titleHeight / 2) - 5;

		// layout the children of this view (and thus calculate the height of this view)
		Iterator childrenIt = childViewList.iterator();

		int prevChildHeight = 0;
		int childHeight = 0;
		int totalChildrenHeight = 0;

		while (childrenIt.hasNext())
		{
			View view = (View)childrenIt.next();
			childHeight = view.layout(width - SPACING_HORIZONTAL);
			prevChildHeight = childHeight;
			totalChildrenHeight += childHeight + SPACING_VERTICAL;
		}

		if (childViewList.size() > 0)
		{
			contentHeight = totalChildrenHeight + SPACING_VERTICAL;
		}
		else
		{
			contentHeight = 0;
		}

		return getHeight();
	}


	/**
	  This method calculates the positions off the attributes. Called during initial layout
	  or when attributes have changed/removed/added.
	 */
	public void layoutAttributes()
	{
		Rectangle2D elementNameBounds = elementNameLayout.getBounds();
		int attrPos = BORDER_WIDTH + COLLAPS_ICON_WIDTH + (int)elementNameBounds.getWidth() + ATTR_SPACING;
		AttrViewInfo attrViewInfo;
		int numberOfAttrs = attrViewInfoList.size();
		for (int i = 0; i < numberOfAttrs; i++)
		{
			attrViewInfo = (AttrViewInfo)attrViewInfoList.get(i);
			Attr attr;
			if (attrViewInfo.attributeSpec.nsUri.equals(""))
			{
				attr = element.getAttributeNode(attrViewInfo.attributeSpec.localName);
			}
			else
			{
				attr = element.getAttributeNodeNS(attrViewInfo.attributeSpec.nsUri,
						attrViewInfo.attributeSpec.localName);
			}

			if (attr == null || attr.getValue().length() < 1)
			{
				attrViewInfo.visible = false;
			}
			else
			{
				attrViewInfo.valueLayout = new TextLayout(attr.getValue(), attributeValueFont, graphics.getFontRenderContext());
				if (attrViewInfo.nameLayout == null)
				{
					String prefix = attr.getPrefix();
					String qname = attr.getLocalName();
					if (prefix != null)
						qname = prefix + ":" + qname;

					qname += ": ";
					attrViewInfo.nameLayout = new TextLayout(qname, attrViewInfo.attributeSpec.nameFont,
							graphics.getFontRenderContext());
				}
				attrViewInfo.namePos = attrPos;
				attrPos += attrViewInfo.nameLayout.getBounds().getWidth();
				attrPos += ATTR_NAME_VALUE_SPACING;
				attrViewInfo.valuePos = attrPos;
				attrPos += attrViewInfo.valueLayout.getBounds().getWidth();
				attrPos += ATTR_SPACING;
				attrViewInfo.visible = true;
			}
		}
	}


	/**
	  Recalculates the height of this view an recursively of its parent views
	  when the height has changed because of element removal/addition or when
	  collapsing/expanding.
	 */
	public void heightChanged(int amount)
	{
		Iterator childrenIt = childViewList.iterator();

		contentHeight = contentHeight + amount;
		if (parentView != null)
			parentView.heightChanged(amount);
		else
		{
			resetSize();
		}
	}

	protected void resetSize()
	{
		xmlContentEditor.setSize(new Dimension(xmlContentEditor.getWidth(), getHeight()));
		xmlContentEditor.repaint(xmlContentEditor.getVisibleRect());
	}

	public int widthChanged(int amount)
	{
		this.width = this.width + amount;

		Iterator childrenIt = childViewList.iterator();
		while (childrenIt.hasNext())
		{
			View view = (View)childrenIt.next();
			view.widthChanged(amount);
		}

		return getHeight();
	}


	public void addChildView(View childView)
	{
		childViewList.add(childView);
	}

	// mouse events

	public void mouseClicked(MouseEvent e, int startHorizontal, int startVertical)
	{
		// if clicked on the collapse/expand button
		if ((e.getY() > startVertical + collapseSignTop) && (e.getY() < startVertical + collapseSignTop + 10)
				&& (e.getX() > startHorizontal + BORDER_WIDTH) && (e.getX() < startHorizontal + BORDER_WIDTH + 10))
		{
			if (childViewList.size() == 0)
				return;

			int oldHeight = this.getHeight();
			isCollapsed = !isCollapsed;
			int newHeight = this.getHeight();
			if (parentView != null)
				parentView.heightChanged(newHeight - oldHeight);
			else
			{
				xmlContentEditor.setSize(new Dimension(xmlContentEditor.getWidth(), getHeight()));
				xmlContentEditor.repaint(xmlContentEditor.getVisibleRect());
			}
		}
		else if ( e.getY() > startVertical && e.getY() < startVertical + titleHeight
			   && e.getX() > startHorizontal + BORDER_WIDTH + 10)
		{
			ElementClickedEvent ece = new ElementClickedEvent(element, e);
			xmlContentEditor.fireElementClickedEvent(ece);
			
			// make that the current element is indicated
			xmlContentEditor.setSelectedView(this);

			Rectangle redrawRect = new Rectangle(startHorizontal, startVertical, width, getHeight());
			if (xmlContentEditor.getSelectedViewRect() != null)
				redrawRect.add(xmlContentEditor.getSelectedViewRect());
			xmlContentEditor.setSelectedViewRect(new Rectangle(startHorizontal, startVertical, width, getHeight()));
			Rectangle visibleRect = xmlContentEditor.getVisibleRect();
			redrawRect.y = visibleRect.y > redrawRect.y ? visibleRect.y : redrawRect.y;
			redrawRect.height = visibleRect.y + visibleRect.height - redrawRect.y;

			xmlContentEditor.repaint(redrawRect);
		}
		else
		{
			Iterator childrenIt = childViewList.iterator();
			int childVertPos = startVertical + titleHeight + SPACING_VERTICAL;
			while (childrenIt.hasNext())
			{
				View childView = (View)childrenIt.next();
				if (e.getY() > childVertPos && e.getY() < childVertPos + childView.getHeight())
				{
					childView.mouseClicked(e, startHorizontal + SPACING_HORIZONTAL, childVertPos);
					break;
				}
				childVertPos += childView.getHeight() + SPACING_VERTICAL;
			}
		}
	}


	public int getHeight()
	{
		if (!isCollapsed && childViewList.size() > 0)
			return titleHeight + contentHeight + END_MARKER_HEIGHT; 
		else if (isCollapsed)
			return titleHeight + END_MARKER_HEIGHT; 
		else
			return titleHeight; 
	}

	public boolean needsRepainting(int startVertical, int clipStartVertical, int clipEndVertical)
	{
		int absStartVertical = startVertical;
		int absEndVertical = absStartVertical + getHeight();


		if ((absStartVertical >= clipStartVertical && absEndVertical <= clipEndVertical)
				|| (absStartVertical <= clipStartVertical && absEndVertical >= clipStartVertical)
				|| (absStartVertical <= clipEndVertical && absEndVertical >= clipEndVertical)
				|| (absStartVertical <= clipStartVertical && absEndVertical >= clipEndVertical))
			return true;
		else
			return false;
	}

	public void handleEvent(Event e)
	{
		try
		{
			if (e.getType().equalsIgnoreCase("DOMNodeInserted"))
			{
				if (((MutationEvent)e).getRelatedNode() != element)
					return;
				e.stopPropagation();
				createViewsForNewChildren();
			}
			else if (e.getType().equalsIgnoreCase("DOMNodeRemoved"))
			{
				if (((Element)e.getTarget()).getParentNode() == element)
				{
					removeViewForRemovedChild((Element)e.getTarget());
					e.stopPropagation();
				}
			}
			else if (e.getType().equalsIgnoreCase("DOMAttrModified"))
			{
				MutationEvent me = (MutationEvent)e;
				if (me.getTarget() == element)
				{
					e.stopPropagation();
					layoutAttributes();
					xmlContentEditor.repaint(xmlContentEditor.getVisibleRect());
				}
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

	public Element getElement()
	{
		return element;
	}


	/**
	  This method updates the list of childviews: if elements were inserted
	  in the dom, new corresponding views are created. Also updates the
	  layout.
	 */
	public void createViewsForNewChildren()
	{
		NodeList children = element.getChildNodes();
		int elementChildNodeCounter = 0;
		boolean hasChildren = childViewList.size() > 0;
		int heightChangeAmount = 0;
		int oldHeight = getHeight();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if ((node.getNodeType() == Node.ELEMENT_NODE))
			{
				View correspondingView = null;
				try
				{
					correspondingView = (View)childViewList.get(elementChildNodeCounter);
				}
				catch (IndexOutOfBoundsException e) { }

				if (correspondingView == null || correspondingView.getElement() != node)
				{
					View childView = xmlContentEditor.createView((Element)node, this);
					childView.layout(width - SPACING_HORIZONTAL);
					childViewList.add(elementChildNodeCounter, childView);
					heightChangeAmount += childView.getHeight() + SPACING_VERTICAL;
				}
				elementChildNodeCounter++;
			}
		}

		if (heightChangeAmount != 0)
		{
			if (!hasChildren && childViewList.size() > 0)
				heightChangeAmount += SPACING_VERTICAL;

			contentHeight += heightChangeAmount;
			int newHeight = getHeight();
			int diff = newHeight - oldHeight;
			if (parentView != null)
				parentView.heightChanged(diff);
			else
				resetSize();
		}
	}

	public void removeViewForRemovedChild(Element removedChild)
	{
		NodeList children = element.getChildNodes();
		int elementChildNodeCounter = 0;
		boolean hasChildren = childViewList.size() > 0;
		int heightChangeAmount = 0;
		int oldHeight = getHeight();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if ((node == removedChild))
			{
				View correspondingView = (View)childViewList.get(elementChildNodeCounter);
				heightChangeAmount -= correspondingView.getHeight() + SPACING_VERTICAL;
				correspondingView.removeEventListeners();
				childViewList.remove(elementChildNodeCounter);
				break;
				
			}
			if (node.getNodeType() == Node.ELEMENT_NODE)
				elementChildNodeCounter++;
		}
		if (childViewList.size() == 0)
		{
			// if last child is removed, contentheight should become zero
			heightChangeAmount = 0 - contentHeight;
		}
		if (heightChangeAmount != 0)
		{
			contentHeight += heightChangeAmount;
			int newHeight = getHeight();
			int diff = newHeight - oldHeight;
			if (parentView != null)
				parentView.heightChanged(diff);
			else
				resetSize();
		}
	}

	public void removeEventListeners()
	{
		((EventTarget)element).removeEventListener("DOMAttrModified", this, false);
		((EventTarget)element).removeEventListener("DOMNodeInserted", this, false);
		((EventTarget)element).removeEventListener("DOMNodeRemoved", this, false);

		Iterator childViewListIt = childViewList.iterator();
		while (childViewListIt.hasNext())
		{
			View childView = (View)childViewListIt.next();
			childView.removeEventListeners();
		}
	}


	public void dragGestureRecognized(DragGestureEvent event, int startHorizontal, int startVertical)
	{
		Point p = event.getDragOrigin();
		if ( p.getY() > startVertical && p.getY() < startVertical + titleHeight
			   && p.getX() > startHorizontal)
		{
			if (parentView == null)
			{
				// the mainView may not be dragged
				return;
			}
			xmlContentEditor.setDraggingElement(element);
			xmlContentEditor.getDragSource().startDrag(event, DragSource.DefaultMoveDrop,
					new XmlTransferable(element), xmlContentEditor);
		}
		else
		{
			Iterator childrenIt = childViewList.iterator();
			int childVertPos = startVertical + titleHeight + SPACING_VERTICAL;
			while (childrenIt.hasNext())
			{
				View childView = (View)childrenIt.next();
				if (p.getY() > childVertPos && p.getY() < childVertPos + childView.getHeight())
				{
					childView.dragGestureRecognized(event, startHorizontal + SPACING_HORIZONTAL, childVertPos);
					break;
				}
				childVertPos += childView.getHeight() + SPACING_VERTICAL;
			}
		}
	}

	public void dragOver(DropTargetDragEvent event, int startH, int startV)
	{
		if (isCollapsed)
		{
			xmlContentEditor.setDropData(xmlContentEditor.DROP_ACTION_NOT_ALLOWED, null);
			event.rejectDrag();
			return;
		}

		Point p = event.getLocation();

		if (p.getY() > startV && p.getY() < startV + titleHeight && p.getX() > startH)
		{ // it is on an element
			if (childViewList.size() > 0)
			{
				xmlContentEditor.setDropData(xmlContentEditor.DROP_ACTION_NOT_ALLOWED, null);
				event.rejectDrag();
				return;
			}

			// draw drag over effect
			Rectangle rect = new Rectangle(startH, startV, width - PADDING_RIGHT, titleHeight);
			xmlContentEditor.setDragOverEffectRedraw(new Rectangle(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2));
			Graphics2D graphics = (Graphics2D)xmlContentEditor.getGraphics();
			graphics.setColor(new Color(255, 0, 0));
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(rect);

			xmlContentEditor.setDropData(xmlContentEditor.DROP_ACTION_APPEND_CHILD, element);
			if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
				event.acceptDrag(DnDConstants.ACTION_MOVE);
			else if (event.isDataFlavorSupported(CommandTransferable.commandFlavor))
				event.acceptDrag(DnDConstants.ACTION_MOVE);
			return;
		}
		else if (p.getX() > startH && p.getX() < (startH + width - PADDING_RIGHT - SPACING_HORIZONTAL) ) // maybe it's between to elements
		{
			Iterator childrenIt = childViewList.iterator();
			int childVertPos = startV + titleHeight + SPACING_VERTICAL;
			View childView = null;
			boolean lastOne = false;
			while (childrenIt.hasNext() || (lastOne = true))
			{
				if (!lastOne)
					childView = (View)childrenIt.next();

				if (!lastOne && p.getY() > childVertPos && p.getY() < childVertPos + childView.getHeight())
				{
					childView.dragOver(event, startH + SPACING_HORIZONTAL, childVertPos);
					break;
				}
				else if (p.getY() > (childVertPos - SPACING_VERTICAL) && p.getY() < childVertPos)
				{
					if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor) ||
							event.isDataFlavorSupported(CommandTransferable.commandFlavor))
					{
						// draw drag over effect
						Rectangle rect = new Rectangle(startH + SPACING_HORIZONTAL, childVertPos - 3,
							   	width - PADDING_RIGHT - SPACING_HORIZONTAL, 2);
						xmlContentEditor.setDragOverEffectRedraw(rect);
						Graphics2D graphics = (Graphics2D)xmlContentEditor.getGraphics();
						graphics.setColor(new Color(255, 0, 0));
						graphics.fill(rect);

						if (!lastOne)
							xmlContentEditor.setDropData(xmlContentEditor.DROP_ACTION_INSERT_BEFORE, childView.getElement());
						else
							xmlContentEditor.setDropData(xmlContentEditor.DROP_ACTION_APPEND_CHILD, 
									(Element)childView.getElement().getParentNode());
					}
					if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
						event.acceptDrag(DnDConstants.ACTION_MOVE);
					else if (event.isDataFlavorSupported(CommandTransferable.commandFlavor))
						event.acceptDrag(DnDConstants.ACTION_MOVE);
					return;
				}
				else
				{
					xmlContentEditor.setDropData(xmlContentEditor.DROP_ACTION_NOT_ALLOWED, null);
					event.rejectDrag();
				}
				childVertPos += childView.getHeight() + SPACING_VERTICAL;

				if (lastOne == true)
					break;
			}

		}
		else
		{
			xmlContentEditor.setDropData(xmlContentEditor.DROP_ACTION_NOT_ALLOWED, null);
			event.rejectDrag();
		}
	}

}
