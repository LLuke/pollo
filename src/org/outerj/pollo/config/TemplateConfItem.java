package org.outerj.pollo.config;

import org.outerj.pollo.template.ITemplate;
import org.outerj.pollo.template.ITemplateFactory;
import org.outerj.pollo.xmleditor.ComponentManager;
import org.outerj.pollo.xmleditor.exception.PolloException;

public class TemplateConfItem extends ConfItem
{
    public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
            org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

    protected String description;

    public TemplateConfItem()
    {
    }

    public ITemplate createTemplate()
        throws PolloException
    {
        ITemplate template = null;
        try
        {
            ITemplateFactory templateFactory = (ITemplateFactory)ComponentManager.getFactoryInstance(getFactoryClass());
            template = templateFactory.getTemplate(getInitParams());
        }
        catch (Exception e)
        {
            logcat.error("Exception creating template", e);
            throw new PolloException("[TemplateConfItem] Error creating template.", e);
        }
        return template;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public String toString()
    {
        return description;
    }
}
