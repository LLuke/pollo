package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.exception.PolloException;


public class XPathQuery
{
	protected String description;
	protected String expression;

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public String toString()
	{
		return description;
	}
}
