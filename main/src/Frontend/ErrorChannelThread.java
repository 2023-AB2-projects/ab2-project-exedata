package Frontend;

public class ErrorChannelThread extends Thread {
    private PanelDown panelDown;
    public ErrorChannelThread(PanelDown panelDown) {
        this.panelDown = panelDown;
        panelDown.getErrorLabel().setText("Hiba uzenet!");

    }
    @Override
    public void run() {

    }
}
