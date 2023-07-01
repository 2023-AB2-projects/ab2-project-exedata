package Frontend.View;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class PanelDown extends JPanel {
    private final JTextPane errorArea;

    public PanelDown() {
        errorArea = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(errorArea);
        errorArea.setEditable(false);
        errorArea.setForeground(Color.red);
        errorArea.setFont(new Font("Berlin Sans FB", Font.BOLD, 20));

        StyledDocument doc = errorArea.getStyledDocument();
        SimpleAttributeSet centerAlign = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAlign, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), centerAlign, false);

        scrollPane.setPreferredSize(new Dimension(1000, 50));

        this.add(scrollPane);
    }

    public JTextPane getErrorArea() {
        return errorArea;
    }
}
