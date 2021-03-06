/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.wst.server.tests.performance;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.wst.server.ui.tests.dialog.WizardTestCase;

public class AddRemoveModulesWizardTestCase extends PerformanceTestCase {
	public void testAddRemoveModulesWizard() throws Exception {
		Dimension[] dims = new Dimension[] {Dimension.ELAPSED_PROCESS, Dimension.USED_JAVA_HEAP};
		tagAsGlobalSummary("Add/remove modules wizard", dims);
		
		for (int i = 0; i < 15; i++) {
			startMeasuring();
			WizardTestCase.testRoS(null);
			stopMeasuring();
		}
		commitMeasurements();
		assertPerformance();
	}
}
