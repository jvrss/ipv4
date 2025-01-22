package br.com.jvrss;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
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

                JMenuItem ipv4MenuItem = new JMenuItem("Mostrar IPv4");
                JMenuItem ipv6MenuItem = new JMenuItem("Mostrar IPv6");
                JMenuItem copyIpv4MenuItem = new JMenuItem("Copiar IPv4");
                JMenuItem copyIpv6MenuItem = new JMenuItem("Copiar IPv6");
                JMenuItem exitMenuItem = new JMenuItem("Sair");

                popupMenu.add(ipv4MenuItem);
                popupMenu.add(ipv6MenuItem);
                popupMenu.add(copyIpv4MenuItem);
                popupMenu.add(copyIpv6MenuItem);
                popupMenu.addSeparator();
                popupMenu.add(exitMenuItem);

                // Ações dos botões
                ipv4MenuItem.addActionListener(e -> {
                    String ipv4 = getIPAddress("https://api.ipify.org", false);
                    JOptionPane.showMessageDialog(null, "Seu IPv4 público é: " + ipv4);
                });

                ipv6MenuItem.addActionListener(e -> {
                    String ipv6 = getIPAddress("https://api64.ipify.org", true);
                    JOptionPane.showMessageDialog(null, "Seu IPv6 público é: " + ipv6);
                });

                copyIpv4MenuItem.addActionListener(e -> {
                    String ipv4 = getIPAddress("https://api.ipify.org", false);
                    copyToClipboard(ipv4);
                    JOptionPane.showMessageDialog(null, "IPv4 copiado para a área de transferência!");
                });

                copyIpv6MenuItem.addActionListener(e -> {
                    String ipv6 = getIPAddress("https://api64.ipify.org", true);
                    copyToClipboard(ipv6);
                    JOptionPane.showMessageDialog(null, "IPv6 copiado para a área de transferência!");
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
                return "Erro: IPv6 não detectado. \nRetornado: " + ip;
            }

            return ip; // IPv4 ou IPv6
        } catch (Exception e) {
            return "Erro ao obter IP: " + e.getMessage();
        }
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
