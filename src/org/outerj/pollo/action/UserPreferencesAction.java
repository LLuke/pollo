package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.Pollo;
import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.config.PolloConfiguration;
import org.outerj.pollo.gui.UserPreferencesDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action that shows the user preferences dialog.
 */
public class UserPreferencesAction extends AbstractAction
{
    protected PolloFrame polloFrame;
    protected PolloConfiguration polloConfiguration;

    public UserPreferencesAction(PolloFrame polloFrame, PolloConfiguration polloConfiguration)
    {
        this.polloFrame = polloFrame;
        this.polloConfiguration = polloConfiguration;
        final ResourceManager resourceManager = ResourceManager.getManager(UserPreferencesAction.class);
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        UserPreferencesDialog dialog = UserPreferencesDialog.getInstance();
        dialog.readConfiguration(polloConfiguration);
        if (dialog.showDialog(polloFrame))
        {
            dialog.storeConfiguration(polloConfiguration);
        }
    }
}
