package org.outerj.xmleditor.model;

import org.outerj.xmleditor.model.XmlModel;

import java.util.Stack;
import java.util.EmptyStackException;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.events.Event;


/**
  The undo-class enables the undoing of modifications to the DOM-tree.
  This is achieved by registring this class as event listener on the DOM tree
  for MutationEvents.
 */
public class Undo implements EventListener
{
	Stack undos = new Stack();
	boolean undoing = false;
	UndoAction undoAction;

	public Undo(XmlModel xmlModel)
	{
		Element element = xmlModel.getDocument().getDocumentElement();
		((EventTarget)element).addEventListener("DOMAttrModified", this, true);
		((EventTarget)element).addEventListener("DOMNodeInserted", this, true);
		((EventTarget)element).addEventListener("DOMNodeRemoved", this, true);

		undoAction = new UndoAction(this);
		undoAction.setEnabled(false);
	}

	public void handleEvent(Event event)
	{
		if (undoing)
		{
			// if the current mutation event is the result of an undo operation,
			// ignore it.
			undoing = false;
			return;
		}

		try
		{
			if (event.getType().equalsIgnoreCase("DOMNodeInserted"))
			{
				NodeInsertedUndo undo = new NodeInsertedUndo((MutationEvent)event);
				undoAction.putValue(UndoAction.NAME, "Undo " + undo.getDescription());
				undoAction.setEnabled(true);
				undos.push(undo);
			}
			else if (event.getType().equalsIgnoreCase("DOMNodeRemoved"))
			{
				NodeRemovedUndo undo = new NodeRemovedUndo((MutationEvent)event);
				undoAction.putValue(UndoAction.NAME, "Undo " + undo.getDescription());
				undoAction.setEnabled(true);
				undos.push(undo);
			}
			else if (event.getType().equalsIgnoreCase("DOMAttrModified"))
			{
				AttrModifiedUndo undo = new AttrModifiedUndo((MutationEvent)event);
				undoAction.putValue(UndoAction.NAME, "Undo " + undo.getDescription());
				undoAction.setEnabled(true);
				undos.push(undo);
			}
			else
			{
				System.out.println("[undo] WARNING: unprocessed dom event: " + event.getType());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void undo()
	{
		Undoable undoable = null;
		try
		{
			undoable = (Undoable)undos.pop();
		}
		catch (EmptyStackException ese)
		{
			System.out.println("nothing to undo");
			return;
		}

		if (undos.empty())
		{
			undoAction.setEnabled(false);
			undoAction.putValue(UndoAction.NAME, "Undo");
		}
		else
			undoAction.putValue(UndoAction.NAME, "Undo " +
					((Undoable)undos.peek()).getDescription());


		undoing = true;
		undoable.undo();
	}

	public interface Undoable
	{
		public void undo();
		public String getDescription();
	}

	public class NodeInsertedUndo implements Undoable
	{
		public Element insertedEl;

		public NodeInsertedUndo(MutationEvent me)
		{
			insertedEl = (Element)me.getTarget();
		}

		public void undo()
		{
			Element parent = (Element)insertedEl.getParentNode();
			parent.removeChild(insertedEl);
		}

		public String getDescription()
		{
			String prefix = insertedEl.getPrefix();
			String localName = insertedEl.getLocalName();
			String qname = prefix != null? prefix + ":" + localName : localName;
			return "insert node '" + qname + "'";
		}
	}


	public class NodeRemovedUndo implements Undoable
	{
		public Element removedEl;
		public Element parentEl;
		public Node nextSibling;

		public NodeRemovedUndo(MutationEvent me)
		{
			removedEl = (Element)me.getTarget();
			parentEl = (Element)removedEl.getParentNode();
			nextSibling = removedEl.getNextSibling();
		}

		public void undo()
		{
			parentEl.insertBefore(removedEl, nextSibling);
		}

		public String getDescription()
		{
			String prefix = removedEl.getPrefix();
			String localName = removedEl.getLocalName();
			String qname = prefix != null? prefix + ":" + localName : localName;
			return "remove node '" + qname + "'";
		}
	}


	public class AttrModifiedUndo implements Undoable
	{
		protected int type;
		protected Element element;
		protected String namespaceURI;
		protected String localName;
		protected String prefix;
		protected String value;
		protected String newValue;

		public AttrModifiedUndo(MutationEvent me)
		{
			element = (Element)me.getTarget();
			type = me.getAttrChange();

			// getRelatedNode returns null but according to the dom spec
			// it shoudl return the Attr node that was modified. Bug in Xerces?
			// Attr attr = (Attr)me.getRelatedNode();

			// FIXME
			// this is a work around but is not namespace safe
			Attr attr = element.getAttributeNode(me.getAttrName());
			if (attr.getLocalName() != null)
			{
				namespaceURI = attr.getNamespaceURI();
				localName = attr.getLocalName();
				prefix = attr.getPrefix();
			}
			else
			{
				localName = attr.getName();
			}
			value = me.getPrevValue();
			newValue = attr.getValue();
		}

		public void undo()
		{
			if (type == MutationEvent.ADDITION)
			{
				if (namespaceURI != null)
					element.removeAttributeNS(namespaceURI, localName);
				else
					element.removeAttribute(localName);
			}
			else
			{
				if (namespaceURI != null)
				{
					element.setAttributeNS(namespaceURI, localName, value);
					Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
					if (prefix != null)
						attr.setPrefix(prefix);
				}
				else
				{
					element.setAttribute(localName, value);
				}
			}
		}

		public String getDescription()
		{
			String qname = prefix != null? prefix + ":" + localName : localName;
			String elQName = element.getPrefix() != null ? element.getPrefix() +
				":" + element.getLocalName() : element.getLocalName();
			switch(type)
			{
				case MutationEvent.ADDITION:
					return "add attribute '" + qname + "' to '" + elQName + "'";
				case MutationEvent.MODIFICATION:
					return "change attribute '" + qname + "' of '" + elQName +
						"' to '" + newValue + "'";
				case MutationEvent.REMOVAL:
					return "removal of attribute '" + qname + "' from '" +
						elQName + "'";
			}
			return "Undo attribute modification";
		}
	}


	public UndoAction getUndoAction()
	{
		return undoAction;
	}

}
