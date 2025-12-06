package UI;

import Map.Intersection;
import Controller.Controller;
import Routing.Route;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * The primary class of the front-end; a Swing JFrame with a MapPanel and SidebarPanel.
 * Receives an instance of a Controller that is used to interact with the backend.
 *
 * @author Emily Hart
 * @version 12/3/25
 */
public class RouterGUI extends JFrame {

    /**
     * The Controller used by this GUI
     */
    private final Controller myCar;
    /**
     * The GUI's map panel, used to display the city map and roads
     */
    private final MapPanel myMapPanel;
    /**
     * The GUI's sidebar panel, used to list the currently computed routes
     */
    private final SidebarPanel mySidebarPanel;
    /**
     * The JMenu used to configure which routes are currently displayed
     */
    private final JMenu myViewMenu;

    /**
     * Constructs a new RouterGUI. This is the primary UI class, so it should only be
     * constructed once.
     * @param theController the Controller instance to be used by the UI
     */
    public RouterGUI(Controller theController) {
        super("City-AutoRouter");
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.myCar = theController;

        this.myViewMenu = new JMenu("View");
        this.setJMenuBar(buildMenuBar());

        this.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        this.myMapPanel = new MapPanel(theController);
        c.gridx = 0;
        c.weightx = 0.9;
        this.add(this.myMapPanel, c);

        this.mySidebarPanel = new SidebarPanel(this, theController);
        c.gridx = 1;
        c.weightx = 0.1;
        this.add(this.mySidebarPanel, c);
    }

    /**
     * Builds a single menu item with an attached ActionListener.
     *
     * @param text      the text for the JMenuItem
     * @param listener  the ActionListener to attach
     * @return the created menu item
     */
    private JMenuItem buildMenuItem(String text, ActionListener listener) {
        final JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }

    /**
     * Constructs the menu bar and adds the static items to it.
     * @return the created JMenuBar
     */
    private JMenuBar buildMenuBar() {
        final JMenu mapMenu = new JMenu("Map");
        mapMenu.add(buildMenuItem("Import new map", theEvent -> {
            // TODO: use JFileChooser to import a .txt format map
            System.out.println("choose file");
        }));

        final JMenu simMenu = new JMenu("Simulation");
        simMenu.add(buildMenuItem("Load simulation", theEvent -> {
            String test = JOptionPane.showInputDialog(
                this,
                "Load which simulation ID?",
                "Load simulation",
                JOptionPane.QUESTION_MESSAGE);
            try {
                myCar.loadSim(Integer.parseInt(test));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID", "Load simulation", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(this, "An unknown exception occurred!", "Load simulation", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }));
        simMenu.add(buildMenuItem("Randomize simulation", theEvent -> {
            myCar.generateRandomSimulation();;
        }));

        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(mapMenu);
        menuBar.add(simMenu);
        menuBar.add(myViewMenu);
        return menuBar;
    }

    /**
     * Computes the routes between the specified intersections,
     * and updates the map & sidebar panels to display the new routes.
     *
     * @param theStartID    the ID of the start intersection
     * @param theEndID      the ID of the end intersection
     */
    public void computeRoutes(final int theStartID, final int theEndID) {
        final Intersection theStart = this.myCar.getMap().getIntersection(theStartID);
        final Intersection theEnd = this.myCar.getMap().getIntersection(theEndID);
        final Route[] routes = this.myCar.computeRoute(theStart, theEnd, 0.05, 5);

        if(routes == null) {
            JOptionPane.showMessageDialog(this, "Routing error", "No routes found!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.myMapPanel.setEndpoints(theStart, theEnd);
        this.myMapPanel.setRoutes(routes);
        this.mySidebarPanel.setRoutes(routes);

        myViewMenu.removeAll();
        for(int i = 0; i < routes.length; i++) {
            final Route route = routes[i];
            final JCheckBoxMenuItem visibility = new JCheckBoxMenuItem("Route " + (i + 1), true);
            visibility.addActionListener(theEvent ->
                this.myMapPanel.setRouteVisibility(route, visibility.getState())
            );
            myViewMenu.add(visibility);
        }
        this.repaint();
    }
}
