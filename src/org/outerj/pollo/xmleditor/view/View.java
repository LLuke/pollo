package org.outerj.pollo.xmleditor.view;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;

/**
  A view contains the visual represantation of an element from
  the corresponding document.

  A view in itself doesn't know it's absolute coordinates, therfore
  it gets it's top-left coordinate in its paint/mousePressed method.
 */
public interface View extends EventListener
{
	public void paint(Graphics gr, int startHorizontal, int startVertical);

	/**
	  The layout method is responsible for layouting the view, eg
	  defining where everything should be placed etc, so that the paint
	  method doesn't need to do a lot of calculations.

	  @param width the available width

	 */
	public void layout(int width);

	/**
	  Returns the height of this view.
	 */
	public int getHeight();

	/**
	  Relayouts the view when it's height has changed.
	  Used by the collapsing features.
	 */
	public void heightChanged(int amount);

	/**
	  Relayouts the view when it's width is changed, like
	  when the user resizes when the window. Returns the
	  new height.
	 */
	public int widthChanged(int amount);

	/**
	  Determines whether this view needs to be repainted.
	 */
	public boolean needsRepainting(int startVertical, int clipStartVertical, int clipEndVertical);

	public void addChildView(View childView);

	public void mousePressed          (MouseEvent e,              int startH, int startV);
	public void dragGestureRecognized (DragGestureEvent event,    int startH, int startV);
	public void dragOver              (DropTargetDragEvent event, int startH, int startV);

	public Node getNode();

	public void removeEventListeners();

	public boolean isCollapsed();
	public void collapse();
	public void collapseAll();
	public void expand();
	public void expandAll();
}
