package Frontend;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimerThread implements Runnable {
    private final JLabel timeLabel;
    public TimerThread(PanelTop panelTop) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.now();
        timeLabel = new JLabel(dateTimeFormatter.format(localDateTime));
        timeLabel.setFont(new Font("Agency FB", Font.PLAIN, 35));
        panelTop.add(timeLabel);
    }

    @Override
    public void run() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime localDateTime;
        while (true) {
            localDateTime = LocalDateTime.now();
            timeLabel.setText(dateTimeFormatter.format(localDateTime));
        }
    }
}
