package org.outerj.pollo.xmleditor.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.apache.xpath.XPathAPI;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;


/**
  DOM-based model for an xml file. This class adds:
  <ul>
    <li>loading/saving of the XML file</li>
	<li>functions for: prefix to namespace, namespace to prefix, finding default namespace.</li>
	<li>function for getting an element based on an xpath expression</li>
  </ul>
 */
public class XmlModel
{
	protected Document document;
	protected Element pipelines;
	protected File file;
	protected Undo undo;
	protected ArrayList registeredViewsList = new ArrayList();
	protected ArrayList xmlModelListeners = new ArrayList();
	protected boolean modified;
	protected Action saveAction = new SaveAction();
	protected Action saveAsAction = new SaveAsAction();
	protected Action closeAction = new CloseAction();

	public XmlModel(File file)
		throws InvalidXmlException
	{
		this.file = file;
		try
		{
			/*
			This would be the JAXP way -- but this isn't used because we want to remove textnodes
			consisting only of whitespace, therefore a custom subclass of the xerces DOMParser is used (see below)
			
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", new Boolean(false));
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse(new InputSource(file.getAbsolutePath()));
			*/
			PolloDOMParser parser = new PolloDOMParser();
			parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion",false);
			parser.setFeature("http://xml.org/sax/features/namespaces", true);
			parser.parse(new InputSource(file.getAbsolutePath()));
			document = parser.getDocument();
		}
		catch (Exception e)
		{
			throw new InvalidXmlException("Error occured during parsing xml: " + e.getMessage());
		}
		undo = new Undo(this);

		modified = false;
		saveAction.setEnabled(false);
	}

	public Document getDocument()
	{
		return document;
	}

	public void store(String filename)
		throws Exception
		//throws TransformerConfigurationException, TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		/*
		Transformer serializer = transformerFactory.newTransformer();
		serializer.setOutputProperty("method", "xml");
		serializer.setOutputProperty("indent", "yes");
		String encoding = document.getEncoding();
		if (encoding != null)
			serializer.setOutputProperty("encoding", encoding);
		serializer.transform(new DOMSource(getDocument()), new StreamResult(filename));
		*/

		String encoding = document.getEncoding();
		OutputFormat outputFormat = new OutputFormat(document, encoding != null ? encoding : "ISO-8859-1", true);
		outputFormat.setIndent(2);

		FileOutputStream output = null;
		try
		{
			output = new FileOutputStream(filename);
			XMLSerializer serializer = new XMLSerializer(output, outputFormat);
			serializer.serialize(document);
		}
		finally
		{
			try { output.close(); } catch (Exception e) {}
		}
		
		modified = false;
		saveAction.setEnabled(false);
	}

	public void store()
		throws Exception
		//throws TransformerConfigurationException, TransformerException
	{
		store(file.getAbsolutePath());
	}

	public Element getNextElementSibling(Element element)
	{
		// search the next element sibling (null is also allowed)
		Element nextElement = null;
		Node nextNode = element;
		while ((nextNode = nextNode.getNextSibling()) != null)
		{
			if (nextNode.getNodeType() == Node.ELEMENT_NODE)
			{
				nextElement = (Element)nextNode;
				break;
			}
		}
		return nextElement;
	}


	/**
	  Finds the namespace with which the prefix is associated, or null
	  if not found.

	  @param element Element from which to start searching
	 */
	public String findNamespaceForPrefix(Element element, String prefix)
	{
		if (prefix == null)
			return null;

		if (prefix.equals("xml"))
			return "http://www.w3.org/XML/1998/namespace";

		if (prefix.equals("xmlns"))
			return null; // xmlns is itself not bound to a namespace

		Element currentEl = element;
		String searchForAttr = "xmlns:" + prefix;

		do
		{
			String attrValue = currentEl.getAttribute(searchForAttr);
			if (attrValue != null && attrValue.length() > 0)
			{
				return attrValue;
			}

			if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
				currentEl = (Element)currentEl.getParentNode();
			else
				currentEl = null;
		}
		while (currentEl != null);

		return null;
	}


	/**
	  Finds a prefix declaration for the given namespace, or null if
	  not found.

	  @param element Element from which to start searching

	  @return null if no prefix is found, an empty string if it is the
	  default namespace, and otherwise the found prefix
	 */
	public String findPrefixForNamespace(Element element, String ns)
	{
		if (ns == null)
			return null;

		if (ns.equals("http://www.w3.org/XML/1998/namespace"))
			return "xml";

		Element currentEl = element;

		do
		{
			NamedNodeMap attrs = currentEl.getAttributes();

			for (int i = 0; i < attrs.getLength(); i++)
			{
				Attr attr = (Attr)attrs.item(i);
				if (attr.getValue().equals(ns))
				{
				   	if (attr.getPrefix() != null && attr.getPrefix().equals("xmlns"))
					{
						return attr.getLocalName();
					}
					else if (attr.getLocalName().equals("xmlns"))
					{
						return "";
					}
				}
			}
			if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
				currentEl = (Element)currentEl.getParentNode();
			else
				currentEl = null;
		}
		while (currentEl != null);

		return null;
	}

	/**
	  Returns a list of all the namespace prefixes that are known in the given context.

	  @param element Element from which to start searching
	 */
	public HashMap findNamespaceDeclarations(Element element)
	{
		HashMap namespaces = new HashMap();
		Element currentEl = element;

		do
		{
			NamedNodeMap attrs = currentEl.getAttributes();

			for (int i = 0; i < attrs.getLength(); i++)
			{
				Attr attr = (Attr)attrs.item(i);
				if (attr.getPrefix() != null && attr.getPrefix().equals("xmlns") )
				{
					// only the first declartion found counts.
					if (!namespaces.containsKey(attr.getLocalName()))
						namespaces.put(attr.getLocalName(), attr.getValue());
				}
			}
			if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
				currentEl = (Element)currentEl.getParentNode();
			else
				currentEl = null;
		}
		while (currentEl != null);

		return namespaces;
	}


	/**
	  Finds a default namespace declaration.
	 */
	public String findDefaultNamespace(Element element)
	{
		// Note: the prefix xmlns is not bound to any namespace URI
		Element currentEl = element;
		do
		{
			String xmlns = currentEl.getAttribute("xmlns");
			if (xmlns != null)
				return xmlns;

			if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
				currentEl = (Element)currentEl.getParentNode();
			else
				currentEl = null;
		}
		while (currentEl != null);

		return null;
	}

	public Element getNode(String xpath)
	{
		try
		{
			Element el =  (Element)XPathAPI.selectSingleNode(document.getDocumentElement(), xpath);
			if (el == null)
				System.out.println("xpath returned null: " + xpath);
			return el;
		}
		catch (Exception e)
		{
			System.out.println("error executing xpath: " + xpath);
			return null;
		}
	}

	public Undo getUndo()
	{
		return undo;
	}

	public File getFile()
	{
		return file;
	}

	public void markModified()
	{
		modified = true;
		if (!saveAction.isEnabled())
		{
			saveAction.setEnabled(true);
		}
	}

	public void registerView(View view)
	{
		registeredViewsList.add(view);
	}

	public void addListener(XmlModelListener listener)
	{
		xmlModelListeners.add(listener);
	}

	/**
	  @return false if the user cancelled the operation
	 */
	public boolean closeView(View view)
		throws Exception
	{
		if (!registeredViewsList.contains(view))
			throw new RuntimeException("Tried to call XmlModel.closeView for a view that was not registered.");

		if (registeredViewsList.size() == 1)
		{
			// this was the last view on the model
			if (!askToSave()) return false;

			// last view was closed, notified XmlModelListeners of this fact
			notifyLastViewClosed();
		}
		registeredViewsList.remove(view);
		return true;
	}

	public void notifyLastViewClosed()
	{
		Iterator xmlModelListenersIt = xmlModelListeners.iterator();
		while (xmlModelListenersIt.hasNext())
		{
			XmlModelListener listener = (XmlModelListener)xmlModelListenersIt.next();
			listener.lastViewClosed(this);
		}
	}

	public void save()
		throws Exception
	{
		if (file == null)
		{
			saveAs();
		}
		if (file != null)
			store();
	}

	public void saveAs()
		throws Exception
	{
		// ask for a filename
		JFileChooser fileChooser = new JFileChooser();
		switch (fileChooser.showSaveDialog(null))
		{
			case JFileChooser.APPROVE_OPTION:
				file = fileChooser.getSelectedFile();
				break;
			case JFileChooser.CANCEL_OPTION:
				break;
			case JFileChooser.ERROR_OPTION:
				break;
		}
		if (file != null)
			save();
	}

	public boolean closeAllViews()
		throws Exception
	{
		if (!askToSave()) return false;

		Iterator registeredViewsIt = registeredViewsList.iterator();

		while (registeredViewsIt.hasNext())
		{
			View view = (View)registeredViewsIt.next();
			view.stop();
		}

		registeredViewsList.clear();
		notifyLastViewClosed();

		return true;
	}


	/**
	 * @return false if the user pressed cancel
	 */
	public boolean askToSave()
		throws Exception
	{
		if (modified)
		{
			switch (JOptionPane.showConfirmDialog(null, "The file " + file.getAbsolutePath() + " was modified. Save it?",
						"Pollo message", JOptionPane.YES_NO_CANCEL_OPTION))
			{
				case JOptionPane.YES_OPTION:
					save();
					break;
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION:
					return false;
			}
		}
		return true;
	}


	public Action getSaveAction()
	{
		return saveAction;
	}

	public Action getSaveAsAction()
	{
		return saveAsAction;
	}

	public Action getCloseAction()
	{
		return closeAction;
	}

	public class SaveAction extends AbstractAction
	{
		public SaveAction()
		{
			super("Save");
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				save();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public class SaveAsAction extends AbstractAction
	{
		public SaveAsAction()
		{
			super("Save As...");
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				saveAs();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public class CloseAction extends AbstractAction
	{
		public CloseAction()
		{
			super("Close");
		}

		public void actionPerformed(ActionEvent event)
		{
			try
			{
				closeAllViews();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
