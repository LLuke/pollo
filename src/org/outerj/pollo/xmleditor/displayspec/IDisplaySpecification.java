package org.outerj.pollo.xmleditor.displayspec;

import java.awt.*;

/**
 * Interface that must be implemented by display specifications.
 * A display specification contains the information about how elements
 * should be rendered on the screen. It plays the same role as CSS
 * does for browsers.
 *
 * @author Bruno Dumon
 */
public interface IDisplaySpecification
{
	/**
	 * Get the background fill color.
	 */
	public Color getBackgroundColor();

	/**
	 * Gets the element specification, this is an object
	 * containing attributes for how this element should be
	 * rendered.
	 */
	public ElementSpec getElementSpec(String namespaceURI, String localName);

	/**
	 * Get the font to be used for attribute names.
	 */
	public Font getAttributeNameFont();

	/**
	 * Get the font to be used for attribute values.
	 */
	public Font getAttributeValueFont();

	/**
	 * Get the font to be used for element names.
	 */
	public Font getElementNameFont();

}
