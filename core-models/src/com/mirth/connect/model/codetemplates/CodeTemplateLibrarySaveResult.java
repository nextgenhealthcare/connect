/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("codeTemplateLibrarySaveResult")
public class CodeTemplateLibrarySaveResult implements Serializable {

    private boolean overrideNeeded;
    private boolean librariesSuccess;
    private Throwable librariesCause;
    private Map<String, LibraryUpdateResult> libraryResults = new HashMap<String, LibraryUpdateResult>();
    private Map<String, CodeTemplateUpdateResult> codeTemplateResults = new HashMap<String, CodeTemplateUpdateResult>();

    public CodeTemplateLibrarySaveResult() {
        this(false);
    }

    public CodeTemplateLibrarySaveResult(boolean overrideNeeded) {
        this.overrideNeeded = overrideNeeded;
    }

    public boolean isOverrideNeeded() {
        return overrideNeeded;
    }

    public void setOverrideNeeded(boolean overrideNeeded) {
        this.overrideNeeded = overrideNeeded;
    }

    public boolean isLibrariesSuccess() {
        return librariesSuccess;
    }

    public void setLibrariesSuccess(boolean librariesSuccess) {
        this.librariesSuccess = librariesSuccess;
    }

    public Throwable getLibrariesCause() {
        return librariesCause;
    }

    public void setLibrariesCause(Throwable librariesCause) {
        this.librariesCause = librariesCause;
    }

    public Map<String, LibraryUpdateResult> getLibraryResults() {
        return libraryResults;
    }

    public void setLibraryResults(Map<String, LibraryUpdateResult> libraryResults) {
        this.libraryResults = libraryResults;
    }

    public Map<String, CodeTemplateUpdateResult> getCodeTemplateResults() {
        return codeTemplateResults;
    }

    public void setCodeTemplateResults(Map<String, CodeTemplateUpdateResult> codeTemplateResults) {
        this.codeTemplateResults = codeTemplateResults;
    }

    public static class LibraryUpdateResult implements Serializable {
        private int newRevision;
        private Calendar newLastModified;

        public int getNewRevision() {
            return newRevision;
        }

        public void setNewRevision(int newRevision) {
            this.newRevision = newRevision;
        }

        public Calendar getNewLastModified() {
            return newLastModified;
        }

        public void setNewLastModified(Calendar newLastModified) {
            this.newLastModified = newLastModified;
        }
    }

    public static class CodeTemplateUpdateResult implements Serializable {
        private boolean success;
        private int newRevision;
        private Calendar newLastModified;
        private Throwable cause;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getNewRevision() {
            return newRevision;
        }

        public void setNewRevision(int newRevision) {
            this.newRevision = newRevision;
        }

        public Calendar getNewLastModified() {
            return newLastModified;
        }

        public void setNewLastModified(Calendar newLastModified) {
            this.newLastModified = newLastModified;
        }

        public Throwable getCause() {
            return cause;
        }

        public void setCause(Throwable cause) {
            this.cause = cause;
        }
    }
}