package org.outerj.pollo.displayspeceditor.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list of attribute specifications, part of an {@link ElementSpecification}.
 * It is also a ListModel, so that it can be used in conjunction with JList
 * to show a list of the attributes.
 * It will also record undo-events for removal and addition of attributes.
 *
 * @author Bruno Dumon
 */
public class AttributeSpecifications extends AbstractListModel
{
    private ArrayList attributes = new ArrayList();
    private ElementSpecification elementSpecification;

    public AttributeSpecifications(ElementSpecification elementSpecification)
    {
        this.elementSpecification = elementSpecification;
    }

    public int getSize()
    {
        return attributes.size();
    }

    public Object getElementAt(int index)
    {
        return attributes.get(index);
    }

    public void addAttribute(String qName) throws InvalidQNameException
    {
        QNameSupport.checkQName(elementSpecification.getElementSpecifications().getDisplaySpecification(), qName);
        if (attributes.contains(qName))
            throw new InvalidQNameException("Duplicate attribute name: " + qName);
        attributes.add(qName);
        int newAttrPos = attributes.size() - 1;
        fireIntervalAdded(this, newAttrPos, newAttrPos);
        elementSpecification.getElementSpecifications().getDisplaySpecification().addUndoable(new AddAttributeUndoable(qName));
    }

    public void removeAttribute(int index)
    {
        String attribute = (String)attributes.remove(index);
        elementSpecification.getElementSpecifications().getDisplaySpecification().addUndoable(new RemoveAttributeUndoable(attribute, index));
        fireIntervalRemoved(this, index, index);
    }

    public String toCommaSeparatedString()
    {
        StringBuffer result = new StringBuffer();

        Iterator attrIt = attributes.iterator();
        while (attrIt.hasNext())
        {
            if (result.length() > 0)
                result.append(",");

            String name = (String)attrIt.next();
            result.append(name);
        }

        return result.toString();
    }

    /**
     * @return the new index
     */
    public int moveAttributeDown(int index)
    {
        String attribute = (String)attributes.get(index);
        removeAttribute(index);
        index = index >= attributes.size() ? attributes.size() : index + 1;
        attributes.add(index, attribute);
        fireIntervalAdded(this, index, index);
        return index;
    }

    /**
     * @return the new index
     */
    public int moveAttributeUp(int index)
    {
        String attribute = (String)attributes.get(index);
        removeAttribute(index);
        index = index <= 0 ? 0 : index - 1;
        attributes.add(index, attribute);
        fireIntervalAdded(this, index, index);
        return index;
    }

    public class AddAttributeUndoable implements DisplaySpecification.Undoable
    {
        private String addedAttribute;

        public AddAttributeUndoable(String attribute)
        {
            this.addedAttribute = attribute;
        }

        public void undo()
        {
            int index = attributes.indexOf(addedAttribute);
            attributes.remove(addedAttribute);
            fireIntervalRemoved(this, index, index);
        }

        public String getDescription()
        {
            return "addition of attribute " + addedAttribute + " to " + elementSpecification.getElementPath();
        }
    }

    public class RemoveAttributeUndoable implements DisplaySpecification.Undoable
    {
        private String removedAttribute;
        private int position;

        public RemoveAttributeUndoable(String removedAttribute, int position)
        {
            this.removedAttribute = removedAttribute;
            this.position = position;
        }

        public void undo()
        {
            attributes.add(position, removedAttribute);
            fireIntervalAdded(AttributeSpecifications.this, position, position);
        }

        public String getDescription()
        {
            return "remove attribute " + removedAttribute + " of " + elementSpecification.getElementPath();
        }
    }
}
