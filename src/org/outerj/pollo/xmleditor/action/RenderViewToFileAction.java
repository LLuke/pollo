package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import org.w3c.dom.Node;

/**
 * An action that renders the selected view to an image and
 * stores it in a file.
 *
 * @author Bruno Dumon
 */
public class RenderViewToFileAction extends AbstractAction
{
	protected XmlEditor xmlEditor;
	protected JFileChooser fileChooser;

	public RenderViewToFileAction(XmlEditor xmlEditor)
	{
		super("Store as image (jpeg)...");
		this.xmlEditor = xmlEditor;
	}

	public void actionPerformed(ActionEvent event)
	{
		View selectedView = xmlEditor.getSelectionInfo().getSelectedNodeView();
		if (selectedView != null)
		{
			// create the image
			int width = selectedView.getWidth() + 1;
			int height = selectedView.getHeight() + 2;
			BufferedImage image;
			try
			{
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			}
			catch (OutOfMemoryError e)
			{
				JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
						"Not enough memory to create image.");	
				return;
			}

			Graphics2D gr = image.createGraphics();
			gr.setClip(0, 0, width, height);

			// unselect the selected view, so that the selection highlight won't be rendered
			xmlEditor.getSelectionInfo().unselect();

			// draw the background
			Color backgroundColor = xmlEditor.getDisplaySpec().getBackgroundColor();
			gr.setColor(backgroundColor);
			gr.fillRect(0, 0, width, height);

			// draw the view
			selectedView.paint(gr, 0, 0);

			// ask user for a filename
			if (fileChooser == null)
				fileChooser = new JFileChooser();
			int returnVal = fileChooser.showOpenDialog(xmlEditor.getTopLevelAncestor());
			if(returnVal != JFileChooser.APPROVE_OPTION)
				return;

			File file = fileChooser.getSelectedFile();
			
			// save it as a jpeg (png would be better but not supported by jdk 1.3)
			try
			{
				OutputStream ostream = new FileOutputStream(file);
				ByteArrayOutputStream bstream = new ByteArrayOutputStream();
				JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(bstream);
				JPEGEncodeParam params = JPEGCodec.getDefaultJPEGEncodeParam(image);
				params.setQuality(1f, true);
				jpegEncoder.encode(image, params);
				ostream.write(bstream.toByteArray());
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
						"Error storing image: " + e.getMessage());	
			}
		}
	}
}
