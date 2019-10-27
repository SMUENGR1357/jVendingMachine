import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.HashMap;

public class VendingMachineGUI {

    private HashMap<Integer, String> pages;
    private JFXPanel jfxPanel;
    private JFrame frame;

    private class VMFrame extends JFrame implements KeyListener{

        @Override
        public void keyTyped(KeyEvent e) {
            MainDriver.keyTyped(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    public VendingMachineGUI() {
        pages = new HashMap<>();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new VMFrame();
        frame.setUndecorated(true);
        jfxPanel = new JFXPanel(); // Scrollable JCompenent
        frame.add(jfxPanel);
        jfxPanel.setSize(screenSize);
        frame.setSize(screenSize);
        frame.setVisible(true);
        jfxPanel.setVisible(true);
    }

    public void addPage(int ID, String pageHTML) {
        URL url = getClass().getResource("pages/homePage.html");
        pages.put(ID, url.toExternalForm());
    }

    public void loadPage(int pageName) {
        Platform.runLater(() -> { // FX components need to be managed by JavaFX
            WebView webView = new WebView();
            webView.getEngine().load(pages.get(pageName));
            jfxPanel.setScene(new Scene(webView));
        });
    }

}
