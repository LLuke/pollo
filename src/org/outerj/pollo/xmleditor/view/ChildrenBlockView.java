package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlTransferable;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;


public abstract class ChildrenBlockView extends BlockView
{
	private int contentHeight;
	private Node node;
	private ArrayList childViewList = new ArrayList(10);

	// some rendering constants
	protected static final int SPACING_VERTICAL = 5;

	protected static final int NOT_CALCULATED    = -1;

	public ChildrenBlockView(View parentView, Node node, XmlEditor xmlEditor)
	{
		super(parentView, xmlEditor);
		this.node = node;

		// register this view as an eventlistener for changes to the element
		((EventTarget)node).addEventListener("DOMNodeInserted", this, false);
		((EventTarget)node).addEventListener("DOMNodeRemoved", this, false);
	}


	public void paint(Graphics gr, int startH, int startV)
	{
		Graphics2D g = (Graphics2D)gr;

		if (childViewList.size()  > 0)
		{
			// now draw the children, but only those that need updating
			Iterator childrenIt = childViewList.iterator();

			int totalHeight = getHeaderHeight();
			int clipStartVertical = (int)g.getClipBounds().getY();
			int clipEndVertical = clipStartVertical + (int)g.getClipBounds().getHeight();
			if (!isCollapsed())
			{
				int childVertPos = startV + getHeaderHeight() + SPACING_VERTICAL;
				while (childrenIt.hasNext())
				{
					View view = (View)childrenIt.next();
					if (view.needsRepainting(childVertPos, clipStartVertical, clipEndVertical))
						view.paint(g, startH + getLeftMargin(), childVertPos);
					childVertPos += view.getHeight() + SPACING_VERTICAL;
				}
			}
		}
	}


	public void layout(int width)
	{
		// layout the children of this view
		if (childViewList.size() > 0)
		{
			Iterator childrenIt = childViewList.iterator();
			while (childrenIt.hasNext())
			{
				View view = (View)childrenIt.next();
				view.layout(width - getLeftMargin());
			}
		}

		contentHeight = NOT_CALCULATED;
	}



	/**
	 * Recalculates the height of this view and recursively of its parent views
	 * when the height has changed because of element removal/addition or when
	 * collapsing/expanding.
	 */
	public void heightChanged(int amount)
	{
		if (!isCollapsed())
		{
			contentHeight = contentHeight + amount;
			if (parentView != null)
				parentView.heightChanged(amount);
			else
			{
				resetSize();
			}
		}
	}

	public int widthChanged(int amount)
	{
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
		Iterator childrenIt = childViewList.iterator();
		int childVertPos = startV + getHeaderHeight() + SPACING_VERTICAL;
		while (childrenIt.hasNext())
		{
			View childView = (View)childrenIt.next();
			if (e.getY() > childVertPos && e.getY() < childVertPos + childView.getHeight())
			{
				childView.mousePressed(e, startH + getLeftMargin(), childVertPos);
				break;
			}
			childVertPos += childView.getHeight() + SPACING_VERTICAL;
		}
	}

	public int getHeight()
	{
		if (!isCollapsed() && childViewList.size() > 0)
			return getHeaderHeight() + getContentHeight() + getFooterHeight(); 
		else if (isCollapsed())
			return getHeaderHeight() + getFooterHeight(); 
		else
			return getHeaderHeight(); 
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
				if (((MutationEvent)e).getRelatedNode() != node)
					return;
				e.stopPropagation();
				createViewsForNewChildren();
			}
			else if (e.getType().equalsIgnoreCase("DOMNodeRemoved"))
			{
				if (((Node)e.getTarget()).getParentNode() == node)
				{
					removeViewForRemovedChild((Node)e.getTarget());
					e.stopPropagation();
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
		return node;
	}


	/**
	  This method updates the list of childviews: if elements were inserted
	  in the dom, new corresponding views are created. Also updates the
	  layout.
	 */
	public void createViewsForNewChildren()
	{
		NodeList children = node.getChildNodes();
		int nodeChildNodeCounter = 0;
		boolean hasChildren = childViewList.size() > 0;
		boolean heightChanged = false;
		int oldHeight = getHeight();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if (XmlEditor.isNodeTypeSupported(node.getNodeType()))
			{
				View correspondingView = null;
				try
				{
					correspondingView = (View)childViewList.get(nodeChildNodeCounter);
				}
				catch (IndexOutOfBoundsException e) { }

				if (correspondingView == null || correspondingView.getNode() != node)
				{
					View childView = xmlEditor.createView(node, this);
					childView.layout(width - getLeftMargin());
					childViewList.add(nodeChildNodeCounter, childView);
					heightChanged = true;
				}
				nodeChildNodeCounter++;
			}
		}

		if (heightChanged)
		{
			invalidateHeight();
			int newHeight = getHeight();
			int diff = newHeight - oldHeight;
			applyHeightChange(diff);
		}
	}

	public void removeViewForRemovedChild(Node removedChild)
	{
		NodeList children = node.getChildNodes();
		int relevantChildNodeCounter = 0; // only nodes that are displayed count (currently e.g. not PI's)
		boolean hasChildren = childViewList.size() > 0;
		boolean heightChanged = false;
		int oldHeight = getHeight();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if ((node == removedChild))
			{
				View correspondingView = (View)childViewList.get(relevantChildNodeCounter);
				heightChanged = true;
				correspondingView.removeEventListeners();
				childViewList.remove(relevantChildNodeCounter);
				break;
				
			}
			if (XmlEditor.isNodeTypeSupported(node.getNodeType()))
				relevantChildNodeCounter++;
		}
		if (heightChanged)
		{
			invalidateHeight();
			int newHeight = getHeight();
			int diff = newHeight - oldHeight;
			applyHeightChange(diff);
		}
	}

	public void removeEventListeners()
	{
		((EventTarget)node).removeEventListener("DOMNodeInserted", this, false);
		((EventTarget)node).removeEventListener("DOMNodeRemoved", this, false);

		Iterator childViewListIt = childViewList.iterator();
		while (childViewListIt.hasNext())
		{
			View childView = (View)childViewListIt.next();
			childView.removeEventListeners();
		}
	}


	protected DocumentFragment createDocumentFragment(Node node)
	{
		DocumentFragment documentFragment = xmlEditor.getXmlModel().getDocument().createDocumentFragment();
		documentFragment.appendChild(node.cloneNode(true));
		return documentFragment;
	}


	public void dragGestureRecognized(DragGestureEvent event, int startH, int startV)
	{
		Iterator childrenIt = childViewList.iterator();
		int childVertPos = startV + getHeaderHeight() + SPACING_VERTICAL;
		Point p = event.getDragOrigin();
		while (childrenIt.hasNext())
		{
			View childView = (View)childrenIt.next();
			if (p.getY() > childVertPos && p.getY() < childVertPos + childView.getHeight())
			{
				childView.dragGestureRecognized(event, startH + getLeftMargin(), childVertPos);
				break;
			}
			childVertPos += childView.getHeight() + SPACING_VERTICAL;
		}
	}


	public void dragOver(DropTargetDragEvent event, int startH, int startV)
	{
		Iterator childrenIt = childViewList.iterator();
		int childVertPos = startV + getHeaderHeight() + SPACING_VERTICAL;
		View childView = null;
		boolean lastOne = false;
		Point p = event.getLocation();
		while (childrenIt.hasNext() || (lastOne = true))
		{
			if (!lastOne)
				childView = (View)childrenIt.next();

			if (!lastOne && p.getY() > childVertPos && p.getY() < childVertPos + childView.getHeight())
			{
				childView.dragOver(event, startH + getLeftMargin(), childVertPos);
				break;
			}
			else if (p.getY() > (childVertPos - SPACING_VERTICAL) && p.getY() < childVertPos)
			{
				if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
				{
					// draw drag over effect
					Rectangle rect = new Rectangle(startH + getLeftMargin(), childVertPos - 3, width, 2);
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
					event.acceptDrag(event.getDropAction());
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

	public abstract int getHeaderHeight();

	public abstract int getFooterHeight();

	public abstract int getLeftMargin();

	public boolean hasChildren()
	{
		return childViewList.size() > 0;
	}

	/**
	 * Overidden method from BlockView class.
	 */
	public int getVerticalPosition(View wantedView)
	{
		int startV = getVerticalPosition();

		Iterator childrenIt = childViewList.iterator();

		if (!isCollapsed())
		{
			int childVertPos = startV + getHeaderHeight() + SPACING_VERTICAL;
			while (childrenIt.hasNext())
			{
				View view = (View)childrenIt.next();
				if (view == wantedView)
					return childVertPos;
				childVertPos += view.getHeight() + SPACING_VERTICAL;
			}
		}
		else
		{
			throw new RuntimeException("Cannot give the position of the childview is its parent is collapsed!");
		}
		throw new RuntimeException("The given view is not a childview.");
	}

	/**
	 * Overidden method from BlockView class.
	 */
	public int getHorizontalPosition(View wantedChildView)
	{
		return getHorizontalPosition() + getLeftMargin();
	}

	/**
	 * Overidden method from BlockView class.
	 */
	public View getNextSibling(View wantedChildView)
	{
		Iterator childrenIt = childViewList.iterator();
		while (childrenIt.hasNext())
		{
			View view = (View)childrenIt.next();
			if (view == wantedChildView)
			{
				try
				{
					return (View)childrenIt.next();
				}
				catch (java.util.NoSuchElementException e)
				{
					return null;
				}
			}
		}
		throw new RuntimeException("The given view is not a childview.");
	}

	/**
	 * Overidden method from BlockView class.
	 */
	public View getNext(boolean visible)
	{
		if (childViewList.size() > 0 && (visible ? !isCollapsed() : true))
		{
			return (View)childViewList.get(0);
		}
		else
		{
			return super.getNext(visible);
		}
	}

	/**
	 * Overidden method from BlockView class.
	 */
	public View getPreviousSibling(View wantedChildView)
	{
		Iterator childrenIt = childViewList.iterator();
		View previousView = null;
		while (childrenIt.hasNext())
		{
			View view = (View)childrenIt.next();
			if (view == wantedChildView)
			{
				return previousView;
			}
			previousView = view;
		}
		throw new RuntimeException("The given view is not a childview.");
	}


	public View getLastChild(boolean visible)
	{
		if (isCollapsed())
			return this;

		if (childViewList.size() > 0)
		{
			return ((View)childViewList.get(childViewList.size() - 1)).getLastChild(visible);
		}
		return this;
	}

	public View findNode(Node soughtNode)
	{
		if (soughtNode == node)
			return this;

		Node previousParent = soughtNode;
		while(true)
		{
			Node parentNode = previousParent.getParentNode();
			if (parentNode == null)
			{
				return null;
			}

			if (parentNode == node)
			{
				// zoek onder mijn kinderen
				Iterator childrenIt = childViewList.iterator();
				while (childrenIt.hasNext())
				{
					View view = (View)childrenIt.next();
					if (view.getNode() == previousParent)
						return view.findNode(soughtNode);
				}
			}
			previousParent = parentNode;
		}
	}
}
