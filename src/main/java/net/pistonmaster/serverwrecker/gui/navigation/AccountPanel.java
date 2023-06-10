/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.gui.navigation;


import javafx.stage.FileChooser;
import net.pistonmaster.serverwrecker.ServerWrecker;
import net.pistonmaster.serverwrecker.auth.AccountRegistry;
import net.pistonmaster.serverwrecker.auth.AccountSettings;
import net.pistonmaster.serverwrecker.auth.AuthType;
import net.pistonmaster.serverwrecker.auth.JavaAccount;
import net.pistonmaster.serverwrecker.gui.libs.JEnumComboBox;
import net.pistonmaster.serverwrecker.gui.libs.JFXFileHelper;
import net.pistonmaster.serverwrecker.gui.libs.PresetJCheckBox;
import net.pistonmaster.serverwrecker.proxy.ProxyRegistry;
import net.pistonmaster.serverwrecker.proxy.ProxyType;
import net.pistonmaster.serverwrecker.proxy.SWProxy;
import net.pistonmaster.serverwrecker.settings.lib.SettingsDuplex;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountPanel extends NavigationItem implements SettingsDuplex<AccountSettings> {
    private final JTextField nameFormat;
    private final JCheckBox shuffleAccounts = new PresetJCheckBox(AccountSettings.DEFAULT_SHUFFLE_ACCOUNTS);

    @Inject
    public AccountPanel(ServerWrecker serverWrecker, JFrame parent) {
        serverWrecker.getSettingsManager().registerDuplex(AccountSettings.class, this);

        setLayout(new GridLayout(2, 1, 10, 10));

        JPanel accountOptionsPanel = new JPanel();
        accountOptionsPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JPanel addAccountPanel = new JPanel();
        addAccountPanel.setLayout(new GridLayout(1, 3, 10, 10));

        addAccountPanel.add(createAccountLoadButton(serverWrecker, parent, AuthType.OFFLINE));
        addAccountPanel.add(createAccountLoadButton(serverWrecker, parent, AuthType.MICROSOFT));
        addAccountPanel.add(createAccountLoadButton(serverWrecker, parent, AuthType.THE_ALTENING));

        accountOptionsPanel.add(addAccountPanel);

        JPanel accountSettingsPanel = new JPanel();
        accountSettingsPanel.setLayout(new GridLayout(0, 2));

        accountSettingsPanel.add(new JLabel("Shuffle accounts: "));
        accountSettingsPanel.add(shuffleAccounts);

        accountSettingsPanel.add(new JLabel("Name Format: "));
        nameFormat = new JTextField(AccountSettings.DEFAULT_NAME_FORMAT);
        accountSettingsPanel.add(nameFormat);

        accountOptionsPanel.add(accountSettingsPanel);

        add(accountOptionsPanel);

        JPanel accountListPanel = new JPanel();
        accountListPanel.setLayout(new GridLayout(1, 1));

        String[] columnNames = {"Username", "UUID", "Auth Token", "Token Expiry", "Type", "Enabled"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            final Class<?>[] columnTypes = new Class<?>[] {
                    String.class, UUID.class, String.class, Long.class, AuthType.class, Boolean.class
            };

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        };

        JTable accountList = new JTable(model);

        serverWrecker.getAccountRegistry().addLoadHook(() -> {
            model.getDataVector().removeAllElements();

            AccountRegistry registry = serverWrecker.getAccountRegistry();
            List<JavaAccount> accounts = registry.getAccounts();
            int registrySize = accounts.size();
            Object[][] dataVector = new Object[registrySize][];
            for (int i = 0; i < registrySize; i++) {
                JavaAccount account = accounts.get(i);

                dataVector[i] = new Object[]{
                        account.username(),
                        account.profileId(),
                        account.authToken(),
                        account.tokenExpireAt(),
                        account.authType(),
                        account.enabled()
                };
            }

            model.setDataVector(dataVector, columnNames);

            accountList.getColumnModel().getColumn(4)
                    .setCellEditor(new DefaultCellEditor(new JEnumComboBox<>(AuthType.class)));

            model.fireTableDataChanged();
        });

        accountList.addPropertyChangeListener(evt -> {
            if ("tableCellEditor".equals(evt.getPropertyName()) && !accountList.isEditing()) {
                List<JavaAccount> accounts = new ArrayList<>();

                for (int i = 0; i < accountList.getRowCount(); i++) {
                    Object[] row = new Object[accountList.getColumnCount()];
                    for (int j = 0; j < accountList.getColumnCount(); j++) {
                        row[j] = accountList.getValueAt(i, j);
                    }

                    String username = (String) row[0];
                    UUID profileId = (UUID) row[1];
                    String authToken = (String) row[2];
                    long tokenExpireAt = (long) row[3];
                    AuthType authType = (AuthType) row[4];
                    boolean enabled = (boolean) row[5];

                    accounts.add(new JavaAccount(authType, username, profileId, authToken, tokenExpireAt, enabled));
                }

                serverWrecker.getAccountRegistry().setAccounts(accounts);
            }
        });

        JScrollPane scrollPane = new JScrollPane(accountList);

        accountListPanel.add(scrollPane);

        add(accountListPanel);
    }

    private JButton createAccountLoadButton(ServerWrecker serverWrecker, JFrame parent, AuthType type) {
        String loadText = String.format("Load %s accounts", type);
        String typeText = String.format("%s list file", type);
        JButton button = new JButton(loadText);

        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(Path.of(System.getProperty("user.dir")).toFile());
        chooser.setTitle(loadText);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(typeText, "*.txt"));

        button.addActionListener(new LoadAccountsListener(serverWrecker, parent, chooser, type));
        return button;
    }

    @Override
    public String getNavigationName() {
        return "Accounts";
    }

    @Override
    public String getNavigationId() {
        return "account-menu";
    }

    @Override
    public void onSettingsChange(AccountSettings settings) {
        nameFormat.setText(settings.nameFormat());
        shuffleAccounts.setSelected(settings.shuffleAccounts());
    }

    @Override
    public AccountSettings collectSettings() {
        return new AccountSettings(
                nameFormat.getText(),
                shuffleAccounts.isSelected()
        );
    }

    private record LoadAccountsListener(ServerWrecker serverWrecker, JFrame frame,
                                        FileChooser chooser, AuthType authType) implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Path accountFile = JFXFileHelper.showOpenDialog(chooser);
            if (accountFile == null) {
                return;
            }

            serverWrecker.getLogger().info("Opening: {}", accountFile.getFileName());

            serverWrecker.getThreadPool().submit(() -> {
                try {
                    serverWrecker.getAccountRegistry().loadFromFile(accountFile, authType);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
