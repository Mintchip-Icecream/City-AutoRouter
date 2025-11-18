package UI;

import Map.CityMap;
import Routing.Route;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RouterGUI extends JFrame {

    private final MapFrame mapFrame;
    private final JMenu viewMenu;

    private RouterGUI() throws IOException {
        super("City-AutoRouter");
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        viewMenu = new JMenu("View");
        this.setJMenuBar(buildMenuBar());

        this.mapFrame = new MapFrame();
        this.add(this.mapFrame);


        CityMap newCM = new CityMap(Files.readString(Path.of("src/simMap.txt")));
        this.mapFrame.setCityMap(newCM);
    }

    private JMenuBar buildMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        return menuBar;
    }

    public void setCityMap(final CityMap theCityMap) {
        this.mapFrame.setCityMap(theCityMap);
    }

    public void setRoutes(final Route[] routes) {
        Color[] colors = {new Color(0xF00000), new Color(0x00F000)};
        for(int i = 0; i < routes.length; i++) {
            final Route route = routes[i];
            this.mapFrame.addRoute(route, colors[i]);

            final JCheckBoxMenuItem visibility = new JCheckBoxMenuItem("Route " + (i + 1), true);
            visibility.addActionListener(theEvent ->
                this.mapFrame.setRouteVisibility(route, visibility.getState())
            );

            viewMenu.add(visibility);
        }
    }

    public static void start(final CityMap theMap, final Route[] theRoutes) {
        SwingUtilities.invokeLater(() -> {
            RouterGUI gui = null;
            try {
                gui = new RouterGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gui.setCityMap(theMap);
            gui.setRoutes(theRoutes);
            gui.setVisible(true);
        });
    }
}
