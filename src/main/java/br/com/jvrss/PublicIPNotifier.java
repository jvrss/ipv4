package br.com.jvrss;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class PublicIPNotifier {
    public static void main(String[] args) {
        // Verificar se o SystemTray é suportado
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null, "SystemTray não é suportado neste sistema.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Carregar o ícone
                Image image = Toolkit.getDefaultToolkit().createImage(
                        PublicIPNotifier.class.getResource("/icon.png")
                );
                if (image == null) {
                    JOptionPane.showMessageDialog(null, "Ícone não encontrado. Verifique o caminho.");
                    return;
                }

                // Criar o menu de popup do Swing
                JPopupMenu popupMenu = new JPopupMenu();

                JMenuItem verIpv4MenuItem        = new JMenuItem("Ver IPv4 Público");
                JMenuItem verIpv6MenuItem        = new JMenuItem("Ver IPv6 Público");
                JMenuItem verIpVPSMenuItem       = new JMenuItem("Ver IP VPS");
                JMenuItem verLocalIpv4MenuItem   = new JMenuItem("Ver IP Local (IPv4)");
                JMenuItem verLocalIpv6MenuItem   = new JMenuItem("Ver IP Local (IPv6)");
                JMenuItem exitMenuItem           = new JMenuItem("Sair");

                popupMenu.add(verIpv4MenuItem);
                popupMenu.add(verIpv6MenuItem);
                popupMenu.add(verIpVPSMenuItem);
                popupMenu.add(verLocalIpv4MenuItem);
                popupMenu.add(verLocalIpv6MenuItem);
                popupMenu.addSeparator();
                popupMenu.add(exitMenuItem);

                // Ações dos botões
                verIpv4MenuItem.addActionListener(e -> {
                    String ip = getIPAddress("https://api.ipify.org", false);
                    showSingleIpDialog("IPv4 Público", ip);
                });

                verIpv6MenuItem.addActionListener(e -> {
                    String ip = getIPAddress("https://api64.ipify.org", true);
                    showSingleIpDialog("IPv6 Público", ip);
                });

                verIpVPSMenuItem.addActionListener(e -> {
                    String ip = args.length > 0 ? args[0] : "IP VPS não fornecido como argumento.";
                    showSingleIpDialog("IP VPS", ip);
                });

                verLocalIpv4MenuItem.addActionListener(e -> {
                    List<String> ips = getLocalIPAddresses(true);
                    showLocalIpDialog("IPs Locais (IPv4)", ips);
                });

                verLocalIpv6MenuItem.addActionListener(e -> {
                    List<String> ips = getLocalIPAddresses(false);
                    showLocalIpDialog("IPs Locais (IPv6)", ips);
                });

                exitMenuItem.addActionListener(e -> System.exit(0));

                // Configurar o ícone da bandeja
                TrayIcon trayIcon = new TrayIcon(image, "IP Público");
                trayIcon.setImageAutoSize(true);

                // Mostrar o menu ao clicar com o botão direito
                trayIcon.addActionListener(e -> SwingUtilities.invokeLater(() -> {
                    Point location = MouseInfo.getPointerInfo().getLocation();
                    popupMenu.setLocation(location.x, location.y);
                    popupMenu.setInvoker(popupMenu);
                    popupMenu.setVisible(true);
                }));

                // Adicionar o ícone ao SystemTray
                SystemTray.getSystemTray().add(trayIcon);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erro ao configurar o ícone da bandeja: " + e.getMessage());
            }
        });
    }

    private static void showSingleIpDialog(String title, String ip) {
        JDialog dialog = new JDialog((Frame) null, title, true);
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

    private static void showLocalIpDialog(String title, List<String> ips) {
        JDialog dialog = new JDialog((Frame) null, title, true);
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

    private static String getIPAddress(String apiUrl, boolean preferIPv6) {
        try {
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

            // Adicionar cabeçalho para solicitar IPv6, se necessário
            if (preferIPv6) {
                connection.setRequestProperty("Accept", "application/json; q=1.0, text/plain; q=0.8");
            }

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
            String ip = in.readLine();
            in.close();

            // Verificar se é um IPv6 válido, caso solicitado
            if (preferIPv6 && ip.contains(":")) {
                return ip; // IPv6 detectado
            } else if (preferIPv6) {
                return "Erro: IPv6 não detectado. Retornado: " + ip;
            }

            return ip; // IPv4 ou IPv6
        } catch (Exception e) {
            return "Erro ao obter IP: " + e.getMessage();
        }
    }

    private static List<String> getLocalIPAddresses(boolean ipv4) {
        List<String> result = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr.isLoopbackAddress()) continue;
                    if (ipv4 && addr instanceof Inet4Address) {
                        result.add(addr.getHostAddress());
                    } else if (!ipv4 && addr instanceof Inet6Address) {
                        String raw = addr.getHostAddress();
                        // Remove zone ID (e.g. %eth0) for cleaner display
                        int zoneIdx = raw.indexOf('%');
                        result.add(zoneIdx >= 0 ? raw.substring(0, zoneIdx) : raw);
                    }
                }
            }
        } catch (Exception e) {
            result.add("Erro ao obter IPs locais: " + e.getMessage());
        }
        return result;
    }

    private static void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nenhum IP disponível para copiar.");
            return;
        }
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }
}
