package UI;

import Map.CityMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RouterGUI extends JFrame {

    private final MapFrame mapFrame;

    private RouterGUI() throws IOException {
        super("City-AutoRouter");
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setJMenuBar(buildMenuBar());

        this.mapFrame = new MapFrame();
        this.add(this.mapFrame);


        CityMap newCM = new CityMap(Files.readString(Path.of("src/simMap.txt")));
        this.mapFrame.setCityMap(newCM);
    }

    private JMenuBar buildMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("file");

        menuBar.add(fileMenu);

        return menuBar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RouterGUI gui = null;
            try {
                gui = new RouterGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gui.setVisible(true);
        });
    }
}
