package org.outerj.pollo.displayspeceditor.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * A list of element specifications, part of a {@link DisplaySpecification}.
 * This class is also a ListModel, so that the list of elements can be directly
 * shown in a JList.
 *
 * @author Bruno Dumon
 */
public class ElementSpecifications extends AbstractListModel
{
    private ArrayList elements = new ArrayList();
    private DisplaySpecification displaySpecification;

    public ElementSpecifications(DisplaySpecification displaySpecification)
    {
        this.displaySpecification = displaySpecification;
    }

    public int getSize()
    {
        return elements.size();
    }

    public Object getElementAt(int index)
    {
        return elements.get(index);
    }

    public ElementSpecification get(int index)
    {
        return (ElementSpecification)elements.get(index);
    }

    public ElementSpecification addElement(String elementPath)
            throws InvalidQNameException
    {
        StringTokenizer tokenizer = new StringTokenizer(elementPath, "/");
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            QNameSupport.checkQName(displaySpecification, token);
            int semiColonPos = token.indexOf(":");
            if (semiColonPos != -1)
            {
                String prefix = token.substring(0, semiColonPos);
                if (!displaySpecification.getNamespaces().prefixExists(prefix))
                    throw new InvalidQNameException("Namespace prefix is not declared: " + prefix);
            }
        }

        ElementSpecification elementSpecification = new ElementSpecification(elementPath, this);
        elements.add(elementSpecification);
        int newElementPos = elements.size() - 1;
        fireIntervalAdded(this, newElementPos, newElementPos);
        displaySpecification.addUndoable(new AddElementSpecificationUndoable(elementSpecification));
        return elementSpecification;
    }

    public void removeElement(int index)
    {
        ElementSpecification removedSpec = (ElementSpecification)elements.remove(index);
        displaySpecification.addUndoable(new RemoveElementSpecificationUndoable(removedSpec, index));
        fireIntervalRemoved(this, index, index);
    }

    protected void elementChanged(ElementSpecification es)
    {
        int index = elements.indexOf(es);
        if (index == -1)
            return;
        fireContentsChanged(this, index, index);
    }

    public DisplaySpecification getDisplaySpecification()
    {
        return displaySpecification;
    }

    public Iterator iterator()
    {
        return elements.iterator();
    }

    public class RemoveElementSpecificationUndoable implements DisplaySpecification.Undoable
    {
        private ElementSpecification removedSpec;
        private int position;

        public RemoveElementSpecificationUndoable(ElementSpecification removedSpec, int position)
        {
            this.removedSpec = removedSpec;
            this.position = position;
        }

        public void undo()
        {
            elements.add(position, removedSpec);
            fireIntervalAdded(ElementSpecifications.this, position, position);
        }

        public String getDescription()
        {
            return "removal of " + removedSpec.getElementPath();
        }
    }

    public class AddElementSpecificationUndoable implements DisplaySpecification.Undoable
    {
        private ElementSpecification addedSpec;

        public AddElementSpecificationUndoable(ElementSpecification addedSpec)
        {
            this.addedSpec = addedSpec;
        }

        public void undo()
        {
            int index = elements.indexOf(addedSpec);
            elements.remove(addedSpec);
            fireIntervalRemoved(ElementSpecifications.this, index, index);
        }

        public String getDescription()
        {
            return "addition of " + addedSpec.getElementPath();
        }
    }
}
