package org.outerj.xmleditor.view;

import java.awt.font.TextLayout;
import org.outerj.xmleditor.DisplaySpecification.AttributeSpec;

public class AttrViewInfo
{
	public AttributeSpec attributeSpec;

	public int namePos;
	public int valuePos;
	public TextLayout valueLayout;
	public TextLayout nameLayout;
	public boolean visible;
}
