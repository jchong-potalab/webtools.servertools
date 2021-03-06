/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.internet.monitor.ui.tests;

import org.eclipse.wst.internet.monitor.ui.internal.provisional.MonitorUICore;

import junit.framework.TestCase;

public class MonitorUICoreTest extends TestCase {
	public void testRequests() {
		assertNotNull(MonitorUICore.getRequests());
	}
}