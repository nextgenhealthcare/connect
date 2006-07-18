/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.applets.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class UndoManagerHelper {
	public static Action getUndoAction(UndoManager manager, String label) {
		return new UndoAction(manager, label);
	}

	public static Action getUndoAction(UndoManager manager) {
		return new UndoAction(manager, (String) UIManager.get("AbstractUndoableEdit.undoText"));
	}

	public static Action getRedoAction(UndoManager manager, String label) {
		return new RedoAction(manager, label);
	}

	public static Action getRedoAction(UndoManager manager) {
		return new RedoAction(manager, (String) UIManager.get("AbstractUndoableEdit.redoText"));
	}

	private abstract static class UndoRedoAction extends AbstractAction {
		UndoManager undoManager = new UndoManager();

		String errorMessage = "Cannot undo";

		String errorTitle = "Undo Problem";

		protected UndoRedoAction(UndoManager manager, String name) {
			super(name);
			undoManager = manager;
		}

		public void setErrorMessage(String newValue) {
			errorMessage = newValue;
		}

		public void setErrorTitle(String newValue) {
			errorTitle = newValue;
		}

		protected void showMessage(Object source) {
			if (source instanceof Component) {
				JOptionPane.showMessageDialog((Component) source, errorMessage, errorTitle, JOptionPane.WARNING_MESSAGE);
			} else {
				System.err.println(errorMessage);
			}
		}
	}

	public static class UndoAction extends UndoRedoAction {
		public UndoAction(UndoManager manager, String name) {
			super(manager, name);
			setErrorMessage("Cannot undo");
			setErrorTitle("Undo Problem");
		}

		public void actionPerformed(ActionEvent actionEvent) {
			try {
				undoManager.undo();
			} catch (CannotUndoException cannotUndoException) {
				showMessage(actionEvent.getSource());
			}
		}
	}

	public static class RedoAction extends UndoRedoAction {
		public RedoAction(UndoManager manager, String name) {
			super(manager, name);
			setErrorMessage("Cannot redo");
			setErrorTitle("Redo Problem");
		}

		public void actionPerformed(ActionEvent actionEvent) {
			try {
				undoManager.redo();
			} catch (CannotRedoException cannotRedoException) {
				showMessage(actionEvent.getSource());
			}
		}
	}
}
