package org.erlide.devtools.builder;

import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class JavaErlangBridgeBuilder extends IncrementalProjectBuilder {

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				buildFile(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				buildFile(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			buildFile(resource);
			// return true to continue visiting children.
			return true;
		}
	}

	class XMLErrorHandler extends DefaultHandler {

		private final IFile file;

		public XMLErrorHandler(IFile file) {
			this.file = file;
		}

		private void addMarker(SAXParseException e, int severity) {
			JavaErlangBridgeBuilder.this.addMarker(file, e.getMessage(), e
					.getLineNumber(), severity);
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_WARNING);
		}
	}

	public static final String BUILDER_ID = "org.erlide.devtools.jebridgeBuilder";

	private static final String MARKER_TYPE = "org.erlide.devtools.xmlProblem";

	private SAXParserFactory parserFactory;

	private void addMarker(IFile file, String message, int lineNumber,
			int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void buildFile(IResource resource) {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			System.out.println("**" + resource.getProjectRelativePath());

			deleteMarkers(file);
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
