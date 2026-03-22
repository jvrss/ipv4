package br.com.jvrss.view;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class TrayView {

    private JPopupMenu popupMenu;
    private Image appIcon;

    // Menu items exposed so the controller can attach listeners
    public final JMenuItem verIpv4MenuItem      = new JMenuItem("Ver IPv4 Público");
    public final JMenuItem verIpv6MenuItem      = new JMenuItem("Ver IPv6 Público");
    public final JMenuItem verIpVPSMenuItem     = new JMenuItem("Ver IP VPS");
    public final JMenuItem verLocalIpv4MenuItem = new JMenuItem("Ver IP Local (IPv4)");
    public final JMenuItem verLocalIpv6MenuItem = new JMenuItem("Ver IP Local (IPv6)");
    public final JMenuItem exitMenuItem         = new JMenuItem("Sair");

    /**
     * Builds the system-tray icon and popup menu.
     * Must be called on the EDT.
     *
     * @throws Exception if the tray icon cannot be added
     */
    public void initialize() throws Exception {
        // Load icon
        Image image = Toolkit.getDefaultToolkit().createImage(
                TrayView.class.getResource("/icon.png")
        );
        if (image == null) {
            JOptionPane.showMessageDialog(null, "Ícone não encontrado. Verifique o caminho.");
            return;
        }
        appIcon = image;

        // Build popup menu
        popupMenu = new JPopupMenu();
        popupMenu.add(verIpv4MenuItem);
        popupMenu.add(verIpv6MenuItem);
        popupMenu.add(verIpVPSMenuItem);
        popupMenu.add(verLocalIpv4MenuItem);
        popupMenu.add(verLocalIpv6MenuItem);
        popupMenu.addSeparator();
        popupMenu.add(exitMenuItem);

        // Configure tray icon
        TrayIcon trayIcon = new TrayIcon(image, "IP Público");
        trayIcon.setImageAutoSize(true);

        // Left-click / double-click also opens the menu
        trayIcon.addActionListener(e -> SwingUtilities.invokeLater(this::showPopupAtPointer));

        SystemTray.getSystemTray().add(trayIcon);
    }

    // -------------------------------------------------------------------------
    // Dialog helpers
    // -------------------------------------------------------------------------

    public void showSingleIpDialog(String title, String ip) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        if (appIcon != null) dialog.setIconImage(appIcon);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(360, 120);
        dialog.setLocationRelativeTo(null);

        JLabel label = new JLabel(ip, SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JButton copyBtn = new JButton("Copiar");
        copyBtn.addActionListener(e -> {
            copyToClipboard(ip);
            dialog.dispose();
            JOptionPane.showMessageDialog(null, "IP copiado para a área de transferência!");
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(copyBtn);

        dialog.add(label, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void showLocalIpDialog(String title, List<String> ips) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        if (appIcon != null) dialog.setIconImage(appIcon);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(null);

        if (ips.isEmpty()) {
            dialog.setSize(320, 100);
            dialog.add(new JLabel("Nenhum IP local encontrado.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            JPanel listPanel = new JPanel(new GridLayout(ips.size(), 2, 8, 6));
            listPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            for (String ip : ips) {
                JLabel ipLabel = new JLabel(ip);
                JButton copyBtn = new JButton("Copiar");
                copyBtn.addActionListener(e -> {
                    copyToClipboard(ip);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(null, ip + "\ncopiado para a área de transferência!");
                });
                listPanel.add(ipLabel);
                listPanel.add(copyBtn);
            }

            dialog.add(new JScrollPane(listPanel), BorderLayout.CENTER);
            dialog.pack();
        }

        dialog.setVisible(true);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void showPopupAtPointer() {
        Point location = MouseInfo.getPointerInfo().getLocation();
        popupMenu.setLocation(location.x, location.y);
        popupMenu.setInvoker(popupMenu);
        popupMenu.setVisible(true);
    }

    private void copyToClipboard(String text) {
        if (text == null || text.isBlank()) {
            JOptionPane.showMessageDialog(null, "Nenhum IP disponível para copiar.");
            return;
        }
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
}



