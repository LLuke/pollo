package org.outerj.pollo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * ColorSelector builds around the {@link ColorSlider} component by adding a label showing
 * the selected color, and adding fields for editing the R, G and B values manually.
 *
 * @author Bruno Dumon (bruno at outerthought dot org)
 */
public class ColorSelector extends JPanel
{
    protected ColorSlider colorSlider;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        ColorSelector colorSelector = new ColorSelector(new Color(200, 200, 200));
        frame.getContentPane().add(colorSelector);
        frame.pack();
        frame.show();
    }

    public ColorSelector(Color initialColor)
    {
        setLayout(new GridBagLayout());

        colorSlider = new ColorSlider();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(2, 2, 2, 2);
        ColorLabel colorLabel = new ColorLabel(colorSlider);
        add(colorLabel, gbc);
        add(colorSlider, gbc);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.insets = new Insets(2, 2, 2, 2);
        gbcField.anchor = GridBagConstraints.LINE_START;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.gridwidth = GridBagConstraints.RELATIVE;

        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.insets = new Insets(2, 2, 2, 2);
        gbcLabel.anchor = GridBagConstraints.LINE_START;

        GridBagConstraints gbcGlue = new GridBagConstraints();
        gbcGlue.gridwidth = GridBagConstraints.REMAINDER;

        add(new JLabel("Red:"), gbcLabel);
        add(new RedField(colorSlider), gbcField);
        add(Box.createHorizontalGlue(), gbcGlue);

        gbc.gridwidth = 1;
        add(new JLabel("Green:"), gbcLabel);
        add(new GreenField(colorSlider), gbcField);
        add(Box.createHorizontalGlue(), gbcGlue);

        gbc.gridwidth = 1;
        add(new JLabel("Blue:"), gbcLabel);
        add(new BlueField(colorSlider), gbcField);
        add(Box.createHorizontalGlue(), gbcGlue);

        colorSlider.setColor(initialColor);
    }

    public Color getColor()
    {
        return colorSlider.getColor();
    }

    public void setColor(Color color)
    {
        colorSlider.setColor(color);
    }

    public ColorSlider getColorSlider()
    {
        return colorSlider;
    }

    public class ColorLabel extends JLabel implements PropertyChangeListener
    {
        public ColorLabel(ColorSlider colorSlider)
        {
            super("Sample Color");
            setOpaque(true);
            colorSlider.addPropertyChangeListener(ColorSlider.COLOR, this);
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(ColorSlider.COLOR))
            {
                this.setBackground((Color) evt.getNewValue());
            }
        }
    }

    abstract class ColorField extends JTextField implements PropertyChangeListener
    {
        public static final int RED = 0, GREEN = 1, BLUE = 2;
        protected ColorSlider colorSlider;

        public ColorField(ColorSlider colorSlider)
        {
            super(3);
            this.colorSlider = colorSlider;
            colorSlider.addPropertyChangeListener(ColorSlider.COLOR, this);
            addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        int x = Integer.parseInt(getText());
                        valueChanged(x);
                    }
                    catch (NumberFormatException exc)
                    {
                    }
                }
            });
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(ColorSlider.COLOR))
            {
                applyChange((Color) evt.getNewValue());
            }
        }

        public abstract void applyChange(Color newColor);

        public abstract void valueChanged(int newValue);
    }

    class RedField extends ColorField
    {
        public RedField(ColorSlider colorSlider)
        {
            super(colorSlider);
        }

        public void applyChange(Color newColor)
        {
            setText(String.valueOf(newColor.getRed()));
        }

        public void valueChanged(int newValue)
        {
            Color currentColor = colorSlider.getColor();
            colorSlider.setColor(new Color(newValue, currentColor.getGreen(), currentColor.getBlue()));
        }
    }

    class GreenField extends ColorField
    {
        public GreenField(ColorSlider colorSlider)
        {
            super(colorSlider);
        }

        public void applyChange(Color newColor)
        {
            setText(String.valueOf(newColor.getGreen()));
        }

        public void valueChanged(int newValue)
        {
            Color currentColor = colorSlider.getColor();
            colorSlider.setColor(new Color(currentColor.getRed(), newValue, currentColor.getBlue()));
        }
    }

    class BlueField extends ColorField
    {
        public BlueField(ColorSlider colorSlider)
        {
            super(colorSlider);
        }

        public void applyChange(Color newColor)
        {
            setText(String.valueOf(newColor.getBlue()));
        }

        public void valueChanged(int newValue)
        {
            Color currentColor = colorSlider.getColor();
            colorSlider.setColor(new Color(currentColor.getRed(), currentColor.getGreen(), newValue));
        }
    }
}
