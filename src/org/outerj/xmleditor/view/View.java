package org.outerj.xmleditor.view;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;

/**
  A view contains the visual represantation of an element from
  the corresponding document.

  A view in itself doesn't know it's absolute coordinates, therfore
  it gets it's top-left coordinate in its paint/mouseClicked method.
 */
public interface View extends EventListener
{
	public void paint(Graphics gr, int startHorizontal, int startVertical);

	/**
	  The layout method is responsible for layouting the view, eg
	  defining where everything should be placed etc, so that the paint
	  method doesn't need to do a lot of calculations.

	  @param width the available width

	  @return the height of this view
	 */
	public int layout(int width);

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
	  when the user resizes when the window.
	 */
	public int widthChanged(int amount);

	/**
	  Determines whether this view needs to be repainted.
	 */
	public boolean needsRepainting(int startVertical, int clipStartVertical, int clipEndVertical);

	public void addChildView(View childView);

	public void mouseClicked          (MouseEvent e,              int startH, int startV);
	public void dragGestureRecognized (DragGestureEvent event,    int startH, int startV);
	public void dragOver              (DropTargetDragEvent event, int startH, int startV);

	public Element getElement();

	public void removeEventListeners();
}
