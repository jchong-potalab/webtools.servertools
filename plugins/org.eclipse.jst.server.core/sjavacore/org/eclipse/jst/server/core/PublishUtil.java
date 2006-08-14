/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.server.core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jst.server.core.internal.JavaServerPlugin;
import org.eclipse.jst.server.core.internal.Messages;
import org.eclipse.jst.server.core.internal.ProgressUtil;
import org.eclipse.jst.server.core.internal.Trace;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
/**
 * Utility class with an assortment of useful file methods.
 * 
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @plannedfor 2.0
 */
public class PublishUtil {
	// size of the buffer
	private static final int BUFFER = 10240;

	// the buffer
	private static byte[] buf = new byte[BUFFER];

	/**
	 * FileUtil cannot be created. Use static methods.
	 */
	private PublishUtil() {
		super();
	}

	/**
	 * Copy a file from a to b. Closes the input stream after use.
	 *
	 * @param in java.io.InputStream
	 * @param to java.lang.String
	 * @deprecated Unused - will be removed.
	 * @return a status
	 */
	public static IStatus copyFile(InputStream in, String to) {
		OutputStream out = null;
		
		try {
			out = new FileOutputStream(to);
			
			int avail = in.read(buf);
			while (avail > 0) {
				out.write(buf, 0, avail);
				avail = in.read(buf);
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error copying file", e);
			return new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCopyingFile, new String[] {to, e.getLocalizedMessage()}), e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception ex) {
				// ignore
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	/**
	 * Smart copy the given module resources to the given path.
	 * 
	 * @param resources
	 * @param path
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @throws CoreException
	 * @deprecated This method only returns a single error in the case of failure. Use publishSmart() instead.
	 */
	public static void smartCopy(IModuleResource[] resources, IPath path, IProgressMonitor monitor) throws CoreException {
		IStatus[] status = PublishUtil.publishSmart(resources, path, monitor);
		if (status != null && status.length > 0)
			throw new CoreException(status[0]);
	}

	/**
	 * Handle a delta publish.
	 * 
	 * @param kind
	 * @param path
	 * @param delta
	 * @throws CoreException
	 * @deprecated This method only returns a single error in the case of failure. Use publishDelta() instead.
	 */
	public static void handleDelta(int kind, IPath path, IModuleResourceDelta delta) throws CoreException {
		IStatus[] status = PublishUtil.publishDelta(delta, path, null);
		if (status != null && status.length > 0)
			throw new CoreException(status[0]);
	}

	/**
	 * 
	 * @param path
	 * @param file
	 * @deprecated does not fail or return status if delete doesn't work
	 */
	protected static void deleteFile(IPath path, IModuleFile file) {
		Trace.trace(Trace.PUBLISHING, "Deleting: " + file.getName() + " from " + path.toString());
		IPath path2 = path.append(file.getModuleRelativePath()).append(file.getName());
		path2.toFile().delete();
	}

	/**
	 * 
	 * @param resources
	 * @param path
	 * @throws CoreException
	 * @deprecated This method only returns a single error in the case of failure. Use publishFull() instead
	 */
	public static void copy(IModuleResource[] resources, IPath path) throws CoreException {
		IStatus[] status = PublishUtil.publishFull(resources, path, null);
		if (status != null && status.length > 0)
			throw new CoreException(status[0]);
	}

	/**
	 * Creates a new zip file containing the given module resources. Deletes the existing file
	 * (and doesn't create a new one) if resources is null or empty.
	 * 
	 * @param resources
	 * @param zipPath
	 * @throws CoreException
	 */
	public static void createZipFile(IModuleResource[] resources, IPath zipPath) throws CoreException {
		IStatus[] status = PublishUtil.publishZip(resources, zipPath, null);
		if (status != null && status.length > 0)
			throw new CoreException(status[0]);
	}

	/**
	 * Copy a file from a to b. Closes the input stream after use.
	 * 
	 * @param in an input stream
	 * @param to a path to copy to. the directory must already exist
	 * @param ts timestamp
	 * @throws CoreException if anything goes wrong
	 */
	private static void copyFile(InputStream in, IPath to, long ts, IModuleFile mf) throws CoreException {
		OutputStream out = null;
		
		File tempFile = null;
		try {
			File file = to.toFile();
			File tempDir = JavaServerPlugin.getInstance().getStateLocation().toFile();
			tempFile = File.createTempFile("tmp", "." + to.getFileExtension(), tempDir);
			
			out = new FileOutputStream(tempFile);
			
			int avail = in.read(buf);
			while (avail > 0) {
				out.write(buf, 0, avail);
				avail = in.read(buf);
			}
			
			out.close();
			out = null;
			
			moveTempFile(tempFile, file);
			
			if (ts != IResource.NULL_STAMP && ts != 0)
				file.setLastModified(ts);
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			IPath path = mf.getModuleRelativePath().append(mf.getName());
			Trace.trace(Trace.SEVERE, "Error copying file: " + to.toOSString() + " to " + path.toOSString(), e);
			throw new CoreException(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCopyingFile, path.toOSString(), e.getLocalizedMessage()), null));
		} finally {
			if (tempFile != null && tempFile.exists())
				tempFile.deleteOnExit();
			try {
				if (in != null)
					in.close();
			} catch (Exception ex) {
				// ignore
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	/**
	 * Utility method to recursively delete a directory.
	 *
	 * @param dir a directory
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public static IStatus[] deleteDirectory(File dir, IProgressMonitor monitor) {
		if (!dir.exists() || !dir.isDirectory())
			return new IStatus[] { new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorNotADirectory, dir.getAbsolutePath()), null) };
		
		List status = new ArrayList();
		
		try {
			File[] files = dir.listFiles();
			int size = files.length;
			monitor = ProgressUtil.getMonitorFor(monitor);
			monitor.beginTask(NLS.bind(Messages.deletingTask, new String[] { dir.getAbsolutePath() }), size * 10);
			
			// cycle through files
			boolean deleteCurrent = true;
			for (int i = 0; i < size; i++) {
				File current = files[i];
				if (current.isFile()) {
					if (!current.delete()) {
						status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, files[i].getAbsolutePath()), null));
						deleteCurrent = false;
					}
					monitor.worked(10);
				} else if (current.isDirectory()) {
					monitor.subTask(NLS.bind(Messages.deletingTask, new String[] {current.getAbsolutePath()}));
					IStatus[] stat = deleteDirectory(current, ProgressUtil.getSubMonitorFor(monitor, 10));
					if (stat != null && stat.length > 0) {
						deleteCurrent = false;
						addArrayToList(status, stat);
					}
				}
			}
			if (deleteCurrent && !dir.delete())
				status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, dir.getAbsolutePath()), null));
			monitor.done();
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error deleting directory " + dir.getAbsolutePath(), e);
			status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), null));
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	/**
	 * Smart copy the given module resources to the given path.
	 * 
	 * @param resources an array of module resources
	 * @param path an external path to copy to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status 
	 */
	public static IStatus[] publishSmart(IModuleResource[] resources, IPath path, IProgressMonitor monitor) {
		if (resources == null)
			return new IStatus[0];
		
		List status = new ArrayList();
		File toDir = path.toFile();
		File[] toFiles = toDir.listFiles();
		int fromSize = resources.length;
		
		if (toDir.exists() && toDir.isDirectory()) {
			int toSize = toFiles.length;
			// check if this exact file exists in the new directory
			for (int i = 0; i < toSize; i++) {
				String name = toFiles[i].getName();
				boolean isDir = toFiles[i].isDirectory();
				boolean found = false;
				for (int j = 0; j < fromSize; j++) {
					if (name.equals(resources[j].getName()) && isDir == resources[j] instanceof IModuleFolder)
						found = true;
				}
				
				// delete file if it can't be found or isn't the correct type
				if (!found) {
					if (isDir) {
						IStatus[] stat = deleteDirectory(toFiles[i], null);
						addArrayToList(status, stat);
					} else {
						if (!toFiles[i].delete())
							status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, toFiles[i].getAbsolutePath()), null));
					}
				}
				if (monitor.isCanceled())
					return new IStatus[] { Status.CANCEL_STATUS };
			}
		} else {
			if (toDir.isFile()) {
				if (!toDir.delete()) {
					status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, toDir.getAbsolutePath()), null));
					IStatus[] stat = new IStatus[status.size()];
					status.toArray(stat);
					return stat;
				}
			}
			if (!toDir.mkdir()) {
				status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorMkdir, toDir.getAbsolutePath()), null));
				IStatus[] stat = new IStatus[status.size()];
				status.toArray(stat);
				return stat;
			}
		}
		
		monitor.worked(50);
		
		// cycle through files and only copy when it doesn't exist
		// or is newer
		toFiles = toDir.listFiles();
		int toSize = 0;
		if (toFiles != null)
			toSize = toFiles.length;
		int dw = 0;
		if (toSize > 0)
			dw = 500 / toSize;
		
		for (int i = 0; i < fromSize; i++) {
			IModuleResource current = resources[i];
			String name = current.getName();
			boolean currentIsDir = current instanceof IModuleFolder;
			IPath toPath = path.append(name);
			
			if (!currentIsDir) {
				// check if this is a new or newer file
				boolean copy = true;
				IModuleFile mf = (IModuleFile) current;
				
				long mod = -1;
				IFile file = (IFile) mf.getAdapter(IFile.class);
				if (file != null) {
					mod = file.getLocalTimeStamp();
				} else {
					File file2 = (File) mf.getAdapter(File.class);
					mod = file2.lastModified();
				}
				
				for (int j = 0; j < toSize; j++) {
					if (name.equals(toFiles[j].getName()) && mod == toFiles[j].lastModified())
						copy = false;
				}
				
				if (copy) {
					try {
						copyFile(mf, toPath);
					} catch (CoreException ce) {
						status.add(ce.getStatus());
					}
				}
				monitor.worked(dw);
			} else { //if (currentIsDir) {
				IModuleFolder folder = (IModuleFolder) current;
				IModuleResource[] children = folder.members();
				monitor.subTask(NLS.bind(Messages.copyingTask, new String[] {resources[i].getName(), current.getName()}));
				IStatus[] stat = publishSmart(children, toPath, ProgressUtil.getSubMonitorFor(monitor, dw));
				addArrayToList(status, stat);
			}
			if (monitor.isCanceled())
				return new IStatus[] { Status.CANCEL_STATUS };
		}
		monitor.worked(500 - dw * toSize);
		monitor.done();
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public static IStatus[] publishDelta(IModuleResourceDelta delta, IPath path, IProgressMonitor monitor) {
		List status = new ArrayList();
		
		IModuleResource resource = delta.getModuleResource();
		int kind2 = delta.getKind();
		
		if (resource instanceof IModuleFile) {
			IModuleFile file = (IModuleFile) resource;
			try {
				if (kind2 == IModuleResourceDelta.REMOVED)
					deleteFile2(path, file);
				else {
					IPath path2 = path.append(file.getModuleRelativePath()).append(file.getName());
					File f = path2.toFile().getParentFile();
					if (!f.exists())
						f.mkdirs();
					
					copyFile(file, path2);
				}
			} catch (CoreException ce) {
				status.add(ce.getStatus());
			}
			IStatus[] stat = new IStatus[status.size()];
			status.toArray(stat);
			return stat;
		}
		
		if (kind2 == IModuleResourceDelta.ADDED) {
			IPath path2 = path.append(resource.getModuleRelativePath()).append(resource.getName());
			File file = path2.toFile();
			if (!file.exists() && !file.mkdirs()) {
				status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorMkdir, path2), null));
				IStatus[] stat = new IStatus[status.size()];
				status.toArray(stat);
				return stat;
			}
		}
		
		IModuleResourceDelta[] childDeltas = delta.getAffectedChildren();
		int size = childDeltas.length;
		for (int i = 0; i < size; i++) {
			IStatus[] stat = publishDelta(childDeltas[i], path, monitor);
			addArrayToList(status, stat);
		}
		
		if (kind2 == IModuleResourceDelta.REMOVED) {
			IPath path2 = path.append(resource.getModuleRelativePath()).append(resource.getName());
			File file = path2.toFile();
			if (file.exists() && !file.delete()) {
				status.add(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, path2), null));
			}
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	private static void deleteFile2(IPath path, IModuleFile file) throws CoreException {
		Trace.trace(Trace.PUBLISHING, "Deleting: " + file.getName() + " from " + path.toString());
		IPath path2 = path.append(file.getModuleRelativePath()).append(file.getName());
		if (path2.toFile().exists() && !path2.toFile().delete())
			throw new CoreException(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, path2), null));
	}

	private static void copyFile(IModuleFile mf, IPath path) throws CoreException {
		Trace.trace(Trace.PUBLISHING, "Copying: " + mf.getName() + " to " + path.toString());
		
		IFile file = (IFile) mf.getAdapter(IFile.class);
		if (file != null)
			copyFile(file.getContents(), path, file.getLocalTimeStamp(), mf);
		else {
			File file2 = (File) mf.getAdapter(File.class);
			InputStream in = null;
			try {
				in = new FileInputStream(file2);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorReading, file2.getAbsolutePath()), e));
			}
			copyFile(in, path, file2.lastModified(), mf);
		}
	}

	/**
	 * Publish the given module resources to the given path.
	 * 
	 * @param resources an array of module resources
	 * @param path a path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public static IStatus[] publishFull(IModuleResource[] resources, IPath path, IProgressMonitor monitor) {
		if (resources == null)
			return new IStatus[0];
		
		List status = new ArrayList();
		int size = resources.length;
		for (int i = 0; i < size; i++) {
			IStatus[] stat = copy(resources[i], path, monitor);
			addArrayToList(status, stat);
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	private static IStatus[] copy(IModuleResource resource, IPath path, IProgressMonitor monitor) {
		Trace.trace(Trace.PUBLISHING, "Copying: " + resource.getName() + " to " + path.toString());
		List status = new ArrayList();
		if (resource instanceof IModuleFolder) {
			IModuleFolder folder = (IModuleFolder) resource;
			IStatus[] stat = publishFull(folder.members(), path, monitor);
			addArrayToList(status, stat);
		} else {
			IModuleFile mf = (IModuleFile) resource;
			path = path.append(mf.getModuleRelativePath()).append(mf.getName());
			File f = path.toFile().getParentFile();
			if (!f.exists())
				f.mkdirs();
			try {
				copyFile(mf, path);
			} catch (CoreException ce) {
				status.add(ce.getStatus());
			}
		}
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}

	/**
	 * Creates a new zip file containing the given module resources. Deletes the existing file
	 * (and doesn't create a new one) if resources is null or empty.
	 * 
	 * @param resources an array of module resources
	 * @param path the path where the zip file should be created 
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 */
	public static IStatus[] publishZip(IModuleResource[] resources, IPath path, IProgressMonitor monitor) {
		if (resources == null || resources.length == 0) {
			// should also check if resources consists of all empty directories
			File file = path.toFile();
			if (file.exists())
				file.delete();
			return new IStatus[0];
		}
		
		File tempFile = null;
		try {
			File file = path.toFile();
			File tempDir = JavaServerPlugin.getInstance().getStateLocation().toFile();
			tempFile = File.createTempFile("tmp", "." + path.getFileExtension(), tempDir);
			
			BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(tempFile));
			ZipOutputStream zout = new ZipOutputStream(bout);
			addZipEntries(zout, resources);
			zout.close();
			
			moveTempFile(tempFile, file);
		} catch (CoreException e) {
			return new IStatus[] { e.getStatus() };
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error zipping", e);
			return new Status[] { new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCreatingZipFile, path.lastSegment(), e.getLocalizedMessage()), e) };
		} finally {
			if (tempFile != null && tempFile.exists())
				tempFile.deleteOnExit();
		}
		return new IStatus[0];
	}

	private static void addZipEntries(ZipOutputStream zout, IModuleResource[] resources) throws Exception {
		int size = resources.length;
		for (int i = 0; i < size; i++) {
			if (resources[i] instanceof IModuleFolder) {
				IModuleFolder mf = (IModuleFolder) resources[i];
				IModuleResource[] res = mf.members();
				
				IPath path = mf.getModuleRelativePath().append(mf.getName());
				String entryPath = path.toPortableString();
				if (!entryPath.endsWith("/"))
					entryPath += '/';
				
				ZipEntry ze = new ZipEntry(entryPath);
				
				long ts = 0;
				IFolder folder = (IFolder) mf.getAdapter(IFolder.class);
				if (folder != null)
					ts = folder.getLocalTimeStamp();
				
				if (ts != IResource.NULL_STAMP && ts != 0)
					ze.setTime(ts);
				
				zout.putNextEntry(ze);
				zout.closeEntry();
				
				addZipEntries(zout, res);
				continue;
			}
			
			IModuleFile mf = (IModuleFile) resources[i];
			IPath path = mf.getModuleRelativePath().append(mf.getName());
			
			ZipEntry ze = new ZipEntry(path.toPortableString());
			
			InputStream in = null;
			long ts = 0;
			IFile file = (IFile) mf.getAdapter(IFile.class);
			if (file != null) {
				ts = file.getLocalTimeStamp();
				in = file.getContents();
			} else {
				File file2 = (File) mf.getAdapter(File.class);
				ts = file2.lastModified();
				in = new FileInputStream(file2);
			}
			
			if (ts != IResource.NULL_STAMP && ts != 0)
				ze.setTime(ts);
			
			zout.putNextEntry(ze);
			
			try {
				int n = 0;
				while (n > -1) {
					n = in.read(buf);
					if (n > 0)
						zout.write(buf, 0, n);
				}
			} finally {
				in.close();
			}
			
			zout.closeEntry();
		}
	}

	/**
	 * Utility method to move a temp file into position by deleting the original and
	 * swapping in a new copy.
	 *  
	 * @param tempFile
	 * @param file
	 * @throws CoreException
	 */
	private static void moveTempFile(File tempFile, File file) throws CoreException {
		if (file.exists()) {
			if (!safeDelete(file, 2)) {
				// attempt to rewrite an existing file with the tempFile contents if
				// the existing file can't be deleted to permit the move
				try {
					InputStream in = new FileInputStream(tempFile);
					IStatus status = copyFile(in, file.getPath());
					if (!status.isOK()) {
						MultiStatus status2 = new MultiStatus(JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, file.toString()), null);
						status2.add(status);
						throw new CoreException(status2);
					}
					return;
				} catch (FileNotFoundException e) {
					// shouldn't occur
				} finally {
					tempFile.delete();
				}
				/*if (!safeDelete(file, 8)) {
					tempFile.delete();
					throw new CoreException(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDeleting, file.toString()), null));
				}*/
			}
		}
		if (!safeRename(tempFile, file, 10))
			throw new CoreException(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorRename, tempFile.toString()), null));
	}

	/**
	 * Safe delete. Tries to delete multiple times before giving up.
	 * 
	 * @param f
	 * @return <code>true</code> if it succeeds, <code>false</code> otherwise
	 */
	private static boolean safeDelete(File f, int retrys) {
		int count = 0;
		while (count < retrys) {
			if (!f.exists())
				return true;
			
			f.delete();
			
			if (!f.exists())
				return true;
			
			count++;
			// delay if we are going to try again
			if (count < retrys) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return false;
	}

	/**
	 * Safe rename. Will try multiple times before giving up.
	 * 
	 * @param from
	 * @param to
	 * @param retrys number of times to retry
	 * @return <code>true</code> if it succeeds, <code>false</code> otherwise
	 */
	private static boolean safeRename(File from, File to, int retrys) {
		if (!from.exists())
			return false;
		
		int count = 0;
		while (count < retrys) {
			if (from.renameTo(to))
				return true;
			
			count++;
			// delay if we are going to try again
			if (count < retrys) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return false;
	}

	private static void addArrayToList(List list, IStatus[] a) {
		if (list == null || a == null || a.length == 0)
			return;
		
		int size = a.length;
		for (int i = 0; i < size; i++)
			list.add(a[i]);
	}
}