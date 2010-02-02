package com.webreach.mirth.client.ui.panels.reference;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXTable;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;

/**
 * ReferenceTable This class provides some basic properties and drag & drop
 * support for a JXTable
 * 
 */
public class ReferenceTable extends JXTable {

    public class TableTransferHandler extends TransferHandler {

        int row = -1;

        protected String exportString(JComponent c) {
            JXTable table = (JXTable) c;
            row = table.getSelectedRow();

            if (row >= 0 && row < table.getRowCount()) {
                return table.getValueAt(row, 0).toString();
            } else {
                return "";
            }
        }

        protected Transferable createTransferable(JComponent c) {
            return new StringSelection(exportString(c));
        }

        protected void exportDone(JComponent c, Transferable data, int action) {
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }

    public ReferenceTable() {
        super();

        this.setTransferHandler(new TableTransferHandler());
        this.setDragEnabled(true);
        this.setFocusable(false);
        this.setOpaque(true);
        this.setRowSelectionAllowed(true);
        this.setSelectionMode(0);
        this.setRowHeight(UIConstants.ROW_HEIGHT);
        this.packTable(UIConstants.COL_MARGIN);
        this.setShowVerticalLines(false);
        this.setBorder(BorderFactory.createEmptyBorder());

        this.addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent evt) {
                refTableMouseDragged(evt);
            }

            public void mouseMoved(MouseEvent evt) {
                refTableMouseMoved(evt);
            }
        });

        this.addMouseListener(new MouseAdapter() {

            public void mouseExited(MouseEvent evt) {
                refTableMouseExited(evt);
            }
        });
    }

    private void refTableMouseExited(MouseEvent evt) {
        if (!(evt.getModifiersEx() == evt.BUTTON1_DOWN_MASK)) {
            this.clearSelection();
        }
    }

    private void refTableMouseDragged(MouseEvent evt) {
    }

    private void refTableMouseMoved(MouseEvent evt) {
        int row = this.rowAtPoint(evt.getPoint());
        int col = this.columnAtPoint(evt.getPoint());

        if (row >= 0 && row < this.getModel().getRowCount() && col >= 0 && col < this.getModel().getColumnCount()) {
            this.setRowSelectionInterval(row, row);
        }
    }
    protected Frame parent = PlatformUI.MIRTH_FRAME;
}
