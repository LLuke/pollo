package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.InvalidXmlException;
import org.outerj.pollo.xmleditor.model.Schema;
import org.outerj.pollo.xmleditor.view.*;
import org.outerj.pollo.xmleditor.action.*;
import org.outerj.pollo.xmleditor.DisplaySpecification.ElementSpec;

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
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.Text;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
import org.w3c.dom.CharacterData;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import org.apache.xpath.XPathAPI;


/**
 * This is the main XML-editing widget.
 *
 * @author Bruno Dumon
 * */
public class XmlEditor extends JComponent implements MouseListener, NodeClickedListener,
	DragGestureListener, DragSourceListener, DropTargetListener, Autoscroll
{
	protected View mainView;
	protected SelectionInfo selectionInfo = new SelectionInfo();

	// fields for managing drag-and-drop
	protected Rectangle dragOverEffectRedraw;
	protected Node dropNode;
	protected int dropAction;
	protected Node draggingNode;

	// clipboard -- static field for now
	protected static DocumentFragment clipboard;

	protected int oldWidth;
	protected int height = 0;
	protected XmlModel xmlModel;
	protected DisplaySpecification displaySpec;
	protected Schema schema;
	protected boolean antialiasing = false;
	protected LinkedList nodeClickedListenerList = new LinkedList();
	protected DragSource dragSource;
	protected String xpathForRoot;
	protected Element rootElement;

	// actions
	protected CopyAction copyAction;
	protected RemoveAction removeAction;
	protected PasteAction pasteBeforeAction;
	protected PasteAction pasteAfterAction;
	protected PasteAction pasteInsideAction;

	protected InsertCharacterDataAction insertCommentBeforeAction;
	protected InsertCharacterDataAction insertCommentAfterAction;
	protected InsertCharacterDataAction insertCommentInsideAction;

	protected InsertCharacterDataAction insertTextBeforeAction;
	protected InsertCharacterDataAction insertTextAfterAction;
	protected InsertCharacterDataAction insertTextInsideAction;

	protected InsertCharacterDataAction insertCDataBeforeAction;
	protected InsertCharacterDataAction insertCDataAfterAction;
	protected InsertCharacterDataAction insertCDataInsideAction;

	protected CommentOutAction commentOutAction;
	protected UncommentAction uncommentAction;

	protected CollapseExpandAction collapseAllAction;
	protected CollapseExpandAction expandAllAction;

	protected static final int MARGIN_LEFT  = 0;
	protected static final int MARGIN_TOP   = 0;
	protected static final int MARGIN_RIGHT = 4;

	public static final int DROP_ACTION_INSERT_BEFORE = 1;
	public static final int DROP_ACTION_APPEND_CHILD  = 2;
	public static final int DROP_ACTION_NOT_ALLOWED   = 3;

	public static final int AUTOSCROLL_REGION = 20;


	/**
	  Construct a new XmlEditor.

	  <p>You also need to call the setXmlModel method to specify what data to show.

	  @param xpathForRoot an xpath expression that selects the element to display as root element.
	  @param displaySpecFile location of the display specification file, see the class DisplaySpecification for more details.
	  @param schema the schema to use, see the class Schema for more details.
	 */
	public XmlEditor(String xpathForRoot, String displaySpecFile, Schema schema)
		throws Exception
	{
		super();
		this.displaySpec = DisplaySpecification.getInstance(displaySpecFile);
		this.schema = schema;
		this.xpathForRoot = xpathForRoot;
		addMouseListener(this);
		addNodeClickedListener(this);
		setOpaque(true);

		// init drag-and-drop
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
		DropTarget dropTarget = new DropTarget(this, this);
		setAutoscrolls(true); // this doesn't help anything i think

		// init actions
		copyAction                = new CopyAction(this);
		removeAction              = new RemoveAction(this);
		pasteBeforeAction         = new PasteAction(this, PasteAction.PASTE_BEFORE);
		pasteAfterAction          = new PasteAction(this, PasteAction.PASTE_AFTER);
		pasteInsideAction         = new PasteAction(this, PasteAction.PASTE_ASCHILD);
		insertCommentBeforeAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_BEFORE,
				InsertCharacterDataAction.TYPE_COMMENT);
		insertCommentAfterAction  = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_AFTER,
				InsertCharacterDataAction.TYPE_COMMENT);
		insertCommentInsideAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_INSIDE,
				InsertCharacterDataAction.TYPE_COMMENT);

		insertTextBeforeAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_BEFORE,
				InsertCharacterDataAction.TYPE_TEXT);
		insertTextAfterAction  = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_AFTER,
				InsertCharacterDataAction.TYPE_TEXT);
		insertTextInsideAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_INSIDE,
				InsertCharacterDataAction.TYPE_TEXT);

		insertCDataBeforeAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_BEFORE,
				InsertCharacterDataAction.TYPE_CDATA);
		insertCDataAfterAction  = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_AFTER,
				InsertCharacterDataAction.TYPE_CDATA);
		insertCDataInsideAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_INSIDE,
				InsertCharacterDataAction.TYPE_CDATA);

		commentOutAction          = new CommentOutAction(this);
		uncommentAction           = new UncommentAction(this);

		collapseAllAction         = new CollapseExpandAction(this, CollapseExpandAction.COLLAPSE_ALL);
		expandAllAction           = new CollapseExpandAction(this, CollapseExpandAction.EXPAND_ALL);
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
			try
			{
				rebuildView();
				mainView.layout(this.getWidth() - MARGIN_RIGHT);
				oldWidth = this.getWidth();
				setSize(new Dimension(getWidth(), mainView.getHeight()));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
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

	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}

	public void setSize(Dimension d)
	{
		// if the component is resized recalculate the layout.
		int newHeight = (int)d.getHeight();
		this.height = newHeight;
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
	public View createView(Node node, View parentView)
	{
		if (node.getNodeType() == Node.ELEMENT_NODE)
		{
			ElementBlockView view = new ElementBlockView(parentView, (Element)node, this);
			createViewsRecursive((Element)node, view);
			return view;
		}
		else if (node.getNodeType() == Node.COMMENT_NODE)
		{
			return createCommentView((Comment)node, parentView);
		}
		else if (node.getNodeType() == Node.TEXT_NODE)
		{
			return createTextView((Text)node, parentView);
		}
		else if (node.getNodeType() == Node.CDATA_SECTION_NODE)
		{
			return createCDataView((CDATASection)node, parentView);
		}
		throw new RuntimeException("Unsupported type of node: " + node.getNodeType());
	}

	public View createCommentView(Comment comment, View parentView)
	{
		CommentView view = new CommentView(parentView, comment, this);
		return view;
	}

	public View createTextView(Text text, View parentView)
	{
		TextView view = new TextView(parentView, text, this);
		return view;
	}

	public View createCDataView(CDATASection cdata, View parentView)
	{
		CDataView view = new CDataView(parentView, cdata, this);
		return view;
	}

	private void createViewsRecursive(Element element, View parentView)
	{
		NodeList children = element.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if ((node.getNodeType() == Node.ELEMENT_NODE) && 
					displaySpec.getElementSpec(node.getNamespaceURI(),
						node.getLocalName()).viewType == ElementSpec.BLOCK_VIEW)
			{
				View childView = createView((Element)node, parentView);
				parentView.addChildView(childView);
			}
			else if ((node.getNodeType() == Node.COMMENT_NODE))
			{
				View childView = createCommentView((Comment)node, parentView);
				parentView.addChildView(childView);
			}
			else if ((node.getNodeType() == Node.TEXT_NODE))
			{
				View childView = createTextView((Text)node, parentView);
				parentView.addChildView(childView);
			}
			else if ((node.getNodeType() == Node.CDATA_SECTION_NODE))
			{
				View childView = createCDataView((CDATASection)node, parentView);
				parentView.addChildView(childView);
			}
		}
	}


	public void mousePressed(MouseEvent e)
	{
		// the mouse event is recursively passed through the view object tree until
		// a view object recognizes that it is the one who is clicked.
		if (mainView != null)
			mainView.mousePressed(e, MARGIN_LEFT, MARGIN_TOP);
	}

	public void mouseClicked(MouseEvent e)  {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e)  {}
	public void mouseExited(MouseEvent e)   {}

	public void dragGestureRecognized(DragGestureEvent event)
	{
		// the drag gesture event is recursively passed through the view object tree until
		// a view object recognizes that it is the one who is being dragged.
		if (mainView != null)
			mainView.dragGestureRecognized(event, MARGIN_LEFT, MARGIN_TOP);
	}
	
	public void showContextMenu(Node node, int x, int y)
	{
		if (node == mainView.getNode())
		{
			// on the root element, show menu with limited choices
			JPopupMenu popupMenu = new JPopupMenu();
			popupMenu.add(getCollapseAllAction());
			popupMenu.add(getExpandAllAction());
			popupMenu.show(this, x, y);
			return;
		}

		JPopupMenu popupMenu = new JPopupMenu();
		if (node instanceof Element)
		{
			Element element = (Element)node;

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
			popupMenu.add(getCommentOutAction());
			popupMenu.addSeparator();
			popupMenu.add(getCollapseAllAction());
			popupMenu.add(getExpandAllAction());

		}
		else if (node instanceof Comment || node instanceof Text || node instanceof CDATASection)
		{
			popupMenu.add(new EditCharacterDataAction(this, (CharacterData)node));
		}

		if (node instanceof Comment)
		{
			popupMenu.add(getUncommentAction());
		}

		popupMenu.addSeparator();

		popupMenu.add(getCopyAction());

		JMenu pasteMenu = new JMenu("Paste");
		pasteMenu.add(getPasteBeforeAction());
		pasteMenu.add(getPasteAfterAction());
		if (node instanceof Element)
			pasteMenu.add(getPasteInsideAction());
		popupMenu.add(pasteMenu);
		popupMenu.addSeparator();

		JMenu commentMenu = new JMenu("Insert Comment");
		commentMenu.add(getInsertCommentBeforeAction());
		commentMenu.add(getInsertCommentAfterAction());
		if (node instanceof Element)
			commentMenu.add(getInsertCommentInsideAction());
		popupMenu.add(commentMenu);

		JMenu textMenu = new JMenu("Insert Text");
		textMenu.add(getInsertTextBeforeAction());
		textMenu.add(getInsertTextAfterAction());
		if (node instanceof Element)
			textMenu.add(getInsertTextInsideAction());
		popupMenu.add(textMenu);

		JMenu cdataMenu = new JMenu("Insert CDATA");
		cdataMenu.add(getInsertCDataBeforeAction());
		cdataMenu.add(getInsertCDataAfterAction());
		if (node instanceof Element)
			cdataMenu.add(getInsertCDataInsideAction());
		popupMenu.add(cdataMenu);

		popupMenu.addSeparator();
		popupMenu.add(getRemoveAction());

		popupMenu.show(this, x, y);
	}

	/**
	  Puts an element on an internal clipboard. TODO: use documentfragment instead
	  of a real document, allow any type of node(s) to be put on the clipboard.
	 */
	public void putOnClipboard(Node node)
	{
		clipboard = xmlModel.getDocument().createDocumentFragment();
		clipboard.appendChild(node.cloneNode(true));
	}

	public DocumentFragment getClipboard()
	{
		return clipboard;
	}

	public void addNodeClickedListener(NodeClickedListener ecl)
	{
		nodeClickedListenerList.add(ecl);
	}

	public void fireNodeClickedEvent(NodeClickedEvent ece)
	{
		Iterator eclListIt = nodeClickedListenerList.iterator();

		while (eclListIt.hasNext())
		{
			NodeClickedListener ecl = (NodeClickedListener)eclListIt.next();
			ecl.nodeClicked(ece);
		}
	}


	public void nodeClicked(NodeClickedEvent nce)
	{
		MouseEvent e = nce.getMouseEvent();
		if (SwingUtilities.isRightMouseButton(e))
		{
			showContextMenu(nce.getNode(), e.getX(), e.getY());
		}
	}

	public Node getSelectedNode()
	{
		return selectionInfo.getSelectedNode();
	}

	public void setSelectedNode(Node node, View view)
	{
		selectionInfo.setSelectedNode(node, view);
	}

	public Rectangle getSelectedViewRect()
	{
		return selectionInfo.getSelectedViewRect();
	}

	public void setSelectedViewRect(Rectangle rect)
	{
		selectionInfo.setSelectedViewRect(rect);
	}

	public boolean antialias()
	{
		return antialiasing;
	}




	// drag source listener methods

	public void dragDropEnd (DragSourceDropEvent event)
	{
		try
		{
			if ( event.getDropSuccess() )
			{
				Element parent = (Element)draggingNode.getParentNode();
				parent.removeChild(draggingNode);
				xmlModel.getUndo().endUndoTransaction();
			}
		}
		finally
		{
			draggingNode = null;
		}
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
			mainView.dragOver(event, MARGIN_LEFT, MARGIN_TOP);
	}

	public void drop(DropTargetDropEvent event)
	{
		try
		{
			if (dropAction == DROP_ACTION_NOT_ALLOWED)
			{
				// if in the dragover event.rejectDrag() is called, we will still
				// get a drop-event, therefore dropAction is used to check if a
				// a drop is allowed
				event.rejectDrop();
				return;
			}

			Node dropNodeAncestor = dropNode;
			while (dropNodeAncestor != null)
			{
				if (dropNodeAncestor == draggingNode)
				{
					event.rejectDrop();
					JOptionPane.showMessageDialog(getTopLevelAncestor(), "Cannot drop an element as descendant of itself.");
					return;
				}
				dropNodeAncestor = dropNodeAncestor.getParentNode();
			}


			try {
				Transferable transferable = event.getTransferable();

				if (transferable.isDataFlavorSupported (XmlTransferable.xmlFlavor))
				{
					Node node = (Node)transferable.getTransferData(XmlTransferable.xmlFlavor);
					Node newNode = xmlModel.getDocument().importNode(node.getFirstChild(), true);

					if (dropAction == DROP_ACTION_INSERT_BEFORE)
					{
						Element parent = (Element)dropNode.getParentNode();

						if (newNode.getNodeType() == Node.ELEMENT_NODE && !schema.isChildAllowed(parent, (Element)newNode))
						{
							event.rejectDrop();
							JOptionPane.showMessageDialog(getContainingFrame(), ((Element)newNode).getLocalName() + " is not allowed here.");
							return;
						}

						if (draggingNode != null)
							xmlModel.getUndo().startUndoTransaction("Drag-and-drop");
						parent.insertBefore(newNode, dropNode);
						event.acceptDrop(DnDConstants.ACTION_MOVE);
						event.getDropTargetContext().dropComplete(true);
					}
					else if (dropAction == DROP_ACTION_APPEND_CHILD)
					{
						if (newNode.getNodeType() == Node.ELEMENT_NODE && !schema.isChildAllowed((Element)dropNode, (Element)newNode))
						{
							event.rejectDrop();
							JOptionPane.showMessageDialog(getContainingFrame(), newNode.getLocalName() + " is not allowed here.");
							return;
						}

						if (draggingNode != null)
							xmlModel.getUndo().startUndoTransaction("Drag-and-drop");
						((Element)dropNode).appendChild(newNode);
						event.acceptDrop(DnDConstants.ACTION_MOVE);
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
		finally
		{
			dropNode = null;
		}
	}

	public void dropActionChanged ( DropTargetDragEvent event ) {}

	// -- end drop target listener methods

	// autoscroll interface
	

	public void autoscroll(Point cursorLocation)
	{
		JViewport viewPort = (JViewport)getParent();
		Rectangle rect = viewPort.getViewRect();

		if (cursorLocation.y > rect.y + rect.height - AUTOSCROLL_REGION)
			rect.translate(0, 10);
		else
			rect.translate(0, -10);

		scrollRectToVisible(rect);
	}

	public Insets getAutoscrollInsets() 
	{
		Rectangle rect = ((JViewport)getParent()).getViewRect();
		return new Insets(rect.y + AUTOSCROLL_REGION, 0,
				getHeight() - rect.y - rect.height + AUTOSCROLL_REGION, 0);
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

	public void setDraggingNode(Node node)
	{
		this.draggingNode = node;
	}

	public void setDropData(int dropAction, Node node)
	{
		this.dropAction  = dropAction;
		this.dropNode = node;
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

	/**
	  Holds information about the currently selected node.
	 */
	public class SelectionInfo implements EventListener
	{
		Node selectedNode;
		View selectedNodeView;
		Rectangle selectedViewRect;
		LinkedList selectionListeners = new LinkedList();

		public void setSelectedNode(Node node, View view)
		{
			if (selectedNode != null)
			{
				((EventTarget)selectedNode).removeEventListener("DOMNodeRemoved", this, false);
			}

			this.selectedNode = node;
			this.selectedNodeView = view;
			// add an event listener to the selected node so that when
			// it gets deleted, the node is no longer marked as selected
			// node
			((EventTarget)node).addEventListener("DOMNodeRemoved", this, false);

			Iterator selectionListenersIt = selectionListeners.iterator();
			while (selectionListenersIt.hasNext())
			{
				SelectionListener listener = (SelectionListener)selectionListenersIt.next();
				listener.nodeSelected(selectedNode);
			}
		}

		public Node getSelectedNode()
		{
			return selectedNode;
		}

		public View getSelectedNodeView()
		{
			return selectedNodeView;
		}

		public void setSelectedViewRect(Rectangle rect)
		{
			this.selectedViewRect = rect;
		}

		public Rectangle getSelectedViewRect()
		{
			return selectedViewRect;
		}

		/**
		  handles the dom node removed event.
		 */
		public void handleEvent(Event event)
		{
			try
			{
				Iterator selectionListenersIt = selectionListeners.iterator();
				while (selectionListenersIt.hasNext())
				{
					SelectionListener listener = (SelectionListener)selectionListenersIt.next();
					listener.nodeUnselected(selectedNode);
				}
				selectedNode = null;
			}
			catch (Exception e)
			{
				System.out.println("Error in XmlEditor$SelectionInfo.handleEvent: " + e);
			}
		}

		public void addListener(SelectionListener listener)
		{
			selectionListeners.add(listener);
		}
	}

	public CopyAction getCopyAction()
	{
		return copyAction;
	}

	public RemoveAction getRemoveAction()
	{
		return removeAction;
	}

	public PasteAction getPasteBeforeAction()
	{
		return pasteBeforeAction;
	}

	public PasteAction getPasteAfterAction()
	{
		return pasteAfterAction;
	}

	public PasteAction getPasteInsideAction()
	{
		return pasteInsideAction;
	}

	public InsertCharacterDataAction getInsertCommentBeforeAction()
	{
		return insertCommentBeforeAction;
	}

	public InsertCharacterDataAction getInsertCommentAfterAction()
	{
		return insertCommentAfterAction;
	}

	public InsertCharacterDataAction getInsertCommentInsideAction()
	{
		return insertCommentInsideAction;
	}

	public InsertCharacterDataAction getInsertTextBeforeAction()
	{
		return insertTextBeforeAction;
	}

	public InsertCharacterDataAction getInsertTextAfterAction()
	{
		return insertTextAfterAction;
	}

	public InsertCharacterDataAction getInsertTextInsideAction()
	{
		return insertTextInsideAction;
	}

	public InsertCharacterDataAction getInsertCDataBeforeAction()
	{
		return insertCDataBeforeAction;
	}

	public InsertCharacterDataAction getInsertCDataAfterAction()
	{
		return insertCDataAfterAction;
	}

	public InsertCharacterDataAction getInsertCDataInsideAction()
	{
		return insertCDataInsideAction;
	}

	public CommentOutAction getCommentOutAction()
	{
		return commentOutAction;
	}

	public UncommentAction getUncommentAction()
	{
		return uncommentAction;
	}

	public CollapseExpandAction getCollapseAllAction()
	{
		return collapseAllAction;
	}

	public CollapseExpandAction getExpandAllAction()
	{
		return expandAllAction;
	}

	public SelectionInfo getSelectionInfo()
	{
		return selectionInfo;
	}

	public static boolean isNodeTypeSupported(int nodeType)
	{
		switch (nodeType)
		{
			case Node.ELEMENT_NODE:
				return true;
			case Node.COMMENT_NODE:
				return true;
			case Node.TEXT_NODE:
				return true;
			case Node.CDATA_SECTION_NODE:
				return true;
			default:
				return false;
		}
	}

}
