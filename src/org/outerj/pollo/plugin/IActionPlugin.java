package org.outerj.pollo.plugin;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.w3c.dom.Node;

import javax.swing.*;

public interface IActionPlugin
{
	/**
	 * @param menu menu to which menu items should be added
	 * @param selectedNode currently selected node, may be null
	 * @param xmlModel the XmlModel of the current document
	 * @param polloFrame the current polloFrame, usefull to use as parent for showing dialogs
	 */
	public void addActionsToPluginMenu(JMenu menu, Node selectedNode, XmlModel xmlModel, PolloFrame polloFrame);

}
