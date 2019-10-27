import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class VendingMachineGUI {
    public static void main(String[] args) {
        VendingMachineGUI gm = new VendingMachineGUI();
    }

    private HashMap<String, String> pages;

    public VendingMachineGUI() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        JFXPanel jfxPanel = new JFXPanel(); // Scrollable JCompenent
        frame.add(jfxPanel);
        Platform.runLater(() -> { // FX components need to be managed by JavaFX
            WebView webView = new WebView();
            URL url =  getClass().getResource("homePage.html");
            System.out.println(url.toString());
            webView.getEngine().load(url.toExternalForm());
            jfxPanel.setScene(new Scene(webView));
        });
        jfxPanel.setSize(screenSize);
        frame.setSize(screenSize);
        frame.setVisible(true);
        jfxPanel.setVisible(true);
    }

    public void addPage(String pageName, String pageHTML){

    }

}
