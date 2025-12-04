package UI;

import Controller.Controller;
import Map.CityMap;
import Map.Intersection;
import Routing.Route;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RouterGUI extends JFrame {

    private MapPanel myMapPanel;
    private final Controller myCar;
    private final Dashboard myDash;
    private JMenu viewMenu;
    private JMenu fileMenu;
    private Intersection myCurrentIntersection;


    public RouterGUI(Controller theController) throws IOException {
        super("City-AutoRouter");
        this.setSize(600, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            System.out.println("could not load System Look and Feel: +" + e);
        }
        this.myCar = theController;

        viewMenu = new JMenu("Route View");
        this.setJMenuBar(buildMenuBar());
        PropertyChangeListener pcl = new GUIListener();
        this.addPropertyChangeListener(pcl);

        this.myMapPanel = new MapPanel(theController);
        this.add(this.myMapPanel);

        this.myDash = new Dashboard(theController);
        this.myDash.setPCL(pcl);
        this.add(this.myDash, BorderLayout.EAST);

//        setCityMap(myCar.getMap());
    }

    private JMenuItem buildMenuItem(String text, ActionListener listener) {
        final JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }

    private JMenuBar buildMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        fileMenu.add(openMapItem());
        fileMenu.add(loadMaps());
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(simMenu());
        menuBar.add(otherMenu());
        return menuBar;
    }

    private JMenu simMenu() {
        JMenu sim = new JMenu("Simulation");
        sim.add(buildMenuItem("Generate Simulation", theEvent -> {
            myCar.generateRandomSimulation();
            myMapPanel.repaint();
        }));
        sim.add(buildMenuItem("Simulate from Seed...", theEvent -> {
            String input = JOptionPane.showInputDialog(this, "Please enter a number:",
                    "Input Seed", JOptionPane.QUESTION_MESSAGE);
            if (input == null) {
                return;
            }
            try {
                long seed = Long.parseLong(input);
                myCar.generateSimulationFromSeed(seed);
                myMapPanel.repaint();
            } catch (NumberFormatException e) {
                myDash.dashLog("Please enter a valid number for the simulation seed");
            }
        }));
        return sim;
    }


    private JMenuItem openMapItem() {
        JMenuItem openMap = new JMenuItem("Open New Map...");
        openMap.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            FileNameExtensionFilter onlyTxt = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
            jfc.setFileFilter(onlyTxt);
            int dialogResult = jfc.showOpenDialog(this);
            if (dialogResult == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                try {
                    myCar.saveMap(selectedFile.getAbsolutePath());
                    setCityMap(myCar.getMap());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return openMap;
    }

    private JMenu loadMaps() {
        JMenu loadMap = new JMenu("Load Previous Maps...");
        Map<Integer, String> theMaps = myCar.getMaps();
        if (!theMaps.isEmpty()) {
            for (Integer i : theMaps.keySet()) {
                JMenuItem mapItem = new JMenuItem(theMaps.get(i));
                mapItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        myCar.loadMap(i);
                        setCityMap(myCar.getMap());
                    }
                });
                loadMap.add(mapItem);
            }
        }
        return loadMap;
    }
    private JMenu otherMenu() {
        JMenu other = new JMenu("Other...");
        other.add(lookAndFeels());
        return other;
    }

    private JMenu lookAndFeels() {
        JMenu laf = new JMenu("Change Look and Feel...");
        ButtonGroup bg = new ButtonGroup();
        JMenuItem crossP = buildMenuItem("Cross Platform", e -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        });
        bg.add(crossP);
        laf.add(crossP);
        JMenuItem nimbus = buildMenuItem("Nimbus", e -> {
            setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        });
        bg.add(nimbus);
        laf.add(nimbus);
        JMenuItem motif = buildMenuItem("Motif", e -> {
            setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        });
        bg.add(motif);
        laf.add(motif);
        JMenuItem system = buildMenuItem("System", e -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        });
        bg.add(system);
        laf.add(system);
        return laf;
    }

    private void setLookAndFeel(String theLAFName) {
        try {
            UIManager.setLookAndFeel(theLAFName);
        } catch (Exception e) {
            myDash.dashLog("Error setting Look and Feel: " + e.getMessage());
        }
        SwingUtilities.updateComponentTreeUI(this);
    }


    public void setCityMap(final CityMap theCityMap) {
        this.remove(myMapPanel);
        this.myMapPanel = new MapPanel(myCar);
        this.add(myMapPanel);
        revalidate();
    }

    public void setRoutes(final Route[] theRoutes) {
        viewMenu.removeAll();
        Color[] colors = {new Color(0xF00000), new Color(0x00F000)};
        myMapPanel.setRoutes(theRoutes);
        for(int i = 0; i < theRoutes.length; i++) {
            final Route route = theRoutes[i];

            final JCheckBoxMenuItem visibility = new JCheckBoxMenuItem("Route " + (i + 1), false);
            visibility.addActionListener(theEvent -> {
                if (this.myMapPanel.setRouteVisibility(route, visibility.getState())) {
                    myDash.setRoute(route);
                }
            });
            viewMenu.add(visibility);
        }
    }

    /**
     * Sets the routes as a group of radio buttons in case we only want to see one route at a time.
     */
    public void setRouteSingular(final Route[] theRoutes) {
        viewMenu.removeAll();
        Color[] colors = {new Color(0xF00000), new Color(0x00F000)};
        ButtonGroup bGroup = new ButtonGroup();
        for(int i = 0; i < theRoutes.length; i++) {
            final Route route = theRoutes[i];
            this.myMapPanel.addRoute(route);
            final JRadioButtonMenuItem theOption = new JRadioButtonMenuItem("Route " + (i+1));
            theOption.addActionListener(theEvent -> {
                if (this.myMapPanel.setRouteVisibility(route, theOption.isSelected())) {
                    myDash.setRoute(route);
                }
            });
            bGroup.add(theOption);
            viewMenu.add(theOption);

        }
    }

    /**
     * Adds a route to the "view" menu.
     *
     * @param theRoute the route to add.
     * @param isLoaded whether the route that's added was gotten from the database or not
     */
    public void appendRouteView(final Route theRoute, boolean isLoaded) {
        if (myMapPanel.hasRoute(theRoute)) {
            return;
        }
        this.myMapPanel.addRoute(theRoute);
        JRadioButtonMenuItem theOption = new JRadioButtonMenuItem((isLoaded ? "Loaded " : "") + "Route: " +
                theRoute.getRouteIDs()[0] + " to " + theRoute.getRouteIDs()[theRoute.getRouteIDs().length-1]);
        theOption.addActionListener(theEvent -> {
            if (this.myMapPanel.setRouteVisibility(theRoute, theOption.isSelected())) {
                myDash.setRoute(theRoute);
            }
        });
        viewMenu.add(theOption);
    }


    /**
     * Property Change Listener to receive updates in the UI.
     */
    private class GUIListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("getSelectedIntersectionStart")) {
                if (myMapPanel.getCurrentIntersection() == null) { return; }
                myDash.setStart(myMapPanel.getCurrentIntersection());
                myMapPanel.setEndpoints(myMapPanel.getCurrentIntersection(), (Intersection) evt.getNewValue());
            }
            if (evt.getPropertyName().equals("getSelectedIntersectionEnd")) {
                if (myMapPanel.getCurrentIntersection() == null) { return; }
                myDash.setEnd(myMapPanel.getCurrentIntersection());
                myMapPanel.setEndpoints((Intersection) evt.getNewValue(), myMapPanel.getCurrentIntersection());
            }
            if (evt.getPropertyName().equals("newRoutesComputed")) {
//                setRoutes((Route[]) evt.getNewValue());
                setRouteSingular((Route[]) evt.getNewValue());
            }
            if (evt.getPropertyName().equals("newRouteVisible")) {
                Route r = (Route) evt.getNewValue();
                myDash.setRoute(r);
            }
            if (evt.getPropertyName().equals("loadThisRoute")) {
                Route r = (Route) evt.getNewValue();
                appendRouteView(r, false);
                myDash.setRoute((Route) evt.getNewValue());
                myMapPanel.setRouteVisibility(r, true);
            }
            if (evt.getPropertyName().equals("loadThisSavedRoute")) {
                Route r = (Route) evt.getNewValue();
                Intersection[] intersections = r.getRoute();
                Route otherRoute = myCar.computeRoute(intersections[0], intersections[intersections.length-1], 0.1,
                        1)[0];
                if (!otherRoute.equals(r)) {
                    int dialogResult = JOptionPane.showConfirmDialog(myMapPanel, "The route you're" +
                            " loading isn't the safest with the current environment. Load the safest route?",
                            "Confirmation", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        r = otherRoute;
                    }
                }
                appendRouteView(r, true);
                myMapPanel.setRouteVisibility(r, true);
                myDash.setRoute((Route) evt.getNewValue());
            }
            if (evt.getPropertyName().equals("loadedSimulation")) {
                myMapPanel.repaint();
            }
        }
    }
}
