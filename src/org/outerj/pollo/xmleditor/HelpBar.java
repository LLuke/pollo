package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.attreditor.AttributesTableModel;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
 * Shows the help text of the selected node.
 */
public class HelpBar extends JLabel implements SelectionListener, ListSelectionListener
{
    protected JTable attributesTable;
    protected AttributesTableModel attrTableModel;
    protected XmlEditor xmlEditor;
    private static final String NO_HELP = "No help available.";

    public HelpBar(XmlEditor xmlEditor, AttributesPanel attrPanel)
    {
        super(NO_HELP);

        xmlEditor.getSelectionInfo().addListener(this);
        attrPanel.getAttributesTable().getSelectionModel().addListSelectionListener(this);

        attributesTable = attrPanel.getAttributesTable();
        attrTableModel = attrPanel.getAttributesTableModel();
        this.xmlEditor = xmlEditor;
    }

    public void nodeUnselected(Node node)
    {
        setText("No node selected.");
    }

    public void nodeSelected(Node node)
    {
        setHelp(xmlEditor.getSelectionInfo().getSelectedNodeView().getHelp());
    }

    public void valueChanged(ListSelectionEvent event)
    {
        int row = attributesTable.getSelectedRow();
        if (row != -1)
        {
            setHelp(attrTableModel.getHelpText(row));
        }
    }

    private void setHelp(String help)
    {
        if (help != null)
            setText(help);
        else
            setText(NO_HELP);
    }
}
