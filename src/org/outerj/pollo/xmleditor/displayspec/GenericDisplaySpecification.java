package org.outerj.pollo.xmleditor.displayspec;

import org.outerj.pollo.xmleditor.util.NodeMap;
import org.outerj.pollo.xmleditor.ElementColorIcon;

import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;
import javax.swing.Icon;


/**
 * An implementation of the IDisplaySpecification that can be used
 * for any XML file.
 *
 * It can both generate colors on the fly or automatically assign colors.
 *
 * @author Bruno Dumon
 */
public class GenericDisplaySpecification implements IDisplaySpecification
{
	/** Default color for elements. */
	protected Color defaultColor;
	/** Font to use for element names. */
	protected Font elementNameFont;
	/** Font to use for attribute names. */
	protected Font attributeNameFont;
	/** Font to use for attribute values. */
	protected Font attributeValueFont;
	/** Color to use as the background of the XmlEditor. */
	protected Color backgroundColor = new Color(235, 235, 235); // light grey
	/** Contains the instances of the ElementSpec class */
	protected NodeMap elementSpecs = new NodeMap();
	/** Indicates if this implementation should randomly assign colors. */
	protected boolean useColors = true;

	/** List of colors to use for elements. The list is stolen from somewhere
	 * in koffice.
	 */
	protected static Color[] colors = {
		new Color(229,229,229) // grey 90%
			, new Color( 255 ,   255 ,   224 ) // Light Yellow
			, new Color( 255 ,   239 ,   213 ) // Papaya Whip
			, new Color( 238 ,   221 ,   130 ) // Light Goldenrod
			, new Color( 135 ,   206 ,   250 ) // Light Skyblue
			, new Color( 143 ,   188 ,   143 ) // Dark Seagreen
			, new Color( 204 ,   204 ,   204 ) // Grey 80%
			, new Color( 240 ,   255 ,   255 ) // Azure
			, new Color( 255 ,   235 ,   205 ) // Blanched Almond
			, new Color( 211 ,   211 ,   211 ) // Light Grey
			, new Color( 255 ,   160 ,   122 ) // Light Salmon
			, new Color( 218 ,   112 ,   214 ) // Orchid
			, new Color( 178 ,   178 ,   178 ) // Grey 70%
			, new Color( 248 ,   248 ,   255 ) // Ghost White
			, new Color( 250 ,   235 ,   215 ) // Antique White
			, new Color( 255 ,   182 ,   193 ) // Light Pink
			, new Color( 135 ,   206 ,   235 ) // Sky Blue
			, new Color( 102 ,   205 ,   170 ) // Medium Aquamarine
			, new Color( 153 ,   153 ,   153 ) // Grey 60%
			, new Color( 240 ,   255 ,   240 ) // Honeydew
			, new Color( 255 ,   228 ,   225 ) // Misty Rose
			, new Color( 176 ,   224 ,   230 ) // Powder Blue
			, new Color( 210 ,   180 ,   140 ) // Tan
			, new Color( 255 ,   127 ,   80  ) // Coral
			, new Color( 255 ,   245 ,   238 ) // Seashell
			, new Color( 230 ,   230 ,   250 ) // Lavender
			, new Color( 127 ,   255 ,   212 ) // Aquamarine
			, new Color( 238 ,   130 ,   238 ) // Violet
			, new Color( 154 ,   205 ,   50  ) // Yellow Green
			, new Color( 240 ,   248 ,   255 ) // Alice Blue
			, new Color( 255 ,   228 ,   196 ) // Bisque
			, new Color( 216 ,   191 ,   216 ) // Thistle
			, new Color( 244 ,   164 ,   96  ) // Sandy Brown
			, new Color( 218 ,   165 ,   32  ) // Goldenrod
			, new Color( 255 ,   248 ,   220 ) // Cornsilk
			, new Color( 255 ,   228 ,   181 ) // Moccasin
			, new Color( 173 ,   216 ,   230 ) // Light Blue
			, new Color( 233 ,   150 ,   122 ) // Dark Salmon
			, new Color( 72  ,   209 ,   204 ) // Medium Turquoise
			, new Color( 255 ,   240 ,   245 ) // Lavender Blush
			, new Color( 255 ,   222 ,   173 ) // Navajo White
			, new Color( 152 ,   251 ,   152 ) // Pale Green
			, new Color( 189 ,   183 ,   107 ) // Dark khaki
			, new Color( 188 ,   143 ,   143 ) // Rosy Brown
			, new Color( 253 ,   245 ,   230 ) // Old Lace
			, new Color( 255 ,   218 ,   185 ) // Peach Puff
			, new Color( 255 ,   215 ,   0   ) // Gold
			, new Color( 127 ,   255 ,   0   ) // Chartreuse
			, new Color( 219 ,   112 ,   147 ) // Pale VioletRed
			, new Color( 245 ,   245 ,   245 ) // White Smoke
			, new Color( 238 ,   232 ,   170 ) // Pale Goldenrod
			, new Color( 173 ,   255 ,   47  ) // Green Yellow
			, new Color( 169 ,   169 ,   169 ) // Dark Gray
			, new Color( 0   ,   250 ,   154 ) // Medium Spring Green
			, new Color( 255 ,   255 ,   240 ) // Ivory
			, new Color( 255 ,   250 ,   205 ) // Lemon Chiffon
			, new Color( 245 ,   222 ,   179 ) // Wheat
			, new Color( 176 ,   196 ,   222 ) // Light Steel Blue
			, new Color( 124 ,   252 ,   0   ) // Lawn Green
			, new Color( 255 ,   99  ,   71  ) // Tomato
			, new Color( 255 ,   250 ,   250 ) // Snow
			, new Color( 224 ,   255 ,   255 ) // Light Cyan
			, new Color( 220 ,   220 ,   220 ) // Gainsboro
			, new Color( 255 ,   105 ,   180 ) // Hot Pink
			, new Color( 0   ,   255 ,   127 ) // Spring Green
			, new Color( 245 ,   255 ,   250 ) // Mint Cream
			, new Color( 250 ,   250 ,   210 ) // Light Goldenrod Yellow
			, new Color( 240 ,   230 ,   140 ) // Khaki
			, new Color( 144 ,   238 ,   144 ) // Light Green
			, new Color( 221 ,   160 ,   221 ) // Plum
			, new Color( 250 ,   128 ,   114 ) // Salmon
			, new Color( 205 ,   133 ,   63  ) // Peru
			, new Color( 255 ,   250 ,   240 ) // Floral White
			, new Color( 250 ,   240 ,   230 ) // Linen
			, new Color( 175 ,   238 ,   238 ) // Pale Turquoise
			, new Color( 190 ,   190 ,   190 ) // Gray
			, new Color( 240 ,   128 ,   128 ) // Light Coral
			, new Color( 100 ,   149 ,   237 ) // Cornflower Blue
			, new Color( 245 ,   245 ,   220 ) // Beige
			, new Color( 255 ,   192 ,   203 ) // Pink
			, new Color( 222 ,   184 ,   135 ) // Burly Wood
			, new Color( 64  ,   224 ,   208 ) // Turquoise
			, new Color( 132 ,   112 ,   255 ) // Light Slate Blue
	};

	protected static int numberOfColors = colors.length;
	protected int colorPointer = 0;


	public void init(HashMap initParams)
	{
		defaultColor = new Color(255, 255, 255);
		elementNameFont = new Font("Default", 0, 12);
		attributeNameFont = new Font("Default", Font.ITALIC, 12);
		attributeValueFont = new Font("Default", 0, 12);

		String useColorsParam = (String)initParams.get("use-colors");
		if (useColorsParam != null && useColorsParam.equals("false"))
			useColors = false;
	}

	protected void addElementSpec(ElementSpec elementSpec)
	{
		elementSpecs.put(elementSpec.nsUri, elementSpec.localName, elementSpec);
	}

	public ElementSpec getElementSpec(String uri, String localName)
	{
		ElementSpec elementSpec = (ElementSpec)elementSpecs.get(uri, localName);
		if (elementSpec == null)
		{
			elementSpec = new ElementSpec();
			elementSpec.nsUri = uri;
			elementSpec.localName = localName;
			elementSpec.attributesToShow = new ArrayList();
			if (useColors)
			{
				elementSpec.backgroundColor = colors[colorPointer % numberOfColors];
				colorPointer++;
			}
			else
			{
				elementSpec.backgroundColor = defaultColor;
			}
			elementSpec.viewType = elementSpec.BLOCK_VIEW;
			elementSpec.icon = new ElementColorIcon(defaultColor);
			addElementSpec(elementSpec);
		}
		return elementSpec;
	}

	public Font getAttributeNameFont()
	{
		return attributeNameFont;
	}

	public Font getAttributeValueFont()
	{
		return attributeValueFont;
	}

	public Font getElementNameFont()
	{
		return elementNameFont;
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}
}
