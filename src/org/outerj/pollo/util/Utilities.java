package org.outerj.pollo.util;

import javax.swing.*;

public class Utilities
{
    /**
     * Creates a JMenuItem based on an Action. The Swing implementation
     * in JDK 1.3 will not copy the accelerator from the action to the
     * JMenuItem, therefore this method will do this additionally.
     */
    public static JMenuItem createMenuItemFromAction(Action action)
    {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setAction(action);
        menuItem.setAccelerator((KeyStroke)action.getValue(Action.ACCELERATOR_KEY));

        return menuItem;
    }
}
