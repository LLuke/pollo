package org.outerj.pollo.engine.ant;

import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.util.CustomTableCellEditor;
import org.outerj.pollo.util.CustomTableCellEditor.Valuable;
import org.outerj.pollo.util.TextFieldValuable;
import org.outerj.pollo.util.ComboBoxValuable;

import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AntAttrEditorPlugin implements IAttributeEditorPlugin
{
	XmlModel xmlModel;
	ISchema schema;

	TextFieldValuable textFieldValuable = null;
	ComboBoxValuable comboBoxValuable = null;

	TableCellEditor textFieldEditor = null;
	TableCellEditor comboBoxEditor = null;

	PropertiesDialog propDialog;

	public void init(HashMap initParams, XmlModel xmlModel, ISchema schema)
	{
		this.xmlModel = xmlModel;
		this.schema = schema;
		this.propDialog = new PropertiesDialog(null);

		// create the textfield editor
		textFieldValuable = new TextFieldValuable();
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(textFieldValuable);
		JButton propButton = new JButton("prop");
		propButton.setToolTipText("Select a property");
		propButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String prop = propDialog.askProperty();
						if (prop != null)
						{
							textFieldValuable.insertString("${" + prop + "}");
						}
					}
				});
		box.add(propButton);
		textFieldEditor = new CustomTableCellEditor(box, textFieldValuable);

		// create the combobox editor
		comboBoxValuable = new ComboBoxValuable();
		box = new Box(BoxLayout.X_AXIS);
		box.add(comboBoxValuable);
		propButton = new JButton("prop");
		propButton.setToolTipText("Select a property");
		propButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String prop = propDialog.askProperty();
						if (prop != null)
						{
							comboBoxValuable.insertString("${" + prop + "}");
						}
					}
				});
		box.add(propButton);
		comboBoxEditor = new CustomTableCellEditor(box, comboBoxValuable);
	}

	public TableCellEditor getAttributeEditor(Element element, String namespaceURI, String localName)
	{
		String [] values = schema.getPossibleAttributeValues(element, namespaceURI, localName);
		if (values != null)
		{
			comboBoxValuable.setModel(new DefaultComboBoxModel(values));
			return comboBoxEditor;
		}
		else
		{
			return textFieldEditor;
		}
	}


	/**
	 * A dialog containing the list with available ant properties.
	 */
	public class PropertiesDialog extends JDialog implements ActionListener
	{
		protected JTable userPropTable;
		protected JTable antPropTable;
		protected JTable javaPropTable;
		protected JTabbedPane tabs;

		protected boolean antPropInitialised = false;
		protected boolean javaPropInitialised = false;

		protected boolean ok = false;

		public PropertiesDialog(Frame parent)
		{
			super(parent, "Insert Ant property");
			setModal(true);
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // users should select ok or cancel
			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(12, 12, 12, 12));
			panel.setLayout(new BorderLayout(12, 12));
			this.setContentPane(panel);

			userPropTable = new JTable();
			antPropTable = new JTable();
			javaPropTable = new JTable();

			userPropTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			antPropTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			javaPropTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			tabs = new JTabbedPane();
			tabs.add(new JScrollPane(userPropTable), "User properties");
			tabs.add(new JScrollPane(antPropTable), "Ant properties");
			tabs.add(new JScrollPane(javaPropTable), "Java properties");

			panel.add(tabs, BorderLayout.CENTER);

			JButton okButton = new JButton("Okay");
			okButton.setActionCommand("ok");
			okButton.addActionListener(this);

			JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(this);

			Box buttons = new Box(BoxLayout.X_AXIS);
			buttons.add(Box.createGlue());
			buttons.add(okButton);
			buttons.add(Box.createHorizontalStrut(6));
			buttons.add(cancelButton);
			panel.add(buttons, BorderLayout.SOUTH);

			pack();
		}

		public void populateUserProperties()
		{
			NodeList userPropList = xmlModel.getDocument().getElementsByTagName("property");
			PropertiesTableModel tm = new PropertiesTableModel();

			for (int i = 0; i < userPropList.getLength(); i++)
			{
				Element element = (Element)userPropList.item(i);
				tm.addProperty(element.getAttribute("name"), element.getAttribute("description"));
			}

			userPropTable.setModel(tm);
		}

		public void populateAntProperties()
		{
			if (!antPropInitialised)
			{
				PropertiesTableModel tm = new PropertiesTableModel();
				tm.addProperty("basedir", "the absolute path of the project's basedir (as set with the basedir attribute of <project>)");
				tm.addProperty("ant.file", "the absolute path of the buildfile.");
				tm.addProperty("ant.version", "the version of Ant.");
				tm.addProperty("ant.project.name", "the name of the project that is currently executing; it is set in the name attribute of <project>.");
				tm.addProperty("ant.java.version", "the JVM version Ant detected; currently it can hold the values '1.1', '1.2', '1.3' and '1.4'.");
				antPropTable.setModel(tm);
				antPropInitialised = true;
			}
		}

		public void populateJavaProperties()
		{
			if (!javaPropInitialised)
			{
				PropertiesTableModel tm = new PropertiesTableModel();

				tm.addProperty("java.version", "Java Runtime Environment version");
			   	tm.addProperty("java.vendor", "Java Runtime Environment vendor");
				tm.addProperty("java.vendor.url", "Java vendor URL");
			  	tm.addProperty("java.home", "Java installation directory");
			   	tm.addProperty("java.vm.specification.version", "Java Virtual Machine specification version");
				tm.addProperty("java.vm.specification.vendor", "Java Virtual Machine specification vendor");
				tm.addProperty("java.vm.specification.name", "Java Virtual Machine specification name");
				tm.addProperty("java.vm.version", "Java Virtual Machine implementation version");
				tm.addProperty("java.vm.vendor", "Java Virtual Machine implementation vendor");
				tm.addProperty("java.vm.name", "Java Virtual Machine implementation name");
				tm.addProperty("java.specification.version", "Java Runtime Environment specification version");
				tm.addProperty("java.specification.vendor", "Java Runtime Environment specification vendor");
				tm.addProperty("java.specification.name", "Java Runtime Environment specification name");
				tm.addProperty("java.class.version", "Java class format version number");
				tm.addProperty("java.class.path", "Java class path");
				tm.addProperty("java.ext.dirs", "Path of extension directory or directories");
				tm.addProperty("os.name", "Operating system name");
				tm.addProperty("os.arch", "Operating system architecture");
				tm.addProperty("os.version", "Operating system version");
				tm.addProperty("file.separator", "File separator ('/' on UNIX)");
				tm.addProperty("path.separator", "Path separator (':' on UNIX)");
				tm.addProperty("line.separator", "Line separator ('\\n' on UNIX)");
				tm.addProperty("user.name", "User's account name  user.home  User's home directory");
				tm.addProperty("user.dir", "User's current working directory");

				javaPropTable.setModel(tm);
				javaPropInitialised = true;
			}
		}

		/**
		 * Shows the dialog and returns the selected property name, or null.
		 */
		public String askProperty()
		{
			populateUserProperties();
			populateAntProperties();
			populateJavaProperties();

			show();

			if (!ok)
				return null;

			JTable currentTable = null;
			switch (tabs.getSelectedIndex())
			{
				case 0:
					currentTable = userPropTable;
					break;
				case 1:
					currentTable = antPropTable;
					break;
				case 2:
					currentTable = javaPropTable;
					break;
			}

			int selected = currentTable.getSelectedRow();

			if (selected == -1)
				return null;
			else
				return (String)currentTable.getValueAt(selected, 0);
		}

		public void actionPerformed(ActionEvent event)
		{
			if (event.getActionCommand().equals("ok"))
			{
				ok = true;
				hide();
			}
			else if (event.getActionCommand().equals("cancel"))
			{
				ok = false;
				hide();
			}
		}

		public class PropertiesTableModel extends AbstractTableModel
		{
			public ArrayList names = new ArrayList();
			public ArrayList descriptions = new ArrayList();

			public void addProperty(String name, String description)
			{
				if (name == null) name = "";
				if (description == null) description = "";
				names.add(name);
				descriptions.add(description);
			}

			public int getRowCount()
			{
				return names.size();
			}

			public int getColumnCount()
			{
				return 2;
			}

			public Object getValueAt(int row, int column)
			{
				if (column == 0)
				{
					return names.get(row);
				}
				else
				{
					return descriptions.get(row);
				}
			}

			public String getColumnName(int column)
			{
				switch (column)
				{
					case 0:
						return "name";
					case 1:
						return "description";
					default:
						return "unkown?!";
				}
			}
		}
	}
}
