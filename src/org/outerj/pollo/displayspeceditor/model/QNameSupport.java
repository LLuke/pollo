package org.outerj.pollo.displayspeceditor.model;

/**
 * Some helper methods for QNames.
 *
 * @author Bruno Dumon
 */
public class QNameSupport
{
    /**
     * Checks the validaty of an XML QName.
     */
    public static void checkQName(DisplaySpecification displaySpecification, String qName) throws InvalidQNameException
    {
        if (!org.apache.xerces.dom.DocumentImpl.isXMLName(qName))
            throw new InvalidQNameException("This is not a valid XML element or attribute name: " + qName);

        int prefixPos = qName.indexOf(":");
        if (prefixPos != -1)
        {
            String prefix = qName.substring(0, prefixPos);
            if (!displaySpecification.getNamespaces().prefixExists(prefix))
                throw new InvalidQNameException("The specified namespace prefix is not declared: " + prefix);
        }
    }

    public static void checkPrefix(String prefix) throws InvalidQNameException
    {
        if (!org.apache.xerces.dom.DocumentImpl.isXMLName(prefix))
            throw new InvalidQNameException("This is not a valid namespace prefix: " + prefix);

        if (prefix.indexOf(":") != -1)
            throw new InvalidQNameException("A namespace prefix can not contain a semicolon.");

        if (prefix.equals("xml"))
            throw new InvalidQNameException("The namespace prefix 'xml' can not be declared.");

    }
}
