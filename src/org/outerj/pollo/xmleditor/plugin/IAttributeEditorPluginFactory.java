package org.outerj.pollo.xmleditor.plugin;

import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.schema.ISchema;

import java.util.HashMap;

public interface IAttributeEditorPluginFactory
{
	public IAttributeEditorPlugin getInstance(HashMap initParams, XmlModel xmlModel, ISchema schema)
		throws PolloException;
}
