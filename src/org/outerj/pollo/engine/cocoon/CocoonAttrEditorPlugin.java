package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.util.CustomTableCellEditor;
import org.outerj.pollo.xmleditor.IconManager;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.AttributeEditorSupport;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.PolloFrame;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Attribute Editor Plugin for Cocoon Sitemap files.
 *
 * <p>
 * Currently supports:
 * <ul>
 *  <li>browsing for files in src attributes
 *  <li>Inserting a reference to a wildcard matcher pattern
 * </ul>
 *
 * @author Al Byers, Bruno Dumon
 */
public class CocoonAttrEditorPlugin implements IAttributeEditorPlugin
{
	protected XmlModel xmlModel;
	protected PolloFrame polloFrame;

	protected AttributeEditorSupport editorSupport;
	protected CustomTableCellEditor.Valuable currentValuable;
	protected Element currentElement;

	protected JButton browseForFileButton;
	protected JFileChooser fileChooser;
	protected JCheckBox relativePathCheckBox;
	protected static HashSet elementsWithSrcChooser = new HashSet();

	protected JButton insertWildcardReferenceButton;
	protected SelectWildcardDialog selectWildcardDialog;

	static
	{
		elementsWithSrcChooser.add("generate");
		elementsWithSrcChooser.add("transform");
		elementsWithSrcChooser.add("serialize");
		elementsWithSrcChooser.add("read");
		elementsWithSrcChooser.add("part");
		elementsWithSrcChooser.add("mount");
	}

	public void init(HashMap initParams, XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
	{
		this.xmlModel = xmlModel;
		this.polloFrame = polloFrame;
		editorSupport = new AttributeEditorSupport(schema);

		// create the filechooser for inserting a file name
		fileChooser = new JFileChooser();
		// customise the file chooser
		fileChooser.setDialogTitle("Browse");
		fileChooser.setApproveButtonText("Select");
		JPanel fileChooserOptions = new JPanel();
		fileChooserOptions.setLayout(new BorderLayout());
		relativePathCheckBox = new JCheckBox("Insert path relative to location of the sitemap.");
		fileChooserOptions.add(relativePathCheckBox, BorderLayout.CENTER);
		// FIXME this line of code if *very* dependent on the underlying JFileChooser implementation
		((JComponent)fileChooser.getComponent(2)).add(fileChooserOptions);

		// create the button for inserting a file name
		browseForFileButton = new JButton(IconManager.getIcon("org/outerj/pollo/engine/cocoon/browse.png"));
		browseForFileButton.setMargin(new Insets(0, 0, 0, 0));
		browseForFileButton.setRequestFocusEnabled(false);
		browseForFileButton.setToolTipText("Browse for a file");
		browseForFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String srcValue = currentElement.getAttribute("src");
				File srcFile = new File(srcValue);
				fileChooser.setCurrentDirectory(srcFile);
				fileChooser.setSelectedFile(srcFile);

				int result = fileChooser.showOpenDialog(CocoonAttrEditorPlugin.this.polloFrame);
				if (result == JFileChooser.APPROVE_OPTION)
				{
					String selectedFileName = fileChooser.getSelectedFile().getAbsolutePath();
					String value = selectedFileName;
					if (relativePathCheckBox.isSelected())
					{
						File sitemapFile = CocoonAttrEditorPlugin.this.xmlModel.getFile();
						if (sitemapFile != null)
						{
							String path = sitemapFile.getParentFile().getAbsolutePath() + System.getProperty("file.separator");
							if (selectedFileName.startsWith(path))
							{
								value = selectedFileName.substring(path.length());
							}
						}
					}
					currentValuable.setValue(value);
				}

			}
		});

		// create the button & stuff for inserting a wilcard reference
		selectWildcardDialog = new SelectWildcardDialog(polloFrame, xmlModel);
		insertWildcardReferenceButton = new JButton(IconManager.getIcon("org/outerj/pollo/engine/cocoon/wildcard.png"));
		insertWildcardReferenceButton.setMargin(new Insets(0, 0, 0, 0));
		insertWildcardReferenceButton.setRequestFocusEnabled(false);
		insertWildcardReferenceButton.setToolTipText("Insert reference to a wildcard");
		insertWildcardReferenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					String result = selectWildcardDialog.showIt(currentElement);
					if (result != null)
					{
						currentValuable.insertString(result);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public TableCellEditor getAttributeEditor(Element element, String namespaceURI, String localName)
	{
		editorSupport.reset(element, namespaceURI, localName);

		currentElement = element;
		currentValuable = editorSupport.getValuable();

		if (elementsWithSrcChooser.contains(element.getLocalName()) && "src".equalsIgnoreCase(localName))
		{
			editorSupport.addComponent(browseForFileButton);
		}
		editorSupport.addComponent(insertWildcardReferenceButton);

		return editorSupport.getEditor();
	}
}
