package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;

public class CocoonActionPlugin implements IActionPlugin
{
	protected final String SITEMAP_NS = "http://apache.org/cocoon/sitemap/1.0";

	public void init(HashMap initParams)
		throws PolloException
	{
		// no init params for now
	}

	public void addActionsToPluginMenu(JMenu menu, Node selectedNode, XmlModel xmlModel, PolloFrame polloFrame)
	{
		if (selectedNode != null && selectedNode instanceof Element)
		{
			Element element = (Element)selectedNode;
			if (element.getLocalName().equals("generate") && SITEMAP_NS.equals(element.getNamespaceURI()))
			{
				menu.add(new EditSourceAction("Edit generator source", element, xmlModel, polloFrame));
			}
		}
	}

	public class EditSourceAction extends AbstractAction
	{
		PolloFrame polloFrame;
		Element element;
		XmlModel xmlModel;

		public EditSourceAction(String name, Element element, XmlModel xmlModel, PolloFrame polloFrame)
		{
			super(name);

			this.polloFrame = polloFrame;
			this.element = element;
			this.xmlModel = xmlModel;
		}

		public void actionPerformed(ActionEvent e)
		{
			String filename = element.getAttribute("src");
			if (filename == null)
			{
				JOptionPane.showMessageDialog(polloFrame, "The element has no 'src' attribute");
				return;
			}

			File relativePath = xmlModel.getFile();
			if (relativePath != null)
				relativePath = relativePath.getParentFile();
			File file = new File(relativePath, filename); // relativePath is allowed to be null
			if (!file.exists())
			{
				JOptionPane.showMessageDialog(polloFrame, "The file " + file.getAbsolutePath() + " does not exist.");
				return;
			}

			Pollo.getInstance().openFile(file, polloFrame);
		}
	}

}
