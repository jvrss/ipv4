package br.com.jvrss.controller;

import br.com.jvrss.model.IPModel;
import br.com.jvrss.view.TrayView;

import javax.swing.*;

public class IPController {

    private final IPModel model;
    private final TrayView view;
    private final String[] args;

    public IPController(IPModel model, TrayView view, String[] args) {
        this.model = model;
        this.view  = view;
        this.args  = args;
    }

    /**
     * Wires the action listeners between the view menu items and the model.
     * Must be called on the EDT after {@link TrayView#initialize()}.
     */
    public void bindActions() {
        view.verIpv4MenuItem.addActionListener(e ->
                SwingUtilities.invokeLater(() -> {
                    String ip = model.getPublicIPv4();
                    view.showSingleIpDialog("IPv4 Público", ip);
                })
        );

        view.verIpv6MenuItem.addActionListener(e ->
                SwingUtilities.invokeLater(() -> {
                    String ip = model.getPublicIPv6();
                    view.showSingleIpDialog("IPv6 Público", ip);
                })
        );

        view.verIpVPSMenuItem.addActionListener(e ->
                SwingUtilities.invokeLater(() -> {
                    String ip = model.getVpsIP(args.length > 0 ? args[0] : null);
                    view.showSingleIpDialog("IP VPS", ip);
                })
        );

        view.verLocalIpv4MenuItem.addActionListener(e ->
                SwingUtilities.invokeLater(() ->
                        view.showLocalIpDialog("IPs Locais (IPv4)", model.getLocalIPv4Addresses())
                )
        );

        view.verLocalIpv6MenuItem.addActionListener(e ->
                SwingUtilities.invokeLater(() ->
                        view.showLocalIpDialog("IPs Locais (IPv6)", model.getLocalIPv6Addresses())
                )
        );

        view.exitMenuItem.addActionListener(e -> System.exit(0));
    }
}

