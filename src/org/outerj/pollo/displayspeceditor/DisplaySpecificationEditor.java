package org.outerj.pollo.displayspeceditor;

import org.outerj.pollo.displayspeceditor.model.DisplaySpecification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Utility application to edit display specification files. This class defines the
 * main window, the menus and associated actions, an manages the currently opened
 * file.
 *
 * @author Bruno Dumon
 */
public class DisplaySpecificationEditor extends JFrame
{
    /** Currently opened display specification, null if none is open. */
    private DisplaySpecification currentDisplaySpecification = null;
    private DisplaySpecificationEditorPanel currentDisplaySpecificationEditorPanel = null;
    private JMenu editMenu;

    public static void main(String[] args)
    {
        DisplaySpecificationEditor editor = new DisplaySpecificationEditor();
        editor.show();
    }

    public DisplaySpecificationEditor()
    {
        super("Pollo Display Specification Editor");

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new NewAction());
        fileMenu.add(new OpenAction());
        fileMenu.add(new SaveAction());
        fileMenu.add(new CloseAction());
        fileMenu.add(new ExitAction());

        editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        helpMenu.add(new AboutAction());

        getContentPane().setLayout(new BorderLayout());

        setSize(400, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (!close())
                    show();
                else
                    System.exit(0);
            }
        });
    }

    private void setCurrentFile(DisplaySpecification displaySpecification)
    {
        currentDisplaySpecification = displaySpecification;
        currentDisplaySpecificationEditorPanel = new DisplaySpecificationEditorPanel(displaySpecification);
        getContentPane().add(currentDisplaySpecificationEditorPanel, BorderLayout.CENTER);
        editMenu.add(displaySpecification.getUndoAction());
        pack();
    }

    public class OpenAction extends AbstractAction
    {
        public OpenAction()
        {
            super("Open...");
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!close())
                return;

            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(DisplaySpecificationEditor.this) != JFileChooser.APPROVE_OPTION)
                return;
            File file = fileChooser.getSelectedFile();
            DisplaySpecification displaySpecification = new DisplaySpecification();
            try
            {
                displaySpecification.load(file);
            }
            catch (Exception exc)
            {
                exc.printStackTrace();
                JOptionPane.showMessageDialog(DisplaySpecificationEditor.this, exc.getMessage());
            }
            setCurrentFile(displaySpecification);
        }
    }

    public class CloseAction extends AbstractAction
    {
        public CloseAction()
        {
            super("Close");
        }

        public void actionPerformed(ActionEvent e)
        {
            close();
        }
    }

    public class AboutAction extends AbstractAction
    {
        public AboutAction()
        {
            super("About...");
        }

        public void actionPerformed(ActionEvent e)
        {
            JOptionPane.showMessageDialog(DisplaySpecificationEditor.this, "Pollo Display Specification Editor 0.01\n(C) Copyright Bruno Dumon, 2003.\n\nThis program is free, open source software.\nYour help in improving it is welcome.\nFor license information, usage instructions, and so on\nsee the web page at\nhttp://pollo.sf.net/dseditor.html");
        }
    }

    public class NewAction extends AbstractAction
    {
        public NewAction()
        {
            super("New");
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!close())
                return;

            setCurrentFile(new DisplaySpecification());
        }
    }

    public class ExitAction extends AbstractAction
    {
        public ExitAction()
        {
            super("Exit...");
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!close())
                return;

            System.exit(0);
        }
    }

    public class SaveAction extends AbstractAction
    {
        public SaveAction()
        {
            super("Save");
        }

        public void actionPerformed(ActionEvent e)
        {
            save();
        }
    }

    /**
     * @return false if the user canceled the operation
     */
    public boolean save()
    {
        if (currentDisplaySpecification == null)
            return true;

        if (currentDisplaySpecification.getFile() == null)
        {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return false;
            File file = fileChooser.getSelectedFile();
            currentDisplaySpecification.setFile(file);
        }
        try
        {
            currentDisplaySpecification.store();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * @return false if the user cancelled the operation
     */
    public boolean close()
    {
        if (currentDisplaySpecification != null)
        {
            int result = JOptionPane.showConfirmDialog(this, "Save current file?", "Closing current file", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION)
                return false;
            else if (result == JOptionPane.YES_OPTION)
                save();
            getContentPane().remove(currentDisplaySpecificationEditorPanel);
            repaint();
            currentDisplaySpecification = null;
            currentDisplaySpecificationEditorPanel = null;
            editMenu.removeAll();
        }
        return true;
    }
}
