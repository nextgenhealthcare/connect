package com.mirth.connect.connectors.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.util.DisplayUtil;

import net.miginfocom.swing.MigLayout;

public class AdvancedSmbSettingsDialog extends AdvancedSettingsDialog {
	private boolean saved;
	private SmbSchemeProperties schemeProperties;

	public AdvancedSmbSettingsDialog(SmbSchemeProperties schemeProperties) {
		setTitle("SMB Settings");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new Dimension(250, 170));
		DisplayUtil.setResizable(this, false);
		setLayout(new MigLayout("insets 8 8 0 8, novisualpadding, hidemode 3"));
		getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
		
		this.schemeProperties = schemeProperties != null ? schemeProperties : new SmbSchemeProperties();

		initComponents();
		initLayout();
		initSmbVersionComboBoxes();

		setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
		setVisible(true);
	}

	@Override
	public boolean wasSaved() {
		return saved;
	}

	@Override
	public SchemeProperties getSchemeProperties() {
		SmbSchemeProperties props = new SmbSchemeProperties();
		props.setSmbMinVersion(((SmbDialectVersion) smbMinVersionComboBox.getSelectedItem()).getVersion());
		props.setSmbMaxVersion(((SmbDialectVersion) smbMaxVersionComboBox.getSelectedItem()).getVersion());
		return props;
	}

	public void initSmbVersionComboBoxes() {
		smbMinVersionComboBox.setCanEnableSave(false);
		smbMinVersionComboBox.setSelectedItem(SmbSchemeProperties.getSmbDialectVersion(schemeProperties.getSmbMinVersion()));
		smbMinVersionComboBox.setCanEnableSave(true);
		
		smbMaxVersionComboBox.setCanEnableSave(false);
		smbMaxVersionComboBox.setSelectedItem(SmbSchemeProperties.getSmbDialectVersion(schemeProperties.getSmbMaxVersion()));
		smbMaxVersionComboBox.setCanEnableSave(true);
	}

	public boolean validateProperties() {
		boolean valid = true;
		SmbDialectVersion minVersion = (SmbDialectVersion) smbMinVersionComboBox.getSelectedItem();
		SmbDialectVersion maxVersion = (SmbDialectVersion) smbMaxVersionComboBox.getSelectedItem();
		
		if (minVersion.getVersion().compareTo(maxVersion.getVersion()) > 0) {
			smbMinVersionComboBox.setBackground(UIConstants.INVALID_COLOR);
			smbMaxVersionComboBox.setBackground(UIConstants.INVALID_COLOR);
			valid = false;
		} else {
			smbMinVersionComboBox.setBackground(null);
			smbMaxVersionComboBox.setBackground(null);
		}
		return valid;
	}

	private void initComponents() {
		smbMinVersionLabel = new JLabel("SMB Minimum Version:");
		smbMinVersionComboBox = new MirthComboBox<>();
		smbMinVersionComboBox.setModel(new DefaultComboBoxModel<SmbDialectVersion>(SmbSchemeProperties.getSupportedVersions()));
		smbMinVersionComboBox.setToolTipText("Select the minimum SMB version to connect with.");
		
		smbMaxVersionLabel = new JLabel("SMB Maximum Version:");
		smbMaxVersionComboBox = new MirthComboBox<>();
		smbMaxVersionComboBox.setModel(new DefaultComboBoxModel<SmbDialectVersion>(SmbSchemeProperties.getSupportedVersions()));
		smbMaxVersionComboBox.setToolTipText("Select the maximum SMB version to connect with.");
		
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				okButtonActionPerformed();
			}
		});

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
	}

	private void initLayout() {
		JPanel propertiesPanel = new JPanel(
				new MigLayout("insets 12, novisualpadding, hidemode 3, fillx", "[right][left]"));
		propertiesPanel.setBackground(UIConstants.BACKGROUND_COLOR);
		propertiesPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "SMB Settings",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

		propertiesPanel.add(smbMinVersionLabel);
		propertiesPanel.add(smbMinVersionComboBox);
		
		propertiesPanel.add(smbMaxVersionLabel, "newline");
		propertiesPanel.add(smbMaxVersionComboBox);

		add(propertiesPanel, "grow, push, top, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("insets 0 8 8 8, novisualpadding, hidemode 3, fill"));
		buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
		buttonPanel.add(new JSeparator(), "growx, sx, wrap");
		buttonPanel.add(okButton, "newline, w 50!, sx, right, split");
		buttonPanel.add(cancelButton, "w 50!");

		add(buttonPanel, "south, span");
	}

	private void okButtonActionPerformed() {
		if (!validateProperties()) {
			return;
		}
		
		if (((SmbDialectVersion) smbMinVersionComboBox.getSelectedItem()).getVersion().equals("SMB1") || ((SmbDialectVersion) smbMaxVersionComboBox.getSelectedItem()).getVersion().equals("SMB1")) {
            if (JOptionPane.showConfirmDialog(this, "SMB v1 is outdated and may pose a security risk. Do you wish to proceed?", "Outdated SMB Version", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 1) {
                return;
            }
        }

		saved = true;
		PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
		dispose();
	}

	private JLabel smbMinVersionLabel;
	private MirthComboBox<SmbDialectVersion> smbMinVersionComboBox;
	private JLabel smbMaxVersionLabel;
	private MirthComboBox<SmbDialectVersion> smbMaxVersionComboBox;

	private JButton okButton;
	private JButton cancelButton;
}
