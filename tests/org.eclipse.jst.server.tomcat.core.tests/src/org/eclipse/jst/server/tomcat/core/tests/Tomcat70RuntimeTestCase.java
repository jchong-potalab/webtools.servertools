/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.server.tomcat.core.tests;

import junit.framework.TestSuite;

public class Tomcat70RuntimeTestCase extends AbstractTomcatRuntimeTestCase {
	protected String getRuntimeTypeId() {
		return "org.eclipse.jst.server.tomcat.runtime.70";
	}

	public static void addOrderedTests(TestSuite suite) {
		AbstractTomcatRuntimeTestCase.addOrderedTests(Tomcat70RuntimeTestCase.class, suite);
	}
}
