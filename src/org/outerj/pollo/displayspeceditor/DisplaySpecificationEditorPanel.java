package org.outerj.pollo.displayspeceditor;

import org.outerj.pollo.displayspeceditor.model.*;
import org.outerj.pollo.gui.ColorSelector;
import org.outerj.pollo.gui.ColorSlider;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel containing the actual functionality for editing a display specification.
 * In MVC-speak this is the View and Controller.
 *
 * @author Bruno Dumon
 */
public class DisplaySpecificationEditorPanel extends JPanel implements ActionListener
{
    private DisplaySpecification displaySpecification;
    private ColorSelector colorSelector;

    private static final String ADD_EL_SPEC_ACTION = "add-element-specification";
    private static final String REMOVE_EL_SPEC_ACTION = "remove-element-specification";
    private static final String ADD_ATTR_ACTION = "add-attribute";
    private static final String REMOVE_ATTR_ACTION = "remove-attribute";
    private static final String MOVE_ATTR_UP_ACTION = "move-attribute-up";
    private static final String MOVE_ATTR_DOWN_ACTION = "move-attribute-down";
    private static final String ADD_NS_ACTION = "add-namespace-action";
    private static final String REMOVE_NS_ACTION = "remove-namespace-action";
    private static final String EDIT_NS_ACTION = "edit-namespace-action";

    private static final String ELEMENT_SELECTED_CARD = "element-selected-card";
    private static final String NO_ELEMENT_SELECTED_CARD = "no-element-selected-card";

    private JList elementsList;
    private JList attrList;
    private JList namespacesList;
    private JPanel elementDetailsPanel;
    private CardLayout elementDetailsCardLayout;

    public DisplaySpecificationEditorPanel(DisplaySpecification displaySpecification)
    {
        this.displaySpecification = displaySpecification;

        setBorder(new EmptyBorder(12, 12, 12, 12));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Elements (or nested element paths):"), gbc);
        add(Box.createHorizontalStrut(12), new GridBagConstraints());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(new JLabel("Namespace prefixes:"), gbc);


        // the list with element specifications
        elementsList = new JList(displaySpecification.getElements());
        elementsList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                elementSelectionChanged();
            }
        });
        elementsList.setCellRenderer(new ElementSpecificationCellRenderer());
        JScrollPane list = new JScrollPane(elementsList);
        gbc = new GridBagConstraints();
        gbc.weightx = 5;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(list, gbc);

        // the list with namespaces
        add(Box.createHorizontalStrut(12), new GridBagConstraints());
        namespacesList = new JList(displaySpecification.getNamespaces());
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(new JScrollPane(namespacesList), gbc);

        // spacing between element/namespace list and add-remove buttons
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(Box.createVerticalStrut(6), gbc);

        // the add/remove buttons for the element specification list
        JButton addButton = new JButton("Add ...");
        addButton.setActionCommand(ADD_EL_SPEC_ACTION);
        addButton.addActionListener(this);
        JButton removeButton = new JButton("Remove");
        removeButton.setActionCommand(REMOVE_EL_SPEC_ACTION);
        removeButton.addActionListener(this);
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalGlue());
        box.add(addButton);
        box.add(Box.createHorizontalStrut(6));
        box.add(removeButton);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(box, gbc);

        // the add/remove buttons for the namespaces list
        JButton addNsButton = new JButton("Add...");
        addNsButton.setActionCommand(ADD_NS_ACTION);
        addNsButton.addActionListener(this);
        JButton editNsButton = new JButton("Edit...");
        editNsButton.setActionCommand(EDIT_NS_ACTION);
        editNsButton.addActionListener(this);
        JButton removeNsButton = new JButton("Remove");
        removeNsButton.setActionCommand(REMOVE_NS_ACTION);
        removeNsButton.addActionListener(this);
        box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalGlue());
        box.add(addNsButton);
        box.add(Box.createHorizontalStrut(6));
        box.add(editNsButton);
        box.add(Box.createHorizontalStrut(6));
        box.add(removeNsButton);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(box, gbc);

        // box containing all element details
        Box elementDetailsBox = new Box(BoxLayout.X_AXIS);
        Border titleBorder = BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Selected element details");
        elementDetailsBox.setBorder(BorderFactory.createCompoundBorder(titleBorder, new EmptyBorder(12, 12, 12, 12)));

        // the color selector
        colorSelector = new ColorSelector(new Color(200, 200, 200));
        colorSelector.getColorSlider().addPropertyChangeListener(ColorSlider.COLOR, new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                colorChanged((Color)evt.getNewValue());
            }
        });
        elementDetailsBox.add(colorSelector);

        // spacing between color chooser and attributes thing
        elementDetailsBox.add(Box.createHorizontalStrut(12));

        // panel on which all attribute related things will be put
        JPanel attrPanel = new JPanel();
        attrPanel.setLayout(new GridBagLayout());
        elementDetailsBox.add(attrPanel);

        // title above attribute list
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        attrPanel.add(new JLabel("Display attributes in this order (optional):"), gbc);

        // the attribute list
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        attrList = new JList();
        JScrollPane attrScrollPane = new JScrollPane(attrList);
        attrPanel.add(attrScrollPane, gbc);

        // spacing between attribute list and up-down buttons
        gbc = new GridBagConstraints();
        attrPanel.add(Box.createHorizontalStrut(6), gbc);

        // up-down buttons for attributes
        box = new Box(BoxLayout.Y_AXIS);
        JButton upButton = new JButton("Up");
        upButton.setActionCommand(MOVE_ATTR_UP_ACTION);
        upButton.addActionListener(this);
        upButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JButton downButton = new JButton("Down");
        downButton.setActionCommand(MOVE_ATTR_DOWN_ACTION);
        downButton.addActionListener(this);
        downButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        box.add(upButton);
        box.add(Box.createVerticalStrut(6));
        box.add(downButton);
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        attrPanel.add(box, gbc);

        // spacing between attribute list and add/remove buttons
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        attrPanel.add(Box.createVerticalStrut(6), gbc);

        // add-remove buttons for attributes
        box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalGlue());
        JButton addAttrButton = new JButton("Add...");
        addAttrButton.setActionCommand(ADD_ATTR_ACTION);
        addAttrButton.addActionListener(this);
        box.add(addAttrButton);
        box.add(Box.createHorizontalStrut(6));
        JButton removeAttrButton = new JButton("Remove");
        removeAttrButton.setActionCommand(REMOVE_ATTR_ACTION);
        removeAttrButton.addActionListener(this);
        box.add(removeAttrButton);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        attrPanel.add(box, gbc);

        // create panel with cardlayout that either shows details about the selected element
        // or a message indicating that no element is selected
        elementDetailsPanel = new JPanel();
        elementDetailsCardLayout = new CardLayout();
        elementDetailsPanel.setLayout(elementDetailsCardLayout);
        JPanel noElementSelectedMessagePanel = new JPanel(new BorderLayout());
        noElementSelectedMessagePanel.add(new JLabel("Select an element to display its details here"), BorderLayout.CENTER);
        elementDetailsPanel.add(noElementSelectedMessagePanel, NO_ELEMENT_SELECTED_CARD);
        elementDetailsPanel.add(elementDetailsBox, ELEMENT_SELECTED_CARD);
        elementDetailsCardLayout.show(elementDetailsPanel, NO_ELEMENT_SELECTED_CARD);


        // add the element details box to the complete layout
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(elementDetailsPanel, gbc);

    }

    public void elementSelectionChanged()
    {
        int index = elementsList.getSelectedIndex();
        if (index == -1)
        {
            elementDetailsCardLayout.show(elementDetailsPanel, NO_ELEMENT_SELECTED_CARD);
        }
        else
        {
            elementDetailsCardLayout.show(elementDetailsPanel, ELEMENT_SELECTED_CARD);
            ElementSpecification elementSpecification = displaySpecification.getElements().get(index);
            attrList.setModel(elementSpecification.getAttributes());
            colorSelector.setColor(elementSpecification.getColor());
        }
    }

    public void colorChanged(Color newColor)
    {
        int index = elementsList.getSelectedIndex();
        if (index == -1)
            return;

        displaySpecification.getElements().get(index).setColor(newColor);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals(ADD_EL_SPEC_ACTION))
        {
            String qName = JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Enter the path for the element.\nThis can be simply a name, or a name qualified with a namespace prefix.\nIt can also be a nested path of elements, such as x:foo/x:bar", "New element specification", JOptionPane.QUESTION_MESSAGE);
            if (qName != null && !qName.trim().equals(""))
            {
                try
                {
                    displaySpecification.getElements().addElement(qName);
                    elementsList.setSelectedIndex(elementsList.getModel().getSize() - 1);
                }
                catch (InvalidQNameException exc)
                {
                    JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Error: " + exc.getMessage());
                }
            }
        }
        else if (e.getActionCommand().equals(REMOVE_EL_SPEC_ACTION))
        {
            int index = elementsList.getSelectedIndex();
            if (index != -1)
                displaySpecification.getElements().removeElement(index);
        }
        else if (e.getActionCommand().equals(ADD_ATTR_ACTION))
        {
            int selectedElementIndex = elementsList.getSelectedIndex();
            if (selectedElementIndex == -1)
                return;
            String qName = JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Enter the name for the attribute.\nThis can be simply a name, or a name qualified with a namespace prefix.", "Add attribute", JOptionPane.QUESTION_MESSAGE);
            if (qName != null && !qName.trim().equals(""))
            {
                try
                {
                    displaySpecification.getElements().get(selectedElementIndex).getAttributes().addAttribute(qName);
                }
                catch (InvalidQNameException exc)
                {
                    JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Error: " + exc.getMessage());
                }
            }
        }
        else if (e.getActionCommand().equals(REMOVE_ATTR_ACTION))
        {
            int selectedAttributeIndex = attrList.getSelectedIndex();
            if (selectedAttributeIndex == -1)
                return;
            int selectedElementIndex = elementsList.getSelectedIndex();
            if (selectedElementIndex == -1)
                return;
            displaySpecification.getElements().get(selectedElementIndex).getAttributes().removeAttribute(selectedAttributeIndex);
        }
        else if (e.getActionCommand().equals(MOVE_ATTR_UP_ACTION) || e.getActionCommand().equals(MOVE_ATTR_DOWN_ACTION))
        {
            int selectedElementIndex = elementsList.getSelectedIndex();
            if (selectedElementIndex == -1)
                return;
            int selectedAttrIndex = attrList.getSelectedIndex();
            if (selectedAttrIndex == -1)
                return;
            AttributeSpecifications attributes = displaySpecification.getElements().get(selectedElementIndex).getAttributes();
            int newIndex;
            if (e.getActionCommand().equals(MOVE_ATTR_UP_ACTION))
                newIndex = attributes.moveAttributeUp(selectedAttrIndex);
            else
                newIndex = attributes.moveAttributeDown(selectedAttrIndex);
            attrList.setSelectedIndex(newIndex);
            attrList.ensureIndexIsVisible(newIndex);
        }
        else if (e.getActionCommand().equals(EDIT_NS_ACTION))
        {
            int selectedIndex = namespacesList.getSelectedIndex();
            if (selectedIndex == -1)
                return;
            String currentValue = displaySpecification.getNamespaces().getNamespace(selectedIndex);
            String prefix = displaySpecification.getNamespaces().getPrefix(selectedIndex);
            String newValue = JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Enter new namespace for the prefix '" + prefix + "':", currentValue);
            if (newValue != null && !newValue.equals(""))
                displaySpecification.getNamespaces().updatePrefix(selectedIndex, newValue);
        }
        else if (e.getActionCommand().equals(ADD_NS_ACTION))
        {
            String prefix = JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Enter the namespace prefix:");
            if (prefix == null || prefix.equals(""))
                return;
            try
            {
                QNameSupport.checkPrefix(prefix);
            }
            catch (InvalidQNameException exc)
            {
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(), exc.getMessage());
                return;
            }
            String namespace = JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Enter the namespace for prefix '" + prefix + "':");
            if (namespace == null || namespace.equals(""))
                return;
            displaySpecification.getNamespaces().addPrefix(prefix, namespace);
        }
        else if (e.getActionCommand().equals(REMOVE_NS_ACTION))
        {
            int selectedIndex = namespacesList.getSelectedIndex();
            if (selectedIndex == -1)
                return;
            displaySpecification.getNamespaces().removePrefix(selectedIndex);
        }
    }

    public class ElementSpecificationCellRenderer extends JLabel implements ListCellRenderer
    {

        public Component getListCellRendererComponent(
                JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
        {
            final ElementSpecification elementSpecification = (ElementSpecification)value;
            setText(elementSpecification.getElementPath());
            setIcon(new Icon()
            {
                public void paintIcon(Component c, Graphics g, int x, int y)
                {
                    g.setColor(elementSpecification.getColor());
                    g.fillRect(x, y, 20, 8);
                    g.setColor(Color.black);
                    g.drawRect(x, y, 20, 8);
                }

                public int getIconWidth()
                {
                    return 20;
                }

                public int getIconHeight()
                {
                    return 8;
                }
            });
            setFont(list.getFont());

            setOpaque(true);
            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            return this;
        }
    }
}
