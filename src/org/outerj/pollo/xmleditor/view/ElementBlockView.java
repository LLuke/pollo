package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.DisplaySpecification;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.NodeClickedEvent;
import org.outerj.pollo.xmleditor.DisplaySpecification.ElementSpec;
import org.outerj.pollo.xmleditor.DisplaySpecification.AttributeSpec;
import org.outerj.pollo.xmleditor.XmlTransferable;
// import org.outerj.pollo.xmleditor.CommandTransferable; deprecated

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
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MutationEvent;


public class ElementBlockView extends BlockView
{
	protected ElementSpec elementSpec;
	protected String elementName;
	protected int titleHeight = 10;
	protected int contentHeight;
	protected int collapseSignTop;
	protected Element element;
	protected Shape elementShape;
	protected ArrayList childViewList = new ArrayList(10);
	protected DisplaySpecification displaySpec;

	// everything for the attributes
	protected ArrayList attrViewInfoList = new ArrayList();
	protected Font attributeValueFont;

	// some rendering constants
	protected static final int SPACING_VERTICAL = 5;
	protected static final int BORDER_WIDTH = 6; //4;
	protected static final int SPACING_HORIZONTAL = BORDER_WIDTH + 7;
	protected static final int EMPTY_CONTENT_HEIGHT = 10;
	protected static final int COLLAPS_ICON_WIDTH = 15;
	protected static final int END_MARKER_HEIGHT = 10;
	protected static final int ATTR_SPACING = 9;
	protected static final int ATTR_NAME_VALUE_SPACING = 2;

	protected static final int NOT_CALCULATED    = -1;

	public ElementBlockView(View parentView, Element element, XmlEditor xmlEditor)
	{
		super(parentView, xmlEditor);
		this.element = element;

		// register this view as an eventlistener for changes to the element
		((EventTarget)element).addEventListener("DOMAttrModified", this, false);
		((EventTarget)element).addEventListener("DOMNodeInserted", this, false);
		((EventTarget)element).addEventListener("DOMNodeRemoved", this, false);

		// get the ElementSpec from the DisplaySpec
		displaySpec = xmlEditor.getDisplaySpec();
		String uri = element.getNamespaceURI();
		String name = element.getLocalName();
		this.elementSpec = displaySpec.getElementSpec(uri, name);

		// create instances of AttrViewInfo for all the attributes we need to show
		int numberOfAttrs = elementSpec.attributesToShow.size();
		AttrViewInfo attrViewInfo;
		for (int i = 0; i < numberOfAttrs; i++)
		{
			attrViewInfo = new AttrViewInfo();
			attrViewInfo.attributeSpec = (AttributeSpec)elementSpec.attributesToShow.get(i);
			attrViewInfoList.add(attrViewInfo);
		}

		// make the string for the element name
		String prefix = element.getPrefix();
		String qname = element.getLocalName();
		if (prefix != null && prefix.length() > 0)
			qname = prefix + ":" + qname;
		this.elementName = qname;
	}

	public void paint(Graphics gr, int startH, int startV)
	{
		Graphics2D g = (Graphics2D)gr;

		// make the shape
		if (childViewList.size()  > 0)
		{
			Polygon poly = new Polygon();
			elementShape = poly;
			// starting at the top left point
			poly.addPoint(startH, startV);
			poly.addPoint(startH + width, startV);
			poly.addPoint(startH + width, startV + titleHeight);
			if (!isCollapsed())
			{
				poly.addPoint(startH + BORDER_WIDTH, startV + titleHeight);
				poly.addPoint(startH + BORDER_WIDTH, startV + titleHeight + getContentHeight());
				poly.addPoint(startH + BORDER_WIDTH + 4, startV + titleHeight + getContentHeight());
				poly.addPoint(startH, startV + titleHeight + getContentHeight() + END_MARKER_HEIGHT);
			}
			else
			{
				poly.addPoint(startH + BORDER_WIDTH + 4, startV + titleHeight);
				poly.addPoint(startH, startV + titleHeight + END_MARKER_HEIGHT);
			}

		}
		else
		{
			// just draw a rectangle
			elementShape = new Rectangle(startH, startV, width, titleHeight);
		}

		g.setColor(elementSpec.backgroundColor);
		g.fill(elementShape);

		g.setColor(Color.black);
		if (xmlEditor.getSelectedNode() == element)
			g.setStroke(BlockView.STROKE_HEAVY);
		else
			g.setStroke(BlockView.STROKE_LIGHT);

		g.draw(elementShape);
	
		g.setStroke(BlockView.STROKE_LIGHT);
		if (childViewList.size()  > 0)
		{
			// draw + or - sign
			drawCollapseSign(g, isCollapsed(), startH + BORDER_WIDTH, startV + collapseSignTop);

			// now draw the children, but only those that need updating
			Iterator childrenIt = childViewList.iterator();

			int totalHeight = titleHeight;
			int clipStartVertical = (int)g.getClipBounds().getY();
			int clipEndVertical = clipStartVertical + (int)g.getClipBounds().getHeight();
			if (!isCollapsed())
			{
				int childVertPos = startV + titleHeight + SPACING_VERTICAL;
				while (childrenIt.hasNext())
				{
					View view = (View)childrenIt.next();
					if (view.needsRepainting(childVertPos, clipStartVertical, clipEndVertical))
						view.paint(g, startH + SPACING_HORIZONTAL, childVertPos);
					childVertPos += view.getHeight() + SPACING_VERTICAL;
				}
			}
		}

		int baseline = startV + g.getFontMetrics(displaySpec.getElementNameFont()).getAscent() + 2;
		// draw the element name
		g.setFont(displaySpec.getElementNameFont());
		g.drawString(elementName, startH + 20, baseline);

		// draw the attributes
		// the position of the attribute names and values was already calculated in the
		// layoutAttributes method. Here they just need to be drawn, but it is also checked
		// if they fit on the screen, otherwise they are clipped and '...' is appended.
		AttrViewInfo attrViewInfo;
		int numberOfAttrs = attrViewInfoList.size();
		int remainingAttrSpace; // the space remaining for attributes
		int requiredSpace;
		g.setFont(displaySpec.getAttributeValueFont());
		int dotsWidth = g.getFontMetrics().stringWidth("...");
		for (int i = 0; i < numberOfAttrs; i++)
		{
			attrViewInfo = (AttrViewInfo)attrViewInfoList.get(i);
			if (attrViewInfo.visible)
			{
				// attribute name
				g.setFont(displaySpec.getAttributeNameFont());
				remainingAttrSpace = width - attrViewInfo.namePos;
				requiredSpace = g.getFontMetrics().stringWidth(attrViewInfo.name) + dotsWidth;
				if (requiredSpace > remainingAttrSpace)
				{
					int c = clipText(attrViewInfo.name, remainingAttrSpace, g.getFontMetrics(), dotsWidth);
					g.drawString(attrViewInfo.name.substring(0, c) + "...", startH + attrViewInfo.namePos, baseline);
					break;

				}
				else
				{
					g.drawString(attrViewInfo.name, startH + attrViewInfo.namePos, baseline);
				}

				// attribute value
				g.setFont(displaySpec.getAttributeValueFont());
				remainingAttrSpace = width - attrViewInfo.valuePos;
				requiredSpace = g.getFontMetrics().stringWidth(attrViewInfo.value) + dotsWidth;
				if (requiredSpace > (remainingAttrSpace - ATTR_SPACING))
				{
					int c = clipText(attrViewInfo.value, remainingAttrSpace, g.getFontMetrics(), dotsWidth);
					g.drawString(attrViewInfo.value.substring(0, c) + "...", startH + attrViewInfo.valuePos, baseline);
					break;
				}
				else
				{
					g.drawString(attrViewInfo.value, startH + attrViewInfo.valuePos, baseline);
				}
			}
		}
	}

	/**
	  Layouts this view (and it's children) to fit to
	  the given width.

	  @param startV is relative to the parent, thus it is 0 for the first child

	  */
	public void layout(int width)
	{
		// init
		this.titleHeight = xmlEditor.getGraphics().getFontMetrics(displaySpec.getElementNameFont()).getHeight() + 4;
		this.width = width;

		// layout the attributes
		layoutAttributes();

		collapseSignTop = (titleHeight / 2) - 5;

		// layout the children of this view
		if (childViewList.size() > 0)
		{
			Iterator childrenIt = childViewList.iterator();
			while (childrenIt.hasNext())
			{
				View view = (View)childrenIt.next();
				view.layout(width - SPACING_HORIZONTAL);
			}
		}

		contentHeight = NOT_CALCULATED;
	}


	/**
	  This method calculates the positions off the attributes. Called during initial layout
	  or when attributes have changed/removed/added.
	 */
	public void layoutAttributes()
	{
		Graphics graphics = xmlEditor.getGraphics();
		FontMetrics attrNameFontMetrics  = graphics.getFontMetrics(displaySpec.getAttributeNameFont());
		FontMetrics attrValueFontMetrics = graphics.getFontMetrics(displaySpec.getAttributeValueFont());

		int elementNameWidth = graphics.getFontMetrics(displaySpec.getElementNameFont()).stringWidth(elementName);
		int attrPos = BORDER_WIDTH + COLLAPS_ICON_WIDTH + elementNameWidth + ATTR_SPACING;
		AttrViewInfo attrViewInfo;
		int numberOfAttrs = attrViewInfoList.size();
		for (int i = 0; i < numberOfAttrs; i++)
		{
			attrViewInfo = (AttrViewInfo)attrViewInfoList.get(i);
			Attr attr;
			if (attrViewInfo.attributeSpec.nsUri == null || attrViewInfo.attributeSpec.nsUri.equals(""))
			{
				attr = element.getAttributeNode(attrViewInfo.attributeSpec.localName);
			}
			else
			{
				attr = element.getAttributeNodeNS(attrViewInfo.attributeSpec.nsUri,
						attrViewInfo.attributeSpec.localName);
			}

			if (attr == null)
			{
				attrViewInfo.visible = false;
			}
			else
			{
				attrViewInfo.value = attr.getValue();
				if (attrViewInfo.name == null)
				{
					String prefix = attr.getPrefix();
					String qname = attr.getLocalName();
					if (prefix != null)
						qname = prefix + ":" + qname;

					qname += ":"; // this is the colon seperating name and value
					attrViewInfo.name = qname;
				}
				attrViewInfo.namePos = attrPos;
				attrPos += attrNameFontMetrics.stringWidth(attrViewInfo.name);
				attrPos += ATTR_NAME_VALUE_SPACING;
				attrViewInfo.valuePos = attrPos;
				attrPos += attrValueFontMetrics.stringWidth(attrViewInfo.value);
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

	public void mousePressed(MouseEvent e, int startH, int startV)
	{
		// if clicked on the collapse/expand button
		if ((e.getY() > startV + collapseSignTop) && (e.getY() < startV + collapseSignTop + 10)
				&& (e.getX() > startH + BORDER_WIDTH) && (e.getX() < startH + BORDER_WIDTH + 10))
		{
			if (isCollapsed())
				expand();
			else
				collapse();
		}
		else if ( e.getY() > startV && e.getY() < startV + titleHeight
			   && e.getX() > startH + BORDER_WIDTH + 10)
		{
			NodeClickedEvent nce = new NodeClickedEvent(element, e);
			xmlEditor.fireNodeClickedEvent(nce);
			
			// make that the current element is indicated
			markAsSelected(startH, startV);
		}
		else
		{
			Iterator childrenIt = childViewList.iterator();
			int childVertPos = startV + titleHeight + SPACING_VERTICAL;
			while (childrenIt.hasNext())
			{
				View childView = (View)childrenIt.next();
				if (e.getY() > childVertPos && e.getY() < childVertPos + childView.getHeight())
				{
					childView.mousePressed(e, startH + SPACING_HORIZONTAL, childVertPos);
					break;
				}
				childVertPos += childView.getHeight() + SPACING_VERTICAL;
			}
		}
	}


	public int getHeight()
	{
		if (!isCollapsed() && childViewList.size() > 0)
			return titleHeight + getContentHeight() + END_MARKER_HEIGHT; 
		else if (isCollapsed())
			return titleHeight + END_MARKER_HEIGHT; 
		else
			return titleHeight; 
	}

	public int getContentHeight()
	{
		if (contentHeight == NOT_CALCULATED)
		{
			Iterator childrenIt = childViewList.iterator();

			if (childViewList.size() > 0)
			{
				int totalChildrenHeight = 0;

				while (childrenIt.hasNext())
				{
					View view = (View)childrenIt.next();
					totalChildrenHeight += view.getHeight() + SPACING_VERTICAL;
				}

				contentHeight = totalChildrenHeight + SPACING_VERTICAL;
			}
			else
			{
				contentHeight = 0;
			}
		}
		return contentHeight;
	}

	public void invalidateHeight()
	{
		contentHeight = NOT_CALCULATED;
	}

	public boolean isCollapsable()
	{
		if (childViewList.size() > 0)
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
				if (((Node)e.getTarget()).getParentNode() == element)
				{
					removeViewForRemovedChild((Node)e.getTarget());
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
					xmlEditor.repaint(xmlEditor.getVisibleRect());
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

	public Node getNode()
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
			if (XmlEditor.isNodeTypeSupported(node.getNodeType()))
			{
				View correspondingView = null;
				try
				{
					correspondingView = (View)childViewList.get(elementChildNodeCounter);
				}
				catch (IndexOutOfBoundsException e) { }

				if (correspondingView == null || correspondingView.getNode() != node)
				{
					View childView = xmlEditor.createView(node, this);
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
			applyHeightChange(diff);
		}
	}

	public void removeViewForRemovedChild(Node removedChild)
	{
		NodeList children = element.getChildNodes();
		int relevantChildNodeCounter = 0; // only element and comment nodes are relevant for now
		boolean hasChildren = childViewList.size() > 0;
		int heightChangeAmount = 0;
		int oldHeight = getHeight();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if ((node == removedChild))
			{
				View correspondingView = (View)childViewList.get(relevantChildNodeCounter);
				heightChangeAmount -= correspondingView.getHeight() + SPACING_VERTICAL;
				correspondingView.removeEventListeners();
				childViewList.remove(relevantChildNodeCounter);
				break;
				
			}
			if (XmlEditor.isNodeTypeSupported(node.getNodeType()))
				relevantChildNodeCounter++;
		}
		if (childViewList.size() == 0)
		{
			// if last child is removed, contentheight should become zero
			heightChangeAmount = 0 - contentHeight;
		}
		if (heightChangeAmount != 0)
		{
			contentHeight += heightChangeAmount;
			if (!isCollapsed())
			{
				int newHeight = getHeight();
				int diff = newHeight - oldHeight;
				applyHeightChange(diff);
			}
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


	private DocumentFragment createDocumentFragment(Node node)
	{
		DocumentFragment documentFragment = xmlEditor.getXmlModel().getDocument().createDocumentFragment();
		documentFragment.appendChild(node.cloneNode(true));
		return documentFragment;
	}


	public void dragGestureRecognized(DragGestureEvent event, int startH, int startV)
	{
		// only allow dragging with left mouse button (otherwise got problems on linux
		// when showing context menus)
		if ((event.getTriggerEvent().getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
			return;

		Point p = event.getDragOrigin();
		if ( p.getY() > startV && p.getY() < startV + titleHeight
			   && p.getX() > startH)
		{
			if (parentView == null)
			{
				// the mainView may not be dragged
				return;
			}
			xmlEditor.setDraggingNode(element);
			xmlEditor.getDragSource().startDrag(event, DragSource.DefaultMoveDrop,
					new XmlTransferable(createDocumentFragment(element)), xmlEditor);
		}
		else
		{
			Iterator childrenIt = childViewList.iterator();
			int childVertPos = startV + titleHeight + SPACING_VERTICAL;
			while (childrenIt.hasNext())
			{
				View childView = (View)childrenIt.next();
				if (p.getY() > childVertPos && p.getY() < childVertPos + childView.getHeight())
				{
					childView.dragGestureRecognized(event, startH + SPACING_HORIZONTAL, childVertPos);
					break;
				}
				childVertPos += childView.getHeight() + SPACING_VERTICAL;
			}
		}
	}

	public void dragOver(DropTargetDragEvent event, int startH, int startV)
	{
		if (isCollapsed())
		{
			xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
			event.rejectDrag();
			return;
		}

		Point p = event.getLocation();

		if (p.getY() > startV && p.getY() < startV + titleHeight && p.getX() > startH)
		{ // it is on this element
			if (childViewList.size() > 0)
			{
				// dropping is only allowed if this element has no children yet
				xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
				event.rejectDrag();
				return;
			}

			// draw drag over effect
			Rectangle rect = new Rectangle(startH, startV, width, titleHeight);
			xmlEditor.setDragOverEffectRedraw(new Rectangle(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2));
			Graphics2D graphics = (Graphics2D)xmlEditor.getGraphics();
			graphics.setColor(new Color(255, 0, 0));
			graphics.setStroke(new BasicStroke(2));
			graphics.draw(rect);

			xmlEditor.setDropData(xmlEditor.DROP_ACTION_APPEND_CHILD, element);
			if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
				event.acceptDrag(DnDConstants.ACTION_MOVE);
			/* CommandTransferable -- deprecated.
			else if (event.isDataFlavorSupported(CommandTransferable.commandFlavor))
				event.acceptDrag(DnDConstants.ACTION_MOVE);
			*/
			return;
		}
		else if (p.getX() > startH && p.getX() < (startH + width - SPACING_HORIZONTAL) ) // maybe it's between to elements
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
					if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor) /* || deprecated
							event.isDataFlavorSupported(CommandTransferable.commandFlavor)*/)
					{
						// draw drag over effect
						Rectangle rect = new Rectangle(startH + SPACING_HORIZONTAL, childVertPos - 3,
							   	width - SPACING_HORIZONTAL, 2);
						xmlEditor.setDragOverEffectRedraw(rect);
						Graphics2D graphics = (Graphics2D)xmlEditor.getGraphics();
						graphics.setColor(new Color(255, 0, 0));
						graphics.fill(rect);

						if (!lastOne)
							xmlEditor.setDropData(xmlEditor.DROP_ACTION_INSERT_BEFORE, childView.getNode());
						else
							xmlEditor.setDropData(xmlEditor.DROP_ACTION_APPEND_CHILD, 
									childView.getNode().getParentNode());
					}
					if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
						event.acceptDrag(DnDConstants.ACTION_MOVE);
					/* CommandTransferable is deprecated
					else if (event.isDataFlavorSupported(CommandTransferable.commandFlavor))
						event.acceptDrag(DnDConstants.ACTION_MOVE);
					*/
					return;
				}
				else
				{
					xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
					event.rejectDrag();
				}
				childVertPos += childView.getHeight() + SPACING_VERTICAL;

				if (lastOne == true)
					break;
			}

		}
		else
		{
			xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
			event.rejectDrag();
		}
	}

	/**
	 * Given a maximum allowed width (in pixels), this method calculates how much text
	 * will fit on one line, assuming that if it doesn't fit, three dots are appended (...).
	 */
	protected int clipText(String text, int maxwidth, FontMetrics fontMetrics, int dotsWidth)
	{
		int totalWidth = dotsWidth;

		int i = 0;
		int length = text.length();
		for (; i < length; i++)
		{
			totalWidth += fontMetrics.charWidth(text.charAt(i));
			if (totalWidth > maxwidth)
				break;
		}

		return i;
	}

	public void collapseAll()
	{
		// collapse myself
		collapse();

		// collapse my child views
		Iterator childrenIt = childViewList.iterator();
		while (childrenIt.hasNext())
		{
			View view = (View)childrenIt.next();
			view.collapseAll();
		}
	}

	public void expandAll()
	{
		// expand my child views
		Iterator childrenIt = childViewList.iterator();
		while (childrenIt.hasNext())
		{
			View view = (View)childrenIt.next();
			view.expandAll();
		}

		// expand myself
		expand();

	}
}
