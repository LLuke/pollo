package org.outerj.pollo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.outerj.pollo.config.ViewTypeConf;
import org.outerj.pollo.dialog.AboutDialog;
import org.outerj.pollo.dialog.ErrorDialog;
import org.outerj.pollo.texteditor.XmlTextDocument;
import org.outerj.pollo.texteditor.XmlTextEditor;
import org.outerj.pollo.texteditor.XmlTextEditorPanel;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlEditorPanel;
import org.outerj.pollo.xmleditor.model.Undo;
import org.outerj.pollo.xmleditor.model.View;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.XmlModelListener;
import org.xml.sax.SAXParseException;


/**
 * Combination of XmlEditorPanel and XmlTextEditorPanel in one JFrame.
 *
 * The code looks a bit funny because I accidentely deleted the source and had
 * to decompile it. Will clean it up later.
 */
public class ViewFrame extends JFrame
    implements ActionListener, View, XmlModelListener, ChangeListener
{

    protected XmlEditorPanel xmlEditorPanel;
    protected XmlTextEditorPanel xmlTextEditorPanel;
    protected JTabbedPane tabbedPane;
    protected JMenu domEditMenu;
    protected JMenuBar domModeMenuBar;
    protected JMenuBar textModeMenuBar;
    protected XmlModel xmlModel;
    protected String frameTitle;

    public ViewFrame(XmlModel xmlmodel, ViewTypeConf viewtypeconf)
        throws Exception
    {
        frameTitle = "";
        xmlModel = xmlmodel;
        org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification idisplayspecification = viewtypeconf.createDisplaySpecChain();
        org.outerj.pollo.xmleditor.schema.ISchema ischema = viewtypeconf.createSchemaChain();
        org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin iattributeeditorplugin = viewtypeconf.createAttrEditorPluginChain(xmlmodel, ischema);
        xmlEditorPanel = new XmlEditorPanel(xmlmodel, null, idisplayspecification, ischema, iattributeeditorplugin);
        xmlTextEditorPanel = new XmlTextEditorPanel(xmlmodel, ischema);
        tabbedPane = new JTabbedPane();
        tabbedPane.add("Tree view", xmlEditorPanel);
        tabbedPane.add("Text view", xmlTextEditorPanel);
        tabbedPane.addChangeListener(this);
        if(xmlmodel.isInTextMode())
            tabbedPane.setSelectedComponent(xmlTextEditorPanel);
        getContentPane().add(tabbedPane, "Center");
        setDefaultCloseOperation(0);
        addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent event) { close(); }});
        xmlmodel.addListener(this);
        fileNameChanged(xmlmodel);
        createMenus();
        setSize(800, 600);
        pack();
    }

    public void createMenus()
    {
        domModeMenuBar = new JMenuBar();
        textModeMenuBar = new JMenuBar();
        JMenu jmenu = new JMenu("File");
        JMenu jmenu1 = new JMenu("File");
        JMenuItem jmenuitem = new JMenuItem(Pollo.getInstance().getFileOpenAction());
        JMenuItem jmenuitem1 = new JMenuItem(Pollo.getInstance().getFileOpenAction());
        jmenu.add(jmenuitem);
        jmenu1.add(jmenuitem1);
        JMenuItem jmenuitem2 = new JMenuItem(xmlModel.getSaveAction());
        JMenuItem jmenuitem3 = new JMenuItem(xmlModel.getSaveAction());
        jmenu.add(jmenuitem2);
        jmenu1.add(jmenuitem3);
        JMenuItem jmenuitem4 = new JMenuItem(xmlModel.getSaveAsAction());
        JMenuItem jmenuitem5 = new JMenuItem(xmlModel.getSaveAsAction());
        jmenu.add(jmenuitem4);
        jmenu1.add(jmenuitem5);
        JMenuItem jmenuitem6 = new JMenuItem(xmlModel.getCloseAction());
        JMenuItem jmenuitem7 = new JMenuItem(xmlModel.getCloseAction());
        jmenu.add(jmenuitem6);
        jmenu1.add(jmenuitem7);
        domModeMenuBar.add(jmenu);
        textModeMenuBar.add(jmenu1);
        JMenu jmenu2 = new JMenu("Edit");
        JMenuItem jmenuitem8 = new JMenuItem(xmlModel.getUndo().getUndoAction());
        jmenu2.add(jmenuitem8);
        jmenu2.addSeparator();
        XmlEditor xmleditor = xmlEditorPanel.getXmlEditor();
        jmenu2.add(new JMenuItem(xmleditor.getCopyAction()));
        jmenu2.add(new JMenuItem(xmleditor.getCutAction()));
        JMenu jmenu3 = new JMenu("Paste");
        jmenu3.add(new JMenuItem(xmleditor.getPasteBeforeAction()));
        jmenu3.add(new JMenuItem(xmleditor.getPasteAfterAction()));
        jmenu3.add(xmleditor.getPasteInsideAction());
        jmenu2.add(jmenu3);
        jmenu2.addSeparator();
        jmenu2.add(xmleditor.getCommentOutAction());
        jmenu2.add(xmleditor.getUncommentAction());
        domModeMenuBar.add(jmenu2);
        JMenu jmenu4 = new JMenu("Insert");
        JMenu jmenu5 = new JMenu("Text Node");
        jmenu5.add(xmleditor.getInsertTextBeforeAction());
        jmenu5.add(xmleditor.getInsertTextAfterAction());
        jmenu5.add(xmleditor.getInsertTextInsideAction());
        jmenu4.add(jmenu5);
        JMenu jmenu6 = new JMenu("Comment Node");
        jmenu6.add(xmleditor.getInsertCommentBeforeAction());
        jmenu6.add(xmleditor.getInsertCommentAfterAction());
        jmenu6.add(xmleditor.getInsertCommentInsideAction());
        jmenu4.add(jmenu6);
        JMenu jmenu7 = new JMenu("CDATA section");
        jmenu7.add(xmleditor.getInsertCDataBeforeAction());
        jmenu7.add(xmleditor.getInsertCDataAfterAction());
        jmenu7.add(xmleditor.getInsertCDataInsideAction());
        jmenu4.add(jmenu7);
        JMenu jmenu8 = new JMenu("Processing Instruction");
        jmenu8.add(xmleditor.getInsertPIBeforeAction());
        jmenu8.add(xmleditor.getInsertPIAfterAction());
        jmenu8.add(xmleditor.getInsertPIInsideAction());
        jmenu4.add(jmenu8);
        domModeMenuBar.add(jmenu4);
        JMenu jmenu9 = new JMenu("Tree");
        jmenu9.add(xmleditor.getCollapseAction());
        jmenu9.add(xmleditor.getExpandAction());
        jmenu9.addSeparator();
        jmenu9.add(xmleditor.getCollapseAllAction());
        jmenu9.add(xmleditor.getExpandAllAction());
        domModeMenuBar.add(jmenu9);
        JMenu jmenu10 = new JMenu("Edit");
        jmenu10.add(xmlTextEditorPanel.getDocument().getUndoAction());
        jmenu10.add(xmlTextEditorPanel.getDocument().getRedoAction());
        jmenu10.addSeparator();
        jmenu10.add(xmlTextEditorPanel.getEditor().getCutAction());
        jmenu10.add(xmlTextEditorPanel.getEditor().getCopyAction());
        jmenu10.add(xmlTextEditorPanel.getEditor().getPasteAction());
        textModeMenuBar.add(jmenu10);
        JMenu jmenu11 = new JMenu("View");
        JMenu jmenu12 = new JMenu("View");
        JMenuItem jmenuitem9 = new JMenuItem("New...");
        JMenuItem jmenuitem10 = new JMenuItem("New...");
        jmenuitem9.setActionCommand("new-view");
        jmenuitem9.addActionListener(this);
        jmenuitem10.setActionCommand("new-view");
        jmenuitem10.addActionListener(this);
        jmenu11.add(jmenuitem9);
        jmenu12.add(jmenuitem10);
        JMenuItem jmenuitem11 = new JMenuItem("Close");
        JMenuItem jmenuitem12 = new JMenuItem("Close");
        jmenuitem11.setActionCommand("close-view");
        jmenuitem11.addActionListener(this);
        jmenuitem12.setActionCommand("close-view");
        jmenuitem12.addActionListener(this);
        jmenu11.add(jmenuitem11);
        jmenu12.add(jmenuitem12);
        domModeMenuBar.add(jmenu11);
        textModeMenuBar.add(jmenu12);
        domModeMenuBar.add(Box.createHorizontalGlue());
        JMenu jmenu13 = new JMenu("Help");
        JMenuItem jmenuitem13 = new JMenuItem("About...");
        jmenuitem13.setActionCommand("about");
        jmenuitem13.addActionListener(this);
        jmenu13.add(jmenuitem13);
        domModeMenuBar.add(jmenu13);
        textModeMenuBar.add(Box.createHorizontalGlue());
        JMenu jmenu14 = new JMenu("Help");
        JMenuItem jmenuitem14 = new JMenuItem("About...");
        jmenuitem14.setActionCommand("about");
        jmenuitem14.addActionListener(this);
        jmenu14.add(jmenuitem14);
        textModeMenuBar.add(jmenu14);
        if(xmlModel.isInParsedMode())
            setJMenuBar(domModeMenuBar);
        else
        if(xmlModel.isInTextMode())
            setJMenuBar(textModeMenuBar);
    }

    public void actionPerformed(ActionEvent actionevent)
    {
        if(actionevent.getActionCommand().equals("new-view"))
            Pollo.getInstance().createView(xmlModel);
        else
        if(actionevent.getActionCommand().equals("close-view"))
            close();
        else
        if(actionevent.getActionCommand().equals("about"))
        {
            AboutDialog aboutdialog = new AboutDialog(this);
            aboutdialog.show();
        }
    }

    public void close()
    {
        try
        {
            if(xmlModel.closeView(this))
            {
                System.out.println("Frame closes, will do cleanup");
                xmlEditorPanel.disconnectFromDom();
                System.out.println("cleanup done");
                stop();
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public void stateChanged(ChangeEvent changeevent)
    {
        if(tabbedPane.getSelectedComponent() == xmlTextEditorPanel)
            try
            {
                xmlModel.switchToTextMode();
                setJMenuBar(textModeMenuBar);
            }
            catch(Exception exception)
            {
                tabbedPane.setSelectedComponent(xmlEditorPanel);
                ErrorDialog errordialog = new ErrorDialog(null, "Could not serialize the DOM tree to text.", exception);
                errordialog.show();
            }
        else
        if(tabbedPane.getSelectedComponent() == xmlEditorPanel)
            try
            {
                xmlModel.switchToParsedMode();
                setJMenuBar(domModeMenuBar);
            }
            catch(SAXParseException saxparseexception)
            {
                tabbedPane.setSelectedComponent(xmlTextEditorPanel);
                xmlTextEditorPanel.showParseException(saxparseexception);
                JOptionPane.showMessageDialog(this, "The document contains well formedness errors.");
            }
            catch(Exception exception1)
            {
                tabbedPane.setSelectedComponent(xmlTextEditorPanel);
                ErrorDialog errordialog1 = new ErrorDialog(null, "Could not parse the text to a DOM tree.", exception1);
                errordialog1.show();
            }
    }

    public void stop()
    {
        hide();
        dispose();
    }

    public void fileNameChanged(XmlModel xmlmodel)
    {
        File file = xmlmodel.getFile();
        if(file == null)
            frameTitle = "Untitled";
        else
            frameTitle = file.getName() + "  (" + file.getParentFile().getPath() + ")";
        if(xmlmodel.isModified())
            setTitle("*" + frameTitle);
        else
            setTitle(frameTitle);
    }

    public void lastViewClosed(XmlModel xmlmodel)
    {
    }

    public void fileChanged(XmlModel xmlmodel)
    {
        setTitle("*" + frameTitle);
    }

    public void fileSaved(XmlModel xmlmodel)
    {
        setTitle(frameTitle);
    }

    public void switchToTextMode(XmlModel xmlmodel)
    {
        xmlTextEditorPanel.jumpToBeginning();
        tabbedPane.setSelectedComponent(xmlTextEditorPanel);
    }

    public void switchToParsedMode(XmlModel xmlmodel)
    {
        xmlEditorPanel.reconnectToDom();
        tabbedPane.setSelectedComponent(xmlEditorPanel);
    }
}
