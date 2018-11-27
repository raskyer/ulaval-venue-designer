package app.gui;

import app.domain.Controller;
import app.domain.UIPanel;

import javax.swing.*;
import java.util.Objects;

import static app.gui.GUIUtils.isNotInteger;

public final class SectionSettings extends JFrame {
    private JPanel panelMain;
    private JTextField columns;
    private JTextField rows;
    private JButton okButton;
    private JButton cancelButton;

    SectionSettings(Controller controller, UIPanel ui, int x, int y, Runnable onSuccess) {
        Objects.requireNonNull(controller);
        setContentPane(panelMain);
        okButton.addActionListener(e -> {
            if (validateForm()) {
                int xInt = Integer.parseInt(columns.getText());
                int yInt = Integer.parseInt(rows.getText());
                controller.createRegularSection(x, y, xInt, yInt);
                setVisible(false);
                dispose();
                ui.repaint();
                onSuccess.run();
            }
        });

        cancelButton.addActionListener( e -> {
            setVisible(false);
            dispose();
        });
    }

    private boolean validateForm() {
        if (columns.getText().isEmpty() || rows.getText().isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "One or more fields are empty",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (isNotInteger(columns.getText()) || isNotInteger(rows.getText())) {
            JOptionPane.showMessageDialog(
                    null,
                    "One or more fields are not an integer",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (Integer.parseInt(columns.getText()) < 1 || Integer.parseInt(rows.getText()) < 1) {
            return false;
        }

        return true;
    }
}
