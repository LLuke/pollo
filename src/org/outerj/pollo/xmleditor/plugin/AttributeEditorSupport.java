package org.outerj.pollo.xmleditor.plugin;

import org.w3c.dom.Element;
import org.outerj.pollo.util.ComboBoxValuable;
import org.outerj.pollo.util.TextFieldValuable;
import org.outerj.pollo.util.CustomTableCellEditor;
import org.outerj.pollo.xmleditor.schema.ISchema;

import javax.swing.table.TableCellEditor;
import javax.swing.*;
import java.util.HashMap;
import java.awt.*;


/**
 * This class provides some basic support for writing new AttributeEditorPlugin's.
 * It will display either a textfield or combobox (if the schema contains predefined
 * values for the attribute). It is possible to add additional components, usually
 * JButtons, to the right of the textfield or combobox. The code behind these buttons
 * can modify the contents of the textfield or combobox through the provided 'Valuable'
 * (see getValuable()).
 *
 * <p>
 * Usage: see the example plugins for Cocoon or Ant.
 *
 * @author Bruno Dumon
 */
public class AttributeEditorSupport
{
	protected TextFieldValuable textFieldValuable = null;
	protected ComboBoxValuable comboBoxValuable = null;

	protected TableCellEditor textFieldEditor = null;
	protected TableCellEditor comboBoxEditor = null;

	protected Box extraTextFieldComponents = null;
	protected Box extraComboBoxComponents = null;

	protected short mode;
	private static final short TEXTFIELD_MODE = 1;
	private static final short COMBOBOX_MODE = 2;

	protected ISchema schema;

	public AttributeEditorSupport(ISchema schema)
	{
		this.schema = schema;

		// create the textfield editor
		textFieldValuable = new TextFieldValuable();
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(textFieldValuable);
		extraTextFieldComponents = new Box(BoxLayout.X_AXIS);
		box.add(extraTextFieldComponents);
		textFieldEditor = new CustomTableCellEditor(box, textFieldValuable);

		// create the combobox editor
		comboBoxValuable = new ComboBoxValuable();
		box = new Box(BoxLayout.X_AXIS);
		box.add(comboBoxValuable);
		extraComboBoxComponents = new Box(BoxLayout.X_AXIS);
		box.add(extraComboBoxComponents);
		comboBoxEditor = new CustomTableCellEditor(box, comboBoxValuable);
	}

	/**
	 * Prepares the editorSupport for a new attribute. This will, based on the schema,
	 * decide wether to use a combobox or textfield. It will also remove any components
	 * previously added using addComponent().
	 */
	public void reset(Element element, String namespaceURI, String localName)
	{
		extraTextFieldComponents.removeAll();
		extraComboBoxComponents.removeAll();

		String [] values = schema.getPossibleAttributeValues(element, namespaceURI, localName);
		if (values != null)
		{
			comboBoxValuable.setModel(new DefaultComboBoxModel(values));
			mode = COMBOBOX_MODE;
		}
		else
		{
			mode = TEXTFIELD_MODE;
		}
	}

	/**
	 * Returns the 'Valuable', this is needed to change the text in either the
	 * textfield or the combobox. This method should be called after the reset method.
	 */
	public CustomTableCellEditor.Valuable getValuable()
	{
		switch (mode)
		{
			case COMBOBOX_MODE:
				return comboBoxValuable;
			case TEXTFIELD_MODE:
				return textFieldValuable;
		}
		throw new Error("[AttributeEditorSupport] mode has incorrect value.");
	}

	/**
	 * Adds a component, such as a JButton, to the right of the combobox or
	 * textfield.
	 */
	public void addComponent(Component component)
	{
		switch (mode)
		{
			case COMBOBOX_MODE:
				extraComboBoxComponents.add(component);
				break;
			case TEXTFIELD_MODE:
				extraTextFieldComponents.add(component);
				break;
			default:
				throw new Error("[AttributeEditorSupport] mode has incorrect value.");
		}
	}

	/**
	 * Returns the actual TableCellEditor.
	 */
	public TableCellEditor getEditor()
	{
		switch (mode)
		{
			case COMBOBOX_MODE:
				return comboBoxEditor;
			case TEXTFIELD_MODE:
				return textFieldEditor;
		}
		throw new Error("[AttributeEditorSupport] mode has incorrect value.");
	}
}
