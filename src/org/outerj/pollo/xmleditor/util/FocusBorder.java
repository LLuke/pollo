package org.outerj.pollo.xmleditor.util;

import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.Color;

import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JComponent;


/**
 * This class can be registered as a focuslistener to a component,
 * and it will display a border around a given component (possibly
 * another component than the first) when it gets the focus.
 */
public class FocusBorder implements FocusListener
{
	protected JComponent component;
	protected Border border = BorderFactory.createLineBorder(Color.red, 1);
	protected Border oldBorder;

	/**
	 * @param component the component around which the border
	 * should be shown
	 */
	public FocusBorder(JComponent component)
	{
		this.component = component;
	}

	public void focusGained(FocusEvent event)
	{
		if (oldBorder == null)
			oldBorder = component.getBorder();
		component.setBorder(border);
	}

	public void focusLost(FocusEvent event)
	{
		component.setBorder(oldBorder);
	}
}
