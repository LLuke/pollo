package org.outerj.pollo.xmleditor;

import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.outerj.pollo.DomConnected;
import org.outerj.pollo.gui.EmptyIcon;
import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.xmleditor.action.*;
import org.outerj.pollo.xmleditor.displayspec.ElementSpec;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.model.InvalidXmlException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.view.*;
import org.w3c.dom.*;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * This is the main XML-editing widget.
 *
 * @author Bruno Dumon
 * */
public class XmlEditor extends JComponent implements MouseListener, NodeClickedListener,
	DragGestureListener, DragSourceListener, DropTargetListener, Autoscroll, DomConnected
{
	protected View mainView;
	protected SelectionInfo selectionInfo = new SelectionInfo();

	// fields for managing drag-and-drop
	protected Rectangle dragOverEffectRedraw;
	protected Node dropNode;
	protected int dropAction;
	protected Node draggingNode;
	protected boolean removeDraggingNode; // is true when moving, false when copying

	// clipboard -- static field for now
	protected static DocumentFragment clipboard;

	protected int oldWidth;
	protected int height = 0;
	protected XmlModel xmlModel;
	protected IDisplaySpecification displaySpec;
	protected ISchema schema;
	protected boolean antialiasing = false;
	protected LinkedList nodeClickedListenerList = new LinkedList();
	protected DragSource dragSource;
	protected String xpathForRoot;
	protected Element rootNodeDisplayed;

	// actions
	protected CopyAction copyAction;
	protected CutAction cutAction;
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

	protected InsertCharacterDataAction insertPIBeforeAction;
	protected InsertCharacterDataAction insertPIAfterAction;
	protected InsertCharacterDataAction insertPIInsideAction;

	protected CommentOutAction commentOutAction;
	protected UncommentAction uncommentAction;

	protected CollapseExpandAction collapseAllAction;
	protected CollapseExpandAction expandAllAction;

	protected CollapseExpandAction collapseAction;
	protected CollapseExpandAction expandAction;

	protected RenderViewToFileAction renderViewToFileAction;

	protected static final int MARGIN_LEFT  = 0;
	protected static final int MARGIN_TOP   = 0;
	protected static final int MARGIN_RIGHT = 4;

	public static final int DROP_ACTION_INSERT_BEFORE = 1;
	public static final int DROP_ACTION_APPEND_CHILD  = 2;
	public static final int DROP_ACTION_NOT_ALLOWED   = 3;

	public static final int AUTOSCROLL_REGION = 20;

	protected static InputMap inputMap;

	static
	{
		inputMap = new InputMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),   "select-next-node");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),     "select-previous-node");
		inputMap.put(KeyStroke.getKeyStroke('j'), "select-next-node"); // vi
		inputMap.put(KeyStroke.getKeyStroke('k'), "select-previous-node"); // vi
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete-node");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.Event.CTRL_MASK), "copy-node");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, java.awt.Event.CTRL_MASK), "cut-node");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, java.awt.Event.CTRL_MASK), "paste-node");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.Event.CTRL_MASK), "undo");
		inputMap.put(KeyStroke.getKeyStroke('-'), "collapse");
		inputMap.put(KeyStroke.getKeyStroke('+'), "expand");
		inputMap.put(KeyStroke.getKeyStroke('-', java.awt.Event.CTRL_MASK), "collapse-all");
		inputMap.put(KeyStroke.getKeyStroke('+', java.awt.Event.CTRL_MASK), "expand-all");
		inputMap.put(KeyStroke.getKeyStroke('o'), "insert-node-after");  // vi
		inputMap.put(KeyStroke.getKeyStroke('O'), "insert-node-before"); // vi
		inputMap.put(KeyStroke.getKeyStroke('i'), "insert-node-inside"); // vi
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit-details"); // vi
		inputMap.put(KeyStroke.getKeyStroke('n'), "next-xpath-result"); // vi
		inputMap.put(KeyStroke.getKeyStroke('N'), "prev-xpath-result"); // vi
	}

	/**
	  Construct a new XmlEditor.

	  <p>You also need to call the setXmlModel method to specify what data to show.

	  @param xpathForRoot an xpath expression that selects the element to display as root element.
	  @param displaySpec an instance of an IDisplaySpecification
	  @param schema the schema to use, see the interface ISchema for more details.
	 */
	public XmlEditor(String xpathForRoot, IDisplaySpecification displaySpec, ISchema schema)
		throws Exception
	{
		super();
		this.displaySpec = displaySpec;
		this.schema = schema;
		this.xpathForRoot = xpathForRoot;
		addMouseListener(this);
		addNodeClickedListener(this);
		setOpaque(true);

		ResourceManager resMgr = ResourceManager.getManager(XmlEditor.class);

		// init drag-and-drop
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		DropTarget dropTarget = new DropTarget(this, this);
		setAutoscrolls(true); // this doesn't help anything i think

		// init actions
		copyAction                = new CopyAction(this);
		cutAction                 = new CutAction(this);
		removeAction              = new RemoveAction(this);
		
		pasteBeforeAction         = new PasteAction(this, PasteAction.PASTE_BEFORE);
		resMgr.configureAction("pasteBeforeAction", pasteBeforeAction);
		pasteAfterAction          = new PasteAction(this, PasteAction.PASTE_AFTER);
		resMgr.configureAction("pasteAfterAction", pasteAfterAction);
		pasteInsideAction         = new PasteAction(this, PasteAction.PASTE_ASCHILD);
		resMgr.configureAction("pasteInsideAction", pasteInsideAction);
		
		insertCommentBeforeAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_BEFORE,
				InsertCharacterDataAction.TYPE_COMMENT);
		resMgr.configureAction("insertCommentBeforeAction", insertCommentBeforeAction);
		insertCommentAfterAction  = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_AFTER,
				InsertCharacterDataAction.TYPE_COMMENT);
		resMgr.configureAction("insertCommentAfterAction", insertCommentAfterAction);
		insertCommentInsideAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_INSIDE,
				InsertCharacterDataAction.TYPE_COMMENT);
		resMgr.configureAction("insertCommentInsideAction", insertCommentInsideAction);

		insertTextBeforeAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_BEFORE,
				InsertCharacterDataAction.TYPE_TEXT);
		resMgr.configureAction("insertTextBeforeAction", insertTextBeforeAction);
		insertTextAfterAction  = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_AFTER,
				InsertCharacterDataAction.TYPE_TEXT);
		resMgr.configureAction("insertTextAfterAction", insertTextAfterAction);
		insertTextInsideAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_INSIDE,
				InsertCharacterDataAction.TYPE_TEXT);
		resMgr.configureAction("insertTextInsideAction", insertTextInsideAction);

		insertCDataBeforeAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_BEFORE,
				InsertCharacterDataAction.TYPE_CDATA);
		resMgr.configureAction("insertCDataBeforeAction", insertCDataBeforeAction);
		insertCDataAfterAction  = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_AFTER,
				InsertCharacterDataAction.TYPE_CDATA);
		resMgr.configureAction("insertCDataAfterAction", insertCDataAfterAction);
		insertCDataInsideAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_INSIDE,
				InsertCharacterDataAction.TYPE_CDATA);
		resMgr.configureAction("insertCDataInsideAction", insertCDataInsideAction);

		insertPIBeforeAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_BEFORE,
				InsertCharacterDataAction.TYPE_PI);
		resMgr.configureAction("insertPIBeforeAction", insertPIBeforeAction);
		insertPIAfterAction  = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_AFTER,
				InsertCharacterDataAction.TYPE_PI);
		resMgr.configureAction("insertPIAfterAction", insertPIAfterAction);
		insertPIInsideAction = new InsertCharacterDataAction(this, InsertCharacterDataAction.INSERT_INSIDE,
				InsertCharacterDataAction.TYPE_PI);
		resMgr.configureAction("insertPIInsideAction", insertPIInsideAction);

		commentOutAction          = new CommentOutAction(this);
		uncommentAction           = new UncommentAction(this);

		collapseAllAction         = new CollapseExpandAction(this, CollapseExpandAction.COLLAPSE_ALL);
		resMgr.configureAction("collapseAllAction", collapseAllAction);
		expandAllAction           = new CollapseExpandAction(this, CollapseExpandAction.EXPAND_ALL);
		resMgr.configureAction("expandAllAction", expandAllAction);
		collapseAction            = new CollapseExpandAction(this, CollapseExpandAction.COLLAPSE);
		resMgr.configureAction("collapseAction", collapseAction);
		expandAction              = new CollapseExpandAction(this, CollapseExpandAction.EXPAND);
		resMgr.configureAction("expandAction", expandAction);

		renderViewToFileAction = new RenderViewToFileAction(this);

		// init keymap and actionmap
		setInputMap(WHEN_FOCUSED, inputMap);
		ActionMap actionMap = getActionMap();
		actionMap.put("select-next-node",     new SelectNextNodeAction(this));
		actionMap.put("select-previous-node", new SelectPreviousNodeAction(this));
		actionMap.put("delete-node",          removeAction);
		actionMap.put("copy-node",            copyAction);
		actionMap.put("cut-node",             cutAction);
		actionMap.put("paste-node",           pasteAfterAction);
		actionMap.put("collapse",             collapseAction);
		actionMap.put("expand",               expandAction);
		actionMap.put("collapse-all",         collapseAllAction);
		actionMap.put("expand-all",           expandAllAction);
	}

	/**
	  Specifies wich data to show.
	 */
	public void setXmlModel(XmlModel xmlModel)
	{
		this.xmlModel = xmlModel;
		this.mainView = null;

		ActionMap actionMap = getActionMap();
		actionMap.put("undo", xmlModel.getUndo().getUndoAction());
	}

	public XmlModel getXmlModel()
	{
		return xmlModel;
	}

	public IDisplaySpecification getDisplaySpec()
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
		JViewport viewPort = (JViewport)getParent();
		Rectangle rect = viewPort.getViewRect();
		return new Dimension(300, Math.max(height, rect.height));
	}

	public Dimension getMinimumSize()
	{
		JViewport viewPort = (JViewport)getParent();
		Rectangle rect = viewPort.getViewRect();
		return new Dimension(300, Math.max(height, rect.height));
	}

	public Dimension getMaximumSize()
	{
		return new Dimension(300, Integer.MAX_VALUE);
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
		if (node.getNodeType() == Node.DOCUMENT_NODE)
		{
			DocumentBlockView view = new DocumentBlockView(parentView, (Document)node, this);
			createViewsRecursive(node, view);
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
		else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
		{
			return createPIView((ProcessingInstruction)node, parentView);
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

	public View createPIView(ProcessingInstruction pi, View parentView)
	{
		PIView view = new PIView(parentView, pi, this);
		return view;
	}

	public View createCDataView(CDATASection cdata, View parentView)
	{
		CDataView view = new CDataView(parentView, cdata, this);
		return view;
	}

	private void createViewsRecursive(Node parentNode, View parentView)
	{
		NodeList children = parentNode.getChildNodes();

		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = (Node)children.item(i);
			if ((node.getNodeType() == Node.ELEMENT_NODE) /*&&
					displaySpec.getElementSpec(node.getNamespaceURI(),
						node.getLocalName()).viewType == ElementSpec.BLOCK_VIEW*/)
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
			else if ((node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE))
			{
				View childView = createPIView((ProcessingInstruction)node, parentView);
				parentView.addChildView(childView);
			}
		}
	}


	public void mousePressed(MouseEvent e)
	{
		requestFocus();

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
		if (node instanceof Document)
			return;

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

		popupMenu.add(getCopyAction());
		popupMenu.add(getCutAction());
		popupMenu.add(getRemoveAction());

		JMenu pasteMenu = new JMenu("Paste");
		pasteMenu.setIcon(EmptyIcon.getInstance());
		pasteMenu.add(getPasteBeforeAction());
		pasteMenu.add(getPasteAfterAction());
		if (node instanceof Element)
			pasteMenu.add(getPasteInsideAction());
		popupMenu.add(pasteMenu);
		popupMenu.addSeparator();

		JMenu commentMenu = new JMenu("Insert Comment");
		commentMenu.setIcon(EmptyIcon.getInstance());
		commentMenu.add(getInsertCommentBeforeAction());
		commentMenu.add(getInsertCommentAfterAction());
		if (node instanceof Element)
			commentMenu.add(getInsertCommentInsideAction());
		popupMenu.add(commentMenu);

		JMenu textMenu = new JMenu("Insert Text");
		textMenu.setIcon(EmptyIcon.getInstance());
		textMenu.add(getInsertTextBeforeAction());
		textMenu.add(getInsertTextAfterAction());
		if (node instanceof Element)
			textMenu.add(getInsertTextInsideAction());
		popupMenu.add(textMenu);

		JMenu cdataMenu = new JMenu("Insert CDATA");
		cdataMenu.setIcon(EmptyIcon.getInstance());
		cdataMenu.add(getInsertCDataBeforeAction());
		cdataMenu.add(getInsertCDataAfterAction());
		if (node instanceof Element)
			cdataMenu.add(getInsertCDataInsideAction());
		popupMenu.add(cdataMenu);

		JMenu piMenu = new JMenu("Insert PI");
		piMenu.setIcon(EmptyIcon.getInstance());
		piMenu.add(getInsertPIBeforeAction());
		piMenu.add(getInsertPIAfterAction());
		if (node instanceof Element)
			piMenu.add(getInsertPIInsideAction());
		popupMenu.add(piMenu);

		popupMenu.addSeparator();
		popupMenu.add(getRenderViewToFileAction());

		if (node instanceof Element)
		{
			popupMenu.addSeparator();
			popupMenu.add(getCollapseAllAction());
			popupMenu.add(getExpandAllAction());
			popupMenu.addSeparator();
			popupMenu.add(getCommentOutAction());

		}

		if (node instanceof Comment)
		{
			popupMenu.addSeparator();
			popupMenu.add(getUncommentAction());
		}

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
				if(removeDraggingNode)
				{
					Node parent = draggingNode.getParentNode();
					parent.removeChild(draggingNode);
				}
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
						Node parent = dropNode.getParentNode();

						if (parent instanceof Document && ((Document)parent).getDocumentElement() != null
								&& !(newNode instanceof Comment || newNode instanceof ProcessingInstruction))
						{
							event.rejectDrop();
							JOptionPane.showMessageDialog(getTopLevelAncestor(), "An XML document can have only one root element.");
							return;
						}

						/* Blocks GUI with jdk 1.4 - windows
						if (newNode.getNodeType() == Node.ELEMENT_NODE && !schema.isChildAllowed((Element)parent, (Element)newNode))
						{
							// schema tells it is not allowed here, but let the user decide
							if (JOptionPane.showConfirmDialog(getTopLevelAncestor(), ((Element)newNode).getLocalName() + " is not allowed here. Insert it anyway?", "Let me ask you something...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
							{
								event.rejectDrop();
								return;
							}
						}*/

						if (draggingNode != null)
							xmlModel.getUndo().startUndoTransaction("Drag-and-drop");
						parent.insertBefore(newNode, dropNode);
						event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
						event.getDropTargetContext().dropComplete(true);
					}
					else if (dropAction == DROP_ACTION_APPEND_CHILD)
					{
						if (dropNode instanceof Document && ((Document)dropNode).getDocumentElement() != null
								&& !(newNode instanceof Comment || newNode instanceof ProcessingInstruction))
						{
							event.rejectDrop();
							JOptionPane.showMessageDialog(getTopLevelAncestor(), "An XML document can have only one root element.");
							return;
						}

						/* Blocks GUI with jdk 1.4 - windows
						if (newNode.getNodeType() == Node.ELEMENT_NODE && !schema.isChildAllowed((Element)dropNode, (Element)newNode))
						{
							if (JOptionPane.showConfirmDialog(getTopLevelAncestor(), ((Element)newNode).getLocalName() + " is not allowed here. Insert it anyway?", "Let me ask you something...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
							{
								event.rejectDrop();
								return;
							}
						}*/

						if (draggingNode != null)
							xmlModel.getUndo().startUndoTransaction("Drag-and-drop");
						dropNode.appendChild(newNode);
						event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
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

	public void setDraggingNode(Node node, boolean move)
	{
		this.draggingNode = node;
		this.removeDraggingNode = move;
	}

	public void setDropData(int dropAction, Node node)
	{
		this.dropAction  = dropAction;
		this.dropNode = node;
	}

	public Node getRootElement()
	{
		if (rootNodeDisplayed == null)
		{
			if (xpathForRoot == null)
			{
				return xmlModel.getDocument();
			}
			else
			{
				try
				{
					Element documentElement = xmlModel.getDocument().getDocumentElement();
					XPath xpath = new DOMXPath(xpathForRoot);
					SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
					namespaceContext.addElementNamespaces(xpath.getNavigator(), documentElement);
					xpath.setNamespaceContext(namespaceContext);
					rootNodeDisplayed = (Element)xpath.selectSingleNode(documentElement);
				}
				catch (Exception e)
				{
					System.out.println("Error evaluating XPath for getting root element: " + e);
					return null;
				}
			}
		}
		return rootNodeDisplayed;
	}

	/**
	 * Holds information about the currently selected node.
	 */
	public class SelectionInfo implements EventListener, DomConnected
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
				unselect();
			}
			catch (Exception e)
			{
				System.out.println("Error in XmlEditor$SelectionInfo.handleEvent: " + e);
			}
		}

		public void unselect()
		{
			Iterator selectionListenersIt = selectionListeners.iterator();
			while (selectionListenersIt.hasNext())
			{
				SelectionListener listener = (SelectionListener)selectionListenersIt.next();
				listener.nodeUnselected(selectedNode);
			}

			selectedNode = null;
			selectedNodeView = null;
		}

		public void addListener(SelectionListener listener)
		{
			selectionListeners.add(listener);
		}

		public void disconnectFromDom()
		{
			if (selectedNode != null)
			{
				((EventTarget)selectedNode).removeEventListener("DOMNodeRemoved", this, false);
			}
		}

		public void reconnectToDom()
		{
			unselect(); // so that action etc. disable themselves properly
			disconnectFromDom();
			selectedNode = null;
			selectedNodeView = null;
			selectedViewRect = null;
		}
	}

	public CopyAction getCopyAction()
	{
		return copyAction;
	}

	public CutAction getCutAction()
	{
		return cutAction;
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

	public InsertCharacterDataAction getInsertPIBeforeAction()
	{
		return insertPIBeforeAction;
	}

	public InsertCharacterDataAction getInsertPIAfterAction()
	{
		return insertPIAfterAction;
	}

	public InsertCharacterDataAction getInsertPIInsideAction()
	{
		return insertPIInsideAction;
	}

	public CommentOutAction getCommentOutAction()
	{
		return commentOutAction;
	}

	public UncommentAction getUncommentAction()
	{
		return uncommentAction;
	}

	public CollapseExpandAction getCollapseAction()
	{
		return collapseAction;
	}

	public CollapseExpandAction getCollapseAllAction()
	{
		return collapseAllAction;
	}

	public CollapseExpandAction getExpandAction()
	{
		return expandAction;
	}

	public CollapseExpandAction getExpandAllAction()
	{
		return expandAllAction;
	}

	public RenderViewToFileAction getRenderViewToFileAction()
	{
		return renderViewToFileAction;
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
			case Node.DOCUMENT_NODE:
				return true;
			case Node.PROCESSING_INSTRUCTION_NODE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Makes sure that the area between the two given parameters is visible,
	 * if not, scrolls the viewport so that the rectangle is aligned to the bottom
	 * of the viewport. This is used for keyboard navigation.
	 */
	public void scrollAlignBottom(int startV, int height)
	{
		JViewport viewPort = (JViewport)getParent();
		Rectangle rect = viewPort.getViewRect();

		if ((startV > rect.y) && ((startV + height) < (rect.y + rect.height)))
		{
			// it is already completely visible
			return;
		}

		if (height > rect.height)
		{
			rect.translate(0, startV - rect.y);
		}
		else
		{
			// startV - rect.height + height is the position we want
			rect.translate(0, startV - rect.height + height - rect.y);
		}

		scrollRectToVisible(rect);
	}

	/**
	 * Makes sure that the area between the two given parameters is visible,
	 * if not, scrolls the viewport so that the rectangle is aligned to the top
	 * of the viewport. This is used for keyboard navigation.
	 */
	public void scrollAlignTop(int startV, int height)
	{
		JViewport viewPort = (JViewport)getParent();
		Rectangle rect = viewPort.getViewRect();

		if ((startV > rect.y) && ((startV + height) < (rect.y + rect.height)))
		{
			// it is already completely visible
			return;
		}

		rect.translate(0, startV - rect.y);

		scrollRectToVisible(rect);
	}


	/**
	 * Returns the root of the view object tree.
	 */
	public View getRootView()
	{
		return mainView;
	}

	public void disconnectFromDom()
	{
		if (mainView != null)
			mainView.removeEventListeners();

		selectionInfo.disconnectFromDom();
	}

	public void reconnectToDom()
	{
		if (mainView != null)
		{
			mainView.removeEventListeners();
			mainView = null;
		}
		rootNodeDisplayed = null;
		selectionInfo.reconnectToDom();
	}

	public ISchema getSchema()
	{
		return schema;
	}
}
