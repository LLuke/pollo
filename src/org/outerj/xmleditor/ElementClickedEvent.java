package org.outerj.xmleditor;

import org.w3c.dom.Element;
import java.awt.event.MouseEvent;


/**
  Event that is fired when the user clicks on an element in the editor.

  TODO: maybe it would be better to use real DOM events for this? Although
  I think these types of events should be associated with the view and not the model.
 */
public class ElementClickedEvent
{
	Element element;
	MouseEvent mouseEvent;


	/**
	  @param element the element which has been clicked on.
	 */
	public ElementClickedEvent(Element element, MouseEvent mouseEvent)
	{
		this.element = element;
		this.mouseEvent = mouseEvent;
	}

	public MouseEvent getMouseEvent()
	{
		return mouseEvent;
	}

	public Element getElement()
	{
		return element;
	}
}
