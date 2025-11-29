package UI;

import Map.Intersection;
import Routing.Route;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class RouterGUI extends JFrame {

    private final Controller myController;

    private final MapPanel myMapPanel;
    private final SidebarPanel mySidebarPanel;

    private final JMenu myViewMenu;

    public RouterGUI() throws SQLException {
        super("City-AutoRouter");
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.myViewMenu = new JMenu("View");
        this.setJMenuBar(buildMenuBar());

        this.myController = new Controller();

        this.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;

        this.myMapPanel = new MapPanel();
        c.gridx = 0;
        c.weightx = 0.9;
        this.add(this.myMapPanel, c);

        this.mySidebarPanel = new SidebarPanel(this, this.myController);
        c.gridx = 1;
        c.weightx = 0.1;
        this.add(this.mySidebarPanel, c);

        this.myMapPanel.setCityMap(myController.getMap());
    }

    private JMenuItem buildMenuItem(String text, ActionListener listener) {
        final JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }

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
                myController.loadSim(Integer.parseInt(test));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID", "Load simulation", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(this, "An unknown exception occurred!", "Load simulation", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }));
        simMenu.add(buildMenuItem("Randomize simulation", theEvent -> {
            myController.generateRandomSimulation();;
        }));

        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(mapMenu);
        menuBar.add(simMenu);
        menuBar.add(myViewMenu);
        return menuBar;
    }

    public void computeRoutes(final int theStartID, final int theEndID) {
        final Intersection theStart = this.myController.getMap().getIntersection(theStartID);
        final Intersection theEnd = this.myController.getMap().getIntersection(theEndID);
        final Route[] routes = this.myController.computeRoute(theStart, theEnd, 0.05, 5);

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

    public static void main(String[] args) throws SQLException {
        RouterGUI gui = new RouterGUI();
        gui.setVisible(true);
    }
}
