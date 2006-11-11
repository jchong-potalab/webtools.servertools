/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.internet.monitor.core.internal;
/**
 * Helper class to route trace output.
 */
public class Trace {
	/**
	 * Config trace event.
	 */
	public static byte CONFIG = 0;
	
	/**
	 * Warning trace event.
	 */
	public static byte WARNING = 1;
	
	/**
	 * Severe trace event.
	 */
	public static byte SEVERE = 2;
	
	/**
	 * Finest trace event.
	 */
	public static byte FINEST = 3;
	
	/**
	 * Parsing trace event.
	 */
	public static byte PARSING = 4;

	/**
	 * Trace constructor comment.
	 */
	private Trace() {
		super();
	}
	
	/**
	 * Trace the given text.
	 *
	 * @param level the trace level
	 * @param s a message
	 */
	public static void trace(byte level, String s) {
		trace(level, s, null);
	}
	
	/**
	 * Trace the given message and exception.
	 *
	 * @param level the trace level
	 * @param s a message
	 * @param t a throwable
	 */
	public static void trace(byte level, String s, Throwable t) {
		if (!MonitorPlugin.getInstance().isDebugging())
			return;

		System.out.println(MonitorPlugin.PLUGIN_ID + " " + System.currentTimeMillis() + " " + s);
		if (t != null)
			t.printStackTrace();
	}
}