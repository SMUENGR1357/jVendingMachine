package VMDriver;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;

public class VendingMachineGUI {

    private HashMap<Integer, WebView> pages;
    private JFXPanel jfxPanel;
    private JFrame frame;
    private Scene toLoad;

    public VendingMachineGUI() {
        // Initialize page dictionary
        pages = new HashMap<>();
        // Get system screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Create JFrame which is the container for all GUI elements
        frame = new JFrame();
        // Make GUI frame fullscreen
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();
        dev.setFullScreenWindow(frame);
        frame.setUndecorated(true);
        // Add wrapper panel for JavaFX scenes (each page of the GUI)
        jfxPanel = new JFXPanel();
        frame.add(jfxPanel);

        // Transparent 16 x 16 pixel cursor image.
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        // Create a new blank cursor.
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        // Set the blank cursor to the JFrame.
        frame.getContentPane().setCursor(blankCursor);
        // Set size of panel / frame to screen dimension
        jfxPanel.setSize(screenSize);
        frame.setSize(screenSize);
        // Make elements visible
        frame.setVisible(true);
        jfxPanel.setVisible(true);
        // Set screen focus
        frame.setAutoRequestFocus(true);

    }

    public void addPage(int ID, String pageHTML) {
        // Load local HTML as resource to render
        URL url = getClass().getResource(pageHTML);
        Platform.runLater(() -> {
            // Load HTML to webView resource, add to page dictionary
            WebView view = new WebView();
            view.getEngine().load(url.toExternalForm());
            pages.put(ID, view);
        });
    }

    public void loadPage(int pageName) {
        Platform.runLater(() -> {
            // FX components need to be managed by JavaFX thread
            if (toLoad == null)
                toLoad = new Scene(pages.get(pageName));
            else
                // setRoot is required for JavaFX pages to load correctly
                toLoad.setRoot(pages.get(pageName));

            // Add event listener for key typing
            toLoad.setOnKeyTyped(event ->
                    MainDriver.keyTyped(event)
            );
            jfxPanel.setScene(toLoad);
        });
    }

    public void modifyPageHTML(int pageID, String elementID, String text) {
        Platform.runLater(() -> {
            // Get resource for page and set element as specified
            WebView webView = pages.get(pageID);
            Document d = webView.getEngine().getDocument();
            d.getElementById(elementID).setTextContent(text);
            pages.put(pageID, webView);
        });
    }
}


