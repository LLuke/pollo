package org.outerj.pollo.displayspeceditor.model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * List of declared namespace prefixes, part of a {@link DisplaySpecification}.
 *
 * @author Bruno Dumon
 */
public class Namespaces extends AbstractListModel
{
    private ArrayList namespacesOrdered = new ArrayList();
    private HashMap namespaces = new HashMap();

    public void addPrefix(String prefix, String namespace)
    {
        if (namespaces.containsKey(prefix))
            removePrefix(prefix);

        namespaces.put(prefix, namespace);
        namespacesOrdered.add(prefix);
        int newNsIndex = namespacesOrdered.size();
        fireIntervalAdded(this, newNsIndex, newNsIndex);
    }

    public void updatePrefix(int index, String updatedNamespace)
    {
        namespaces.put(namespacesOrdered.get(index), updatedNamespace);
        fireContentsChanged(this, index, index);
    }

    public void removePrefix(String prefix)
    {
        namespaces.remove(prefix);
        int index = namespacesOrdered.indexOf(prefix);
        namespacesOrdered.remove(prefix);
        fireIntervalRemoved(this, index, index);
    }

    public void removePrefix(int index)
    {
        String prefix = (String)namespacesOrdered.get(index);
        namespacesOrdered.remove(index);
        namespaces.remove(prefix);
        fireIntervalRemoved(this, index, index);
    }

    public boolean prefixExists(String prefix)
    {
        if (prefix.equals("xml"))
            return true;
        return namespaces.containsKey(prefix);
    }

    public String getNamespace(String prefix)
    {
        return (String)namespaces.get(prefix);
    }

    public Iterator getPrefixIterator()
    {
        return namespacesOrdered.iterator();
    }

    public String getNamespace(int index)
    {
        return (String)namespaces.get(namespacesOrdered.get(index));
    }

    public String getPrefix(int index)
    {
        return (String)namespacesOrdered.get(index);
    }

    public int getSize()
    {
        return namespacesOrdered.size();
    }

    public Object getElementAt(int index)
    {
        String prefix = (String)namespacesOrdered.get(index);
        String namespace = (String)namespaces.get(prefix);
        return prefix + " (" + namespace + ")";
    }
}
