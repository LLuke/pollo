package org.outerj.xmleditor;

import org.outerj.xmleditor.model.*;
import org.outerj.xmleditor.view.*;
import org.outerj.xmleditor.DisplaySpecification.ElementSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.datatransfer.Transferable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import org.apache.xpath.XPathAPI;


/**
  This is the main XML-editing widget.
 */
public class XmlContentEditor extends JComponent implements MouseListener, ElementClickedListener,
	DragGestureListener, DragSourceListener, DropTargetListener, Autoscroll
{
	protected View mainView;
	protected View selectedView;
	protected Rectangle selectedViewRect;

	// fiels for managing drag-and-drop
	protected Rectangle dragOverEffectRedraw;
	protected Element dropElement;
	protected int dropAction;
	protected Element draggingElement;

	protected int oldWidth;
	protected int height = 0;
	protected XmlModel xmlModel;
	protected DisplaySpecification displaySpec;
	protected Schema schema;
	protected boolean antialiasing = false;
	protected Document clipboard;
	protected LinkedList elementClickedListenerList = new LinkedList();
	protected boolean showOnlyElements;
	protected DragSource dragSource;
	protected String xpathForRoot;
	protected Element rootElement;
	

	protected static final int MAINVIEW_START_HORIZONTAL = 0;
	protected static final int MAINVIEW_START_VERTICAL = 0;

	public static final int DROP_ACTION_INSERT_BEFORE = 1;
	public static final int DROP_ACTION_APPEND_CHILD  = 2;
	public static final int DROP_ACTION_NOT_ALLOWED   = 3;


	/**
	  Construct a new XmlContentEditor.

	  <p>You also need to call the setXmlModel method to specify what data to show.

	  @param xpathForRoot an xpath expression that selects the element to display as root element.
	  @param displaySpecFile location of the display specification file, see the class DisplaySpecification for more details.
	  @param schema the schema to use, see the class Schema for more details.
	  @param showOnlyElements should be true for now (in the future, this will toggle if
	  			only the element structure should be shown or also text/comment/... nodes.
	 */
	public XmlContentEditor(String xpathForRoot, String displaySpecFile, Schema schema, boolean showOnlyElements)
		throws Exception
	{
		super();
		this.showOnlyElements = showOnlyElements;
		this.displaySpec = DisplaySpecification.getInstance(displaySpecFile);
		this.schema = schema;
		this.xpathForRoot = xpathForRoot;
		addMouseListener(this);
		addElementClickedListener(this);
		setOpaque(true);

		// init drag-and-drop
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
		DropTarget dropTarget = new DropTarget(this, this);
		setAutoscrolls(true); // this doesn't help anything i think
	}

	/**
	  Specifies wich data to show.
	 */
	public void setXmlModel(XmlModel xmlModel)
	{
		this.xmlModel = xmlModel;
		this.mainView = null;
	}

	public XmlModel getXmlModel()
	{
		return xmlModel;
	}

	public DisplaySpecification getDisplaySpec()
	{
		return displaySpec;
	}

	protected void paintComponent(Graphics g)
	{
		if (antialiasing)
		{
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		if (xmlModel == null)
		{
			// there's nothing to show, thus nothing to paint.
			return;
		}

		if (mainView == null && getRootElement() == null)
		{
			g.drawString("Element not found: " + xpathForRoot, 50, 50);
			return;
		}

		if (mainView == null)
		{
			System.out.println("Rebuilding view tree...");
			try
			{
				rebuildView();
				height = mainView.layout(this.getWidth());
				oldWidth = this.getWidth();
				setSize(getWidth(), height);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("done");
		}
		if (mainView != null)
		{
			// paint background
			g.setColor(displaySpec.getBackgroundColor());
			((Graphics2D)g).fill(g.getClipBounds());

			// paint the views
			mainView.paint(g, 0, 0);
		}
	}

	public Dimension getPreferredSize()
	{
		return new Dimension(200, height);
	}

	public void setSize(Dimension d)
	{
		// if the component is resized recalculate the layout.
		this.height = (int)d.getHeight();
		super.setSize(d);
		if (mainView != null)
		{
			int amount = getWidth() - oldWidth;
			if (amount != 0)
			{
				height = mainView.widthChanged(amount);
				oldWidth = getWidth();
			}
		}
	}


	/**
	  This will build the View-object tree.
	 */
	protected void rebuildView()
		throws InvalidXmlException
	{
		mainView = createView(getRootElement(), null);
	}

	/**
	  Recursively creates view objects.
	 */
	public View createView(Element element, View parentView)
	{
		StructuralView view = new StructuralView(parentView, element, this);
		processChildren(element, view);
		return view;
	}

	private void processChildren(Element element, View parentView)
	{
		NodeList children = element.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if ((node.getNodeType() == Node.ELEMENT_NODE) && 
					displaySpec.getElementSpec(node.getNamespaceURI(),
						node.getLocalName()).viewType == ElementSpec.STRUCTURAL_VIEW)
			{
				View childView = createView((Element)node, parentView);
				parentView.addChildView(childView);
			}
		}
	}


	public void mouseClicked(MouseEvent e)
	{
		// the mouse event is recursively passed through the view object tree until
		// a view object recognizes that it is the one who is clicked.
		if (mainView != null)
			mainView.mouseClicked(e, MAINVIEW_START_HORIZONTAL, MAINVIEW_START_VERTICAL);
	}

	public void dragGestureRecognized(DragGestureEvent event)
	{
		// the drag gesture event is recursively passed through the view object tree until
		// a view object recognizes that it is the one who is being dragged.
		if (mainView != null)
			mainView.dragGestureRecognized(event, MAINVIEW_START_HORIZONTAL, MAINVIEW_START_VERTICAL);
	}

	
	public void mousePressed(MouseEvent e)  {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e)  {}
	public void mouseExited(MouseEvent e)   {}

	public void showContextMenu(Element element, int x, int y)
	{
		// don't show context menu on view the root element
		if (element == mainView.getElement())
			return;

		JPopupMenu popupMenu = new JPopupMenu();
		Element parent = (Element)element.getParentNode();

		JMenu insertBeforeMenu = new JMenu("Insert before");
		JMenu insertAfterMenu = new JMenu("Insert after");
		JMenu appendChildMenu = new JMenu("Append child");

		Iterator subElementsIt = schema.getAllowedSubElements((Element)element.getParentNode()).iterator();
		while (subElementsIt.hasNext())
		{
			Schema.SubElement subElement = (Schema.SubElement)subElementsIt.next();
			ElementSpec elementSpec = displaySpec.getElementSpec(subElement.namespaceURI, subElement.localName);
			insertBeforeMenu.add(new InsertElementAction(this, element,
						InsertElementAction.BEFORE, elementSpec));
			insertAfterMenu.add(new InsertElementAction(this, element,
						InsertElementAction.AFTER, elementSpec));
		}

		subElementsIt = schema.getAllowedSubElements(element).iterator();
		while (subElementsIt.hasNext())
		{
			Schema.SubElement subElement = (Schema.SubElement)subElementsIt.next();
			ElementSpec elementSpec = displaySpec.getElementSpec(subElement.namespaceURI, subElement.localName);
			appendChildMenu.add(new InsertElementAction(this, element,
						InsertElementAction.ASCHILD, elementSpec));
		}

		popupMenu.add(insertBeforeMenu);
		popupMenu.add(insertAfterMenu);
		popupMenu.add(appendChildMenu);
		popupMenu.addSeparator();

		popupMenu.add(new CopyAction(this, element));

		JMenu pasteMenu = new JMenu("Paste");
		pasteMenu.add(new PasteAction(this, element, PasteAction.PASTE_BEFORE));
		pasteMenu.add(new PasteAction(this, element, PasteAction.PASTE_AFTER));
		pasteMenu.add(new PasteAction(this, element, PasteAction.PASTE_ASCHILD));
		popupMenu.add(pasteMenu);
		popupMenu.addSeparator();

		popupMenu.add(new RemoveElementAction(this, element));

		popupMenu.show(this, x, y);
	}

	/**
	  Puts an element on an internal clipboard. TODO: use documentfragment instead
	  of a real document, allow any type of node(s) to be put on the clipboard.
	 */
	public void putOnClipboard(Element element)
	{
		try
		{
			clipboard = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (Exception e)
		{
			System.out.println("Error creating document in 'putOnClipboard': " + e.toString());
		}
		clipboard.appendChild(clipboard.importNode(element, true));
	}

	public Element getClipboard()
	{
		if (clipboard != null)
			return clipboard.getDocumentElement();

		return null;
	}

	public void addElementClickedListener(ElementClickedListener ecl)
	{
		elementClickedListenerList.add(ecl);
	}

	public void fireElementClickedEvent(ElementClickedEvent ece)
	{
		Iterator eclListIt = elementClickedListenerList.iterator();

		while (eclListIt.hasNext())
		{
			ElementClickedListener ecl = (ElementClickedListener)eclListIt.next();
			ecl.elementClicked(ece);
		}
	}


	public void elementClicked(ElementClickedEvent ece)
	{
		MouseEvent e = ece.getMouseEvent();
		if (((e.getModifiers() & e.BUTTON2_MASK) != 0) || ((e.getModifiers() & e.BUTTON3_MASK) != 0))
		{
			showContextMenu(ece.getElement(), e.getX(), e.getY());
		}
	}

	public View getSelectedView()
	{
		return selectedView;
	}

	public void setSelectedView(View view)
	{
		selectedView = view;
	}

	public Rectangle getSelectedViewRect()
	{
		return selectedViewRect;
	}

	public void setSelectedViewRect(Rectangle rect)
	{
		selectedViewRect = rect;
	}

	public boolean antialias()
	{
		return antialiasing;
	}




	// drag source listener methods

	public void dragDropEnd (DragSourceDropEvent event)
	{
		if ( event.getDropSuccess() )
		{
			Element parent = (Element)draggingElement.getParentNode();
			parent.removeChild(draggingElement);
		}
		draggingElement = null;
	}

	public void dragEnter (DragSourceDragEvent event) {}

	public void dragExit (DragSourceEvent event) {}

	public void dragOver (DragSourceDragEvent event) {}

	public void dropActionChanged ( DragSourceDragEvent event) {}


	// drop target listener methods
	public void dragEnter (DropTargetDragEvent event) {}

	public void dragExit (DropTargetEvent event)
	{
		removeDragOverEffect();
	}

	public void dragOver (DropTargetDragEvent event)
	{
		removeDragOverEffect();

		if (mainView != null)
			mainView.dragOver(event, MAINVIEW_START_HORIZONTAL, MAINVIEW_START_VERTICAL);
	}

	public void drop(DropTargetDropEvent event)
	{
		if (dropAction == DROP_ACTION_NOT_ALLOWED)
		{
			// if in the dragover event.rejectDrag() is called, we will still
			// get a drop-event, therefore dropAction is used to check if a
			// a drop is allowed
			event.rejectDrop();
			return;
		}

		try {
			Transferable transferable = event.getTransferable();

			if (transferable.isDataFlavorSupported (XmlTransferable.xmlFlavor))
			{
				Element element = (Element)transferable.getTransferData(XmlTransferable.xmlFlavor);
				Element newElement = (Element)xmlModel.getDocument().importNode(element, true);

				if (dropAction == DROP_ACTION_INSERT_BEFORE)
				{
					Element parent = (Element)dropElement.getParentNode();

					if (!schema.isChildAllowed(parent, newElement))
					{
						event.rejectDrop();
						JOptionPane.showMessageDialog(getContainingFrame(), newElement.getLocalName() + " is not allowed here.");
						return;
					}

					parent.insertBefore(newElement, dropElement);
					event.acceptDrop(DnDConstants.ACTION_MOVE);
					event.getDropTargetContext().dropComplete(true);
				}
				else if (dropAction == DROP_ACTION_APPEND_CHILD)
				{
					if (!schema.isChildAllowed(dropElement, newElement))
					{
						event.rejectDrop();
						JOptionPane.showMessageDialog(getContainingFrame(), newElement.getLocalName() + " is not allowed here.");
						return;
					}

					dropElement.appendChild(newElement);
					event.acceptDrop(DnDConstants.ACTION_MOVE);
					event.getDropTargetContext().dropComplete(true);
				}
			}
			else if (transferable.isDataFlavorSupported (CommandTransferable.commandFlavor))
			{
				HashMap commandInfo = (HashMap)transferable.getTransferData(CommandTransferable.commandFlavor);
				String namespaceURI = (String)commandInfo.get("uri");
				String localName = (String)commandInfo.get("localName");
				Element nsResolveElement;
				if (dropAction == DROP_ACTION_INSERT_BEFORE)
					nsResolveElement = (Element)dropElement.getParentNode();
				else
					nsResolveElement = dropElement;
				String prefix = getXmlModel().findPrefixForNamespace(nsResolveElement, namespaceURI);
				if (prefix != null) localName = prefix + ":" + localName;
				Element newElement = xmlModel.getDocument().createElementNS(namespaceURI, localName);

				if (dropAction == DROP_ACTION_INSERT_BEFORE)
				{
					Element parent = (Element)dropElement.getParentNode();

					if (!schema.isChildAllowed(parent, newElement))
					{
						event.rejectDrop();
						JOptionPane.showMessageDialog(getContainingFrame(), newElement.getLocalName() + " is not allowed here.");
						return;
					}

					event.acceptDrop(DnDConstants.ACTION_MOVE);
					parent.insertBefore(newElement, dropElement);
					event.getDropTargetContext().dropComplete(true);
				}
				else if (dropAction == DROP_ACTION_APPEND_CHILD)
				{
					if (!schema.isChildAllowed(dropElement, newElement))
					{
						event.rejectDrop();
						JOptionPane.showMessageDialog(getContainingFrame(), newElement.getLocalName() + " is not allowed here.");
						return;
					}

					event.acceptDrop(DnDConstants.ACTION_MOVE);
					dropElement.appendChild(newElement);
					event.getDropTargetContext().dropComplete(true);
				}
			}
			else
			{
				event.rejectDrop();
			}
		}
		catch (java.io.IOException exception) {
			exception.printStackTrace();
			System.err.println( "Exception" + exception.getMessage());
			event.rejectDrop();
		}
		catch (java.awt.datatransfer.UnsupportedFlavorException ufException ) {
			ufException.printStackTrace();
			System.err.println( "Exception" + ufException.getMessage());
			event.rejectDrop();
		}
	}

	public void dropActionChanged ( DropTargetDragEvent event ) {}

	// -- end drop target listener methods

	// autoscroll interface
	

	public void autoscroll(Point cursorLocn)
	{
		/*
		if (getParent() instanceof JViewport)
		{
			Rectangle rect = ((JViewport)getParent()).getViewRect();
			rect.translate(0, -10);
			scrollRectToVisible(rect);
		}
		*/
	}

	public Insets getAutoscrollInsets() 
	{
		/*
		if (getParent() instanceof JViewport)
		{
			Rectangle rect = ((JViewport)getParent()).getViewRect();
			return new Insets(rect.y + 20, 0, getHeight() - rect.y - rect.height + 20, 0);
		}
		*/
		return new Insets(0, 0, 0, 0);
	}

	// -- end autoscroll interface

	public DragSource getDragSource()
	{
		return dragSource;
	}

	protected void removeDragOverEffect()
	{
		if (dragOverEffectRedraw != null)
		{
			paintImmediately(dragOverEffectRedraw);
		}
	}


	public void setDragOverEffectRedraw(Rectangle rect)
	{
		this.dragOverEffectRedraw = rect;
	}

	public void setDraggingElement(Element element)
	{
		this.draggingElement = element;
	}

	public void setDropData(int dropAction, Element element)
	{
		this.dropAction  = dropAction;
		this.dropElement = element;
	}

	public Element getRootElement()
	{
		if (rootElement == null)
		{
			try
			{
				rootElement = (Element)XPathAPI.selectSingleNode(xmlModel.getDocument().getDocumentElement(),
						xpathForRoot);
			}
			catch (Exception e)
			{
				return null;
			}
		}
		return rootElement;
	}

	protected JFrame getContainingFrame()
	{
		Component parent = this.getParent();

		while (parent != null && !(parent instanceof JFrame))
		{
			parent = parent.getParent();
		}

		return (JFrame)parent;
	}

}
