import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;

public class VendingMachineGUI {

    private HashMap<Integer, WebView> pages;
    private JFXPanel jfxPanel;
    private JFrame frame;
    private Scene toLoad;

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
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();
        dev.setFullScreenWindow(frame);
    }

    public void addPage(int ID, String pageHTML) {
        URL url = getClass().getResource(pageHTML);
        Platform.runLater(() -> {
            WebView view = new WebView();
            view.getEngine().load(url.toExternalForm());
            pages.put(ID, view);
        });
    }

    public void loadPage(int pageName) {
        Platform.runLater(() -> { // FX components need to be managed by JavaFX
            if(toLoad==null)toLoad = new Scene(pages.get(pageName));
            else toLoad.setRoot(pages.get(pageName));
            toLoad.setOnKeyTyped(event ->
                    MainDriver.keyTyped(event)
            );
            jfxPanel.setScene(toLoad);
        });
    }

    public void changePageElement(int pageID, String elementID, String text) {
        Platform.runLater(() -> {
            WebView webView = pages.get(pageID);
            Document d = webView.getEngine().getDocument();
            d.getElementById(elementID).setTextContent(text);
            pages.put(pageID, webView);

        });
    }
}


