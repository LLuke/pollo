package org.outerj.pollo.displayspeceditor.model;

import java.awt.*;

/**
 * Specification of an element, part of a {@link ElementSpecifications} list.
 *
 * @author Bruno Dumon
 */
public class ElementSpecification
{
    private String elementPath;
    private AttributeSpecifications attributeSpecifications;
    private ElementSpecifications elementSpecifications;
    private Color color;

    public ElementSpecification(String elementPath, ElementSpecifications elementSpecifications)
    {
        this.elementPath = elementPath;
        this.color = new Color(200, 200, 200);
        this.elementSpecifications = elementSpecifications;
        attributeSpecifications = new AttributeSpecifications(this);
    }

    public void setColor(Color newColor)
    {
        // currently disable undo for colors, because moving the sliders of the ColorSelector would
        // cause a bunch of useless undos to be created. Need to think of a solution...
        //elementSpecifications.getDisplaySpecification().addUndoable(new ChangeColorUndoable(color));
        color = newColor;
        elementSpecifications.elementChanged(this);
    }

    public Color getColor()
    {
        return color;
    }

    public String getElementPath()
    {
        return elementPath;
    }

    public String toString()
    {
        return elementPath;
    }

    public AttributeSpecifications getAttributes()
    {
        return attributeSpecifications;
    }

    public ElementSpecifications getElementSpecifications()
    {
        return elementSpecifications;
    }

    public class ChangeColorUndoable implements DisplaySpecification.Undoable
    {
        private Color oldColor;

        public ChangeColorUndoable(Color oldColor)
        {
            this.oldColor = oldColor;
        }

        public void undo()
        {
            color = oldColor;
            elementSpecifications.elementChanged(ElementSpecification.this);
        }

        public String getDescription()
        {
            return "change color of " + elementPath;
        }
    }
}
