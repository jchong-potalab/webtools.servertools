/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.server.ui.internal.wizard.page;

import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.core.internal.IInstallableServer;
import org.eclipse.wst.server.ui.internal.Messages;
import org.eclipse.wst.server.ui.internal.SWTUtil;
import org.eclipse.wst.server.ui.internal.viewers.InstallableServerComposite;
import org.eclipse.wst.server.ui.internal.wizard.NewInstallableServerWizard;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
/**
 * A composite used to select a server to install.
 */
public class NewInstallableServerComposite extends Composite {
	private TaskModel taskModel;
	private IWizardHandle wizard;

	/**
	 * Create a new NewInstallableServerComposite.
	 * 
	 * @param parent a parent composite
	 * @param taskModel a task model
	 * @param wizard the wizard this composite is contained in
	 */
	public NewInstallableServerComposite(Composite parent, TaskModel taskModel, IWizardHandle wizard) {
		super(parent, SWT.NONE);
		this.taskModel = taskModel;
		this.wizard = wizard;
		
		createControl();
	}

	/**
	 * Creates the UI of the page.
	 */
	protected void createControl() {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = SWTUtil.convertHorizontalDLUsToPixels(this, 4);
		layout.verticalSpacing = SWTUtil.convertVerticalDLUsToPixels(this, 4);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 3;
		setLayout(layout);
		//WorkbenchHelp.setHelp(this, ContextIds.SELECT_CLIENT_WIZARD);
		
		Label label = new Label(this, SWT.WRAP);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		data.horizontalSpan = 3;
		data.widthHint = 225;
		label.setLayoutData(data);
		label.setText(Messages.wizNewInstallableServerMessage);
		
		InstallableServerComposite comp = new InstallableServerComposite(this, SWT.NONE, new InstallableServerComposite.InstallableServerSelectionListener() {
			public void installableServerSelected(IInstallableServer server) {
				handleSelection(server);
			}
		});
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		comp.setLayoutData(data);
		
		Dialog.applyDialogFont(this);
	}

	protected void handleSelection(IInstallableServer server) {
		taskModel.putObject("installableServer", server);
		NewInstallableServerWizard.invalidateLicense(taskModel);
		wizard.update();
	}
}