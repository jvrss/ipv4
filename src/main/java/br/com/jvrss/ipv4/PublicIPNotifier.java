package br.com.jvrss.ipv4;

import br.com.jvrss.ipv4.controller.IPController;
import br.com.jvrss.ipv4.model.IPModel;
import br.com.jvrss.ipv4.view.TrayView;

import javax.swing.*;
import java.awt.*;

public class PublicIPNotifier {

    public static void main(String[] args) {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null, "SystemTray não é suportado neste sistema.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                IPModel      model      = new IPModel();
                TrayView     view       = new TrayView();
                IPController controller = new IPController(model, view, args);

                view.initialize();
                controller.bindActions();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erro ao configurar o ícone da bandeja: " + e.getMessage());
            }
        });
    }
}
