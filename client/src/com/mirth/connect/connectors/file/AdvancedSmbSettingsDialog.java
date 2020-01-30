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
		setTitle("Method Settings");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new Dimension(200, 140));
		DisplayUtil.setResizable(this, false);
		setLayout(new MigLayout("insets 8 8 0 8, novisualpadding, hidemode 3"));
		getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
		
		this.schemeProperties = schemeProperties != null ? schemeProperties : new SmbSchemeProperties();

		initComponents();
		initLayout();
		updateSmbVersionComboBox();

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
		props.setSmbVersion(((SmbDialectVersion) smbVersionComboBox.getSelectedItem()).getVersion());
		return props;
	}

	public void updateSmbVersionComboBox() {
		smbVersionComboBox.setCanEnableSave(false);
		smbVersionComboBox.setSelectedItem(SmbSchemeProperties.getSmbDialectVersion(schemeProperties.getSmbVersion()));
		smbVersionComboBox.setCanEnableSave(true);
	}

	public boolean validateProperties() {
		return true;
	}

	private void initComponents() {
		smbVersionLabel = new JLabel("SMB Version:");
		smbVersionComboBox = new MirthComboBox<>();
		smbVersionComboBox.setModel(new DefaultComboBoxModel<SmbDialectVersion>(SmbSchemeProperties.getSupportedVersions()));
		smbVersionComboBox.setToolTipText("Select the SMB version to connect with.");
		
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				okCancelButtonActionPerformed();
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

		propertiesPanel.add(smbVersionLabel);
		propertiesPanel.add(smbVersionComboBox);

		add(propertiesPanel, "grow, push, top, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("insets 0 8 8 8, novisualpadding, hidemode 3, fill"));
		buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
		buttonPanel.add(new JSeparator(), "growx, sx, wrap");
		buttonPanel.add(okButton, "newline, w 50!, sx, right, split");
		buttonPanel.add(cancelButton, "w 50!");

		add(buttonPanel, "south, span");
	}

	private void okCancelButtonActionPerformed() {
		if (!validateProperties()) {
			return;
		}

		saved = true;
		PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
		dispose();
	}

	private JLabel smbVersionLabel;
	private MirthComboBox<SmbDialectVersion> smbVersionComboBox;

	private JButton okButton;
	private JButton cancelButton;
}
