/**********************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.wst.server.preview.internal;

import org.eclipse.osgi.util.NLS;
/**
 * Translated messages.
 */
public class Messages extends NLS {
	public static String errorLocation;
	public static String errorJRE;
	public static String classpathContainerDescription;
	public static String classpathContainer;
	public static String classpathContainerUnbound;

	public static String copyingTask;
	public static String deletingTask;
	public static String errorCopyingFile;
	public static String errorCreatingZipFile;
	public static String errorDelete;
	public static String errorRename;
	public static String errorReading;
	public static String updateClasspathContainers;
	public static String errorNoRuntime;
	public static String errorFacet;
	public static String errorDeleting;
	public static String errorNotADirectory;
	public static String errorMkdir;

	static {
		NLS.initializeMessages(PreviewServerPlugin.PLUGIN_ID + ".internal.Messages", Messages.class);
	}
}