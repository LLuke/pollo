package org.outerj.pollo;

import org.outerj.pollo.xmleditor.model.XmlModel;
import javax.swing.JFrame;
import java.lang.reflect.Constructor;

public class ViewFactory
{
	public static ViewEngine createView(XmlModel xmlModel, String viewEngineName)
		throws Exception
	{
		String viewEngineClassName = Pollo.getInstance().getProperty("viewtype." + viewEngineName + ".viewengine");
		Class viewEngineClass = Class.forName(viewEngineClassName);
		Constructor viewEngineConstructor = viewEngineClass.getConstructor(
				new Class [] { xmlModel.getClass(), Class.forName("java.lang.String") } );
		return (ViewEngine)viewEngineConstructor.newInstance(new Object [] { xmlModel, viewEngineName });
	}

	public static ViewFrame createViewFrame(XmlModel xmlModel, String viewEngineClassName)
		throws Exception
	{
		ViewEngine viewEngine = createView(xmlModel, viewEngineClassName);
		ViewFrame frame = new ViewFrame(viewEngine);

		return frame;
	}
}
