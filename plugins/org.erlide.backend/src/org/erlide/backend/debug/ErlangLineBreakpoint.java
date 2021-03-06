/*******************************************************************************
 * Copyright (c) 2004 Vlad Dumitrescu and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.backend.debug;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.erlide.backend.api.IBackend;
import org.erlide.backend.debug.model.ErlangDebugTarget;
import org.erlide.engine.ErlangEngine;
import org.erlide.engine.model.ErlModelException;
import org.erlide.engine.model.IErlElement;
import org.erlide.engine.model.erlang.IErlFunctionClause;
import org.erlide.engine.model.root.IErlModule;
import org.erlide.util.ErlLogger;

public class ErlangLineBreakpoint extends LineBreakpoint implements IErlangBreakpoint {

    public static final String ERLANG_LINE_BREAKPOINT_MARKER_TYPE = "org.erlide.core.erlang.lineBreakpoint.marker";

    private ErlangDebugTarget target;
    private String clauseHead;
    private int fHitCount;
    private int fBreakAction = IErlangBreakpoint.BREAK_ACTION_BREAK;

    public ErlangLineBreakpoint() {
    }

    @Override
    public String getModelIdentifier() {
        return ErlDebugConstants.ID_ERLANG_DEBUG_MODEL;
    }

    public void createMarker(final IResource resource, final int lineNumber)
            throws CoreException {
        final IWorkspaceRunnable runnable = monitor -> {
            final IMarker marker = DebugMarkerUtils.createErlangLineBreakpointMarker(
                    resource, lineNumber, getModelIdentifier());
            setMarker(marker);
            resetClauseHead(lineNumber - 1, resource);
        };
        run(getMarkerRule(resource), runnable);
    }

    protected void resetClauseHead(final int lineNumber, final IResource resource) {
        clauseHead = "";
        if (resource instanceof IFile) {
            final IFile file = (IFile) resource;
            final IErlModule m = ErlangEngine.getInstance().getModel().findModule(file);
            if (m != null) {
                try {
                    m.open(null);
                    final IErlElement e = m.getElementAtLine(lineNumber);
                    if (e instanceof IErlFunctionClause) {
                        final IErlFunctionClause clause = (IErlFunctionClause) e;
                        clauseHead = clause.getName() + clause.getHead();
                    }
                } catch (final ErlModelException e1) {
                    ErlLogger.warn(e1);
                }
            }
        }
    }

    /**
     * Installs this breakpoint
     *
     * @param theTarget
     *            debug target
     */
    public void install(final ErlangDebugTarget theTarget) {
        target = theTarget;
        if (theTarget != null) {
            createRequest(ErlDebugConstants.REQUEST_INSTALL);
        }
    }

    private void createRequest(final int request) {
        final IBackend b = target.getBackend();
        int line = -1;
        try {
            line = getLineNumber();
        } catch (final CoreException e) {
            ErlLogger.warn(e);
        }
        final IResource r = getMarker().getResource();
        final String module = r.getLocation().toPortableString();
        if (line != -1) {
            ErlideDebug.addDeleteLineBreakpoint(b, module, line, request);
        }
    }

    public String getModule() {
        final IResource r = getMarker().getResource();
        return r.getFullPath().removeFileExtension().lastSegment();
    }

    public void remove(final ErlangDebugTarget theTarget) {
        target = theTarget;
        if (theTarget != null) {
            createRequest(ErlDebugConstants.REQUEST_REMOVE);
        }
    }

    public String getClauseHead() {
        if (clauseHead == null) {
            try {
                resetClauseHead(getLineNumber() - 1, getMarker().getResource());
            } catch (final CoreException e) {
            }
        }
        return clauseHead;
    }

    @Override
    public void setMarker(final IMarker marker) throws CoreException {
        super.setMarker(marker);
        clauseHead = null;
    }

    @Override
    public String getCondition() throws CoreException {
        return null;
    }

    @Override
    public boolean isConditionEnabled() throws CoreException {
        return false;
    }

    @Override
    public void setCondition(final String condition) throws CoreException {
    }

    @Override
    public void setConditionEnabled(final boolean enabled) throws CoreException {
    }

    @Override
    public boolean supportsCondition() {
        return false;
    }

    @Override
    public void setHitCount(final int hitCount) {
        fHitCount = hitCount;
    }

    @Override
    public int getHitCount() {
        return fHitCount;
    }

    @Override
    public int getBreakAction() {
        return fBreakAction;
    }

    @Override
    public void setBreakAction(final int traceAction) {
        fBreakAction = traceAction;
    }

    public ErlangDebugTarget getTarget() {
        return target;
    }

    @Override
    public String getMessage() {
        final IMarker marker = getMarker();
        return "Line Breakpoint: " + marker.getResource().getName() + " [line: "
                + marker.getAttribute(IMarker.LINE_NUMBER, -1) + "]";
    }

}
