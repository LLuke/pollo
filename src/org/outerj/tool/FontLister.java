package org.outerj.tool;

import java.awt.*;


public class FontLister
{
	public static void main(String [] args)
	{
		
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String [] fontnames = ge.getAvailableFontFamilyNames();
		for (int i = 0; i < fontnames.length; i++)
		{
			System.out.println(fontnames[i]);
		}
	}
}
