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
package net.pistonmaster.serverwrecker.gui.libs;

import lombok.Setter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Modified version of: <a href="https://github.com/SKCraft/Launcher/blob/master/launcher/src/main/java/com/skcraft/launcher/swing/MessageLog.java">SKCraft/Launcher Log Panel</a>
 */
public class MessageLogPanel extends JPanel {
    private final SimpleAttributeSet defaultAttributes = new SimpleAttributeSet();
    private final int numLines;
    private JTextArea textComponent;
    private Document document;
    private final NoopDocumentFilter noopDocumentFilter = new NoopDocumentFilter();

    public MessageLogPanel(int numLines) {
        this.numLines = numLines;

        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        this.textComponent = new JTextArea();

        textComponent.setLineWrap(true);
        textComponent.setWrapStyleWord(true);

        textComponent.setFont(new JLabel().getFont());
        textComponent.setEditable(true);
        DefaultCaret caret = (DefaultCaret) textComponent.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        document = textComponent.getDocument();
        document.addDocumentListener(new LimitLinesDocumentListener(numLines, true));
        ((AbstractDocument) document).setDocumentFilter(noopDocumentFilter);

        updatePopup();

        textComponent.addCaretListener(e -> updatePopup());

        JScrollPane scrollText = new JScrollPane();
        scrollText.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        scrollText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollText.setViewportView(textComponent);

        new SmartScroller(scrollText);

        add(scrollText, BorderLayout.CENTER);
    }

    private void updatePopup() {
        JPopupMenu popupMenu = new JPopupMenu();
        if (textComponent.getSelectedText() != null) {
            JMenuItem copyItem = new JMenuItem("Copy");
            copyItem.addActionListener(e -> textComponent.copy());
            popupMenu.add(copyItem);

            JMenuItem cutItem = new JMenuItem("Upload to pastes.dev");
            cutItem.addActionListener(e -> {
                try {
                    String url = "https://pastes.dev/" + PastesDevService.upload(textComponent.getSelectedText());
                    JOptionPane.showMessageDialog(this,
                            SwingTextUtils.createPane("Uploaded to: <a href='" + url + "'>" + url + "</a>"),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to upload!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            popupMenu.add(cutItem);
        }

        // Add divider
        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }

        JMenuItem clearItem = new JMenuItem("Clear");
        clearItem.addActionListener(e -> clear());
        popupMenu.add(clearItem);
        textComponent.setComponentPopupMenu(popupMenu);
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> {
            noopDocumentFilter.setFilter(false);
            textComponent.setText("");
            noopDocumentFilter.setFilter(true);
        });
    }

    /**
     * Log a message given the {@link javax.swing.text.AttributeSet}.
     *
     * @param line line
     */
    public void log(final String line) {
        SwingUtilities.invokeLater(() -> {
            try {
                noopDocumentFilter.setFilter(false);
                int offset = document.getLength();
                document.insertString(offset, line, defaultAttributes);
                noopDocumentFilter.setFilter(true);
            } catch (BadLocationException ignored) {
            }
        });
    }

    @Setter
    private static class NoopDocumentFilter extends DocumentFilter {
        private boolean filter = true;

        @Override
        public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
            if (filter) {
                return;
            }

            super.remove(fb, offset, length);
        }

        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (filter) {
                return;
            }

            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (filter) {
                return;
            }

            super.replace(fb, offset, length, text, attrs);
        }
    }
}
