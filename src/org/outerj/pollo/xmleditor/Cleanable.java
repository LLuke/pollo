package org.outerj.pollo.xmleditor;

public interface Cleanable
{
	/**
	 * In the cleanup method, an object should release any
	 * resources it uses, as well any event listeners it has
	 * registered.
	 */
	public void cleanup();
}
