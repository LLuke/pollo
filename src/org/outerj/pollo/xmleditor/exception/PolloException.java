package org.outerj.pollo.xmleditor.exception;

/**
 * Base class for pollo exceptions.
 *
 * @author Bruno Dumon
 */
public class PolloException extends Exception
{
	protected Exception nestedException;

	public PolloException(String message)
	{
		super(message);
	}

	public PolloException(String message, Exception nestedException)
	{
		super(message);
		this.nestedException = nestedException;
	}
}
