package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.EditorPanelImpl;
import org.outerj.pollo.EditorPanel;
import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.XmlEditorPanel;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.jaxen.dom.XPath;
import org.jaxen.SimpleNamespaceContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;

public class CocoonActionPlugin implements IActionPlugin
{
	protected final String SITEMAP_NS = "http://apache.org/cocoon/sitemap/1.0";
	protected EditorPanel editorPanel;
	protected PolloFrame polloFrame;
	protected XmlModel xmlModel;
	protected XmlEditor xmlEditor;

	/**
	 * This map contains mappings betweeen sitemap element names and the
	 * name of the xml element that declares this type of component, e.g.
	 * generate and generator
	 */
    protected static final HashMap sitemapComponentMap = new HashMap();

	static
	{
		sitemapComponentMap.put("generate", "generator");
		sitemapComponentMap.put("transform", "transformer");
		sitemapComponentMap.put("read", "reader");
		sitemapComponentMap.put("serialize", "serializer");
		sitemapComponentMap.put("select", "selector");
		sitemapComponentMap.put("match", "matcher");
		sitemapComponentMap.put("act", "action");
	}

	public void init(HashMap initParams, EditorPanel editorPanel, PolloFrame polloFrame)
		throws PolloException
	{
		this.editorPanel = editorPanel;
		this.polloFrame = polloFrame;
	}

	private final void lateInitialisation()
	{
		// The initialisation of this ActionPlugin is done 'late' because during the
		// init method, the EditorPanel is not yet fully initialised, e.g. the XmlEditor
		// does not exist yet then

		if (this.xmlModel == null)
		{
			xmlModel = editorPanel.getXmlModel();
		}
		if (this.xmlEditor == null)
		{
			// Note: the method getXmlEditorPanel() is not in the EditorPanel interface,
			// because that interface should remain independent of the type of views in
			// the EditorPanel
			xmlEditor = ((EditorPanelImpl)editorPanel).getXmlEditorPanel().getXmlEditor();
		}
	}

	public void addActionsToPluginMenu(JMenu menu, Node selectedNode)
	{
		lateInitialisation();

		if (selectedNode != null && selectedNode instanceof Element)
		{
			Element element = (Element)selectedNode;
			if (element.getLocalName().equals("generate") && SITEMAP_NS.equals(element.getNamespaceURI()))
			{
				menu.add(new EditSourceAction("Edit generator source", element, xmlModel, polloFrame));
			}
/*
			if (SITEMAP_NS.equals(element.getNamespaceURI()) && sitemapComponentMap.containsKey(element.getLocalName()))
			{
				menu.add(new GotoComponentDeclarationAction(element));
			}*/
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

	/*
    public class GotoComponentDeclarationAction extends AbstractAction
    {
        Element element;

		public GotoComponentDeclarationAction(Element element)
		{
			super("Goto component declaration");
			this.element = element;
		}

        public void actionPerformed(ActionEvent e)
        {
			try
			{
				String componentName = element.getLocalName();
				String componentType = element.getAttribute("type");
				String componentDeclartionName = (String)sitemapComponentMap.get(componentName);


				if (componentType == null)
				{
					// find out what the default type is
					String typeXPathString = "/map:sitemap/map:components/map:" + componentDeclartionName + "s/@default";
					XPath xpath = new XPath(typeXPathString);
					SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
					namespaceContext.addElementNamespaces(xpath.getNavigator(), xmlModel.getDocument().getDocumentElement());
					xpath.setNamespaceContext(namespaceContext);
					Object o = xpath.selectSingleNode(xmlModel.getDocument().getDocumentElement());
					System.out.println("o is: " + o);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
        }
    }
     */
}
