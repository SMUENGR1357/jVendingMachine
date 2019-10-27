import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;

public class VendingMachineGUI {

    private HashMap<Integer, String> pages;
    private JFXPanel jfxPanel;
    private JFrame frame;


    public VendingMachineGUI() {
        pages = new HashMap<>();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame();
        frame.setUndecorated(true);
        jfxPanel = new JFXPanel();
        frame.add(jfxPanel);
        jfxPanel.setSize(screenSize);
        frame.setSize(screenSize);
        frame.setVisible(true);
        jfxPanel.setVisible(true);
        frame.setAutoRequestFocus(true);

    }

    public void addPage(int ID, String pageHTML) {
        URL url = getClass().getResource(pageHTML);
        pages.put(ID, url.toExternalForm());
    }

    public void loadPage(int pageName) {
        Platform.runLater(() -> { // FX components need to be managed by JavaFX
            WebView webView = new WebView();
            webView.getEngine().load(pages.get(pageName));
            Scene toLoad = new Scene(webView);
            toLoad.setOnKeyTyped(event ->
                    MainDriver.keyTyped(event)
                    );
            jfxPanel.setScene(toLoad);
        });
    }

}
