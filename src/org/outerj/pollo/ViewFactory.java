package org.outerj.pollo;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.config.ViewTypeConf;

import javax.swing.JFrame;
import java.lang.reflect.Constructor;

public class ViewFactory
{
	public static ViewEngine createView(XmlModel xmlModel, ViewTypeConf viewTypeConf)
		throws Exception
	{
		String viewEngineClassName = viewTypeConf.getClassName();
		Class viewEngineClass = Class.forName(viewEngineClassName);
		Constructor viewEngineConstructor = viewEngineClass.getConstructor(
				new Class [] { xmlModel.getClass(), ViewTypeConf.class } );
		return (ViewEngine)viewEngineConstructor.newInstance(new Object [] { xmlModel, viewTypeConf });
	}

	public static ViewFrame createViewFrame(XmlModel xmlModel, ViewTypeConf viewTypeConf)
		throws Exception
	{
		ViewEngine viewEngine = createView(xmlModel, viewTypeConf);
		ViewFrame frame = new ViewFrame(viewEngine);

		return frame;
	}
}
