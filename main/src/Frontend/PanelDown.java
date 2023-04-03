package Frontend;

import javax.swing.*;
import java.awt.*;

public class PanelDown extends JPanel {
    private JLabel errorLabel;

    public PanelDown() {
        errorLabel = new JLabel("Error chanel!");
        errorLabel.setForeground(Color.red);
        errorLabel.setFont(new Font("Berlin Sans FB", Font.BOLD, 20));
        this.add(errorLabel);
    }

    public JLabel getErrorLabel() {
        return errorLabel;
    }
}
