package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;

import java.awt.Graphics;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Rectangle;


/**
  This class contains functionality that's shared between block views
  (such as ElementBlockView and CommentView).
 */
public abstract class BlockView implements View
{
	protected boolean isCollapsed = false;
	protected View parentView;
	protected XmlEditor xmlEditor;
	protected int width;

	public static final BasicStroke STROKE_LIGHT = new BasicStroke(1f);
	public static final BasicStroke STROKE_HEAVY = new BasicStroke(3f);

	public BlockView(View parentView, XmlEditor xmlEditor)
	{
		this.parentView = parentView;
		this.xmlEditor = xmlEditor;
	}

	public void drawCollapseSign(Graphics g, boolean isCollapsed, int startH, int startV)
	{
		g.drawRect(startH, startV, 8, 8);
		if (isCollapsed)
		{
			// draw '+' sign
			g.drawLine(startH + 2, startV + 4, startH + 6, startV + 4);
			g.drawLine(startH + 4, startV + 2, startH + 4, startV + 6);
		}
		else
		{
			// draw '-' sign
			g.drawLine(startH + 2, startV + 4, startH + 6, startV + 4);
		}
	}

	/**
	  Changes the height of the editing widget
	 */
	protected void resetSize()
	{
		xmlEditor.setSize(new Dimension(xmlEditor.getWidth(), getHeight()));
		xmlEditor.repaint(xmlEditor.getVisibleRect());
	}

	public boolean needsRepainting(int startV, int clipStartVertical, int clipEndVertical)
	{
		int absStartVertical = startV;
		int absEndVertical = absStartVertical + getHeight();


		if ((absStartVertical >= clipStartVertical && absEndVertical <= clipEndVertical)
				|| (absStartVertical <= clipStartVertical && absEndVertical >= clipStartVertical)
				|| (absStartVertical <= clipEndVertical && absEndVertical >= clipEndVertical)
				|| (absStartVertical <= clipStartVertical && absEndVertical >= clipEndVertical))
			return true;
		else
			return false;
	}

	/**
	  Does parentView.heightChanged if the parentView is not null, otherwise
	  resizes the JComponent (xmlEditor).
	 */
	public void applyHeightChange(int amount)
	{
		if (parentView != null)
			parentView.heightChanged(amount);
		else
		{
			resetSize();
		}
	}

	public void markAsSelected(int startH, int startV)
	{
		xmlEditor.setSelectedNode(getNode(), this);

		Rectangle redrawRect = new Rectangle(startH-2, startV-2, width+4, getHeight()+4);
		if (xmlEditor.getSelectedViewRect() != null)
			redrawRect.add(xmlEditor.getSelectedViewRect());
		xmlEditor.setSelectedViewRect(new Rectangle(startH-2, startV-2, width+4, getHeight()+4));
		Rectangle visibleRect = xmlEditor.getVisibleRect();
		redrawRect.y = visibleRect.y > redrawRect.y ? visibleRect.y : redrawRect.y;
		redrawRect.height = visibleRect.y + visibleRect.height - redrawRect.y;

		xmlEditor.repaint(redrawRect);
	}

	/**
	 * Indicates if this view is collapsed.
	 */
	public boolean isCollapsed()
	{
		return isCollapsed;
	}

	/**
	 * Indicates if this view supports collapsing (and expanding).
	 * Default implementation always returns false.
	 */
	public boolean isCollapsable()
	{
		return false;
	}

	/**
	 * Collapses this view. If the view is not hidden inside another collapsed
	 * view, then height change caused by this collapsing will be propagated upwards
	 * in the view tree. Otherwise, {@link #invalidateHeight} is called.
	 */
	public void collapse()
	{
		if (!isCollapsable())
			return;

		boolean trackHeight = (parentView == null) ||
			(parentView != null && parentView.isCollapsed() != true);
		int oldHeight = 0, newHeight = 0;
		
		if (trackHeight) oldHeight = this.getHeight();
		isCollapsed = true;
		if (trackHeight) newHeight = this.getHeight();

		if (trackHeight)
			applyHeightChange(newHeight - oldHeight);

		invalidateHeight();
	}

	/**
	 * Same as for {@link #collapse}.
	 */
	public void expand()
	{
		if (!isCollapsable())
			return;

		boolean trackHeight = (parentView == null) ||
			(parentView != null && parentView.isCollapsed() != true);
		int oldHeight = 0, newHeight = 0;

		if (trackHeight) oldHeight = this.getHeight();
		isCollapsed = false;
		if (trackHeight) newHeight = this.getHeight();

		if (trackHeight) applyHeightChange(newHeight - oldHeight);

		invalidateHeight();
	}

	/**
	 * This implementation only collapses the current node, thus doesn't
	 * work recursively. Subclasses must overide this behaviour if needed.
	 */
	public void collapseAll()
	{
		collapse();
	}

	/**
	 * This implementation only expands the current node, thus doesn't
	 * work recursively. Subclasses must overide this behaviour if needed.
	 */
	public void expandAll()
	{
		expand();
	}

	/**
	 * This sets a flag that the height of this view is to be recalculated
	 * the next time it is requested.
	 * Default implementation is empty.
	 */
	public void invalidateHeight()
	{
	}
}
