package UI;

import Map.CityMap;
import Map.Intersection;
import Routing.Route;

import java.sql.SQLException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class RouterGUI extends JFrame {

    private final Controller myController;

    private final MapFrame myMapFrame;
    private final JMenu myViewMenu;

    public RouterGUI() throws SQLException {
        super("City-AutoRouter");
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.myViewMenu = new JMenu("View");
        this.setJMenuBar(buildMenuBar());

        this.myMapFrame = new MapFrame();
        this.add(this.myMapFrame);

        this.myController = new Controller();

        this.myMapFrame.setCityMap(myController.getMap());
    }

    private JMenuBar buildMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");

        menuBar.add(fileMenu);
        menuBar.add(myViewMenu);

        return menuBar;
    }

    public void setCityMap(final CityMap theCityMap) {
        this.myMapFrame.setCityMap(theCityMap);
    }

    public void computeRoutes(final Intersection theStart, final Intersection theEnd) {
        final Route[] routes = this.myController.computeRoute(theStart, theEnd, 0.05, 5);
        this.myMapFrame.setEndpoints(theStart, theEnd);
        this.myMapFrame.setRoutes(routes);

        for(int i = 0; i < routes.length; i++) {
            final Route route = routes[i];
            final JCheckBoxMenuItem visibility = new JCheckBoxMenuItem("Route " + (i + 1), true);
            visibility.addActionListener(theEvent ->
                this.myMapFrame.setRouteVisibility(route, visibility.getState())
            );
            myViewMenu.add(visibility);
        }
    }

    public static void main(String[] args) throws SQLException {
        RouterGUI gui = new RouterGUI();

//        gui.myController.loadSim(1);
        Intersection start = gui.myController.getMap().getIntersection(1);
        Intersection end = gui.myController.getMap().getIntersection(56);
        gui.computeRoutes(start, end);

        gui.setVisible(true);
    }
}
