package Frontend;

import javax.swing.*;

public class PanelDown extends JPanel {
    private JLabel errorLabel;

    public PanelDown() {
        errorLabel = new JLabel("Error chanel!");
        this.add(errorLabel);
    }

    public JLabel getErrorLabel() {
        return errorLabel;
    }
}
