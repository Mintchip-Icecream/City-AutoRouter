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
    /**
     * The map display for the application.
     */
    private MapPanel myMapPanel;
    /**
     * The controller for the system, which also holds the system's state.
     */
    private final Controller myCar;
    /**
     * The dashboard display for the GUI for the user to interact with.
     */
    private final Dashboard myDash;
    /**
     * The state-ful menu for selecting routes to be displayed in the GUI.
     */
    private final JMenu viewMenu;
    /**
     * The state-ful menu for opening and loading maps to and from the system database.
     */
    private final JMenu fileMenu;

    /**
     * Initializes the GUI for the City AutoRouter application.
     *
     * @param theController the Controller containing the state and our interface with the backend layer of the system.
     */
    public RouterGUI(Controller theController) {
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
        fileMenu = new JMenu("File");
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

    /**
     * Initializes a menu item with the passed text and action handler.
     *
     * @param text the text of the menu item.
     * @param listener the action that's performed when the item is selected.
     * @return a menu item with the passed text and action handler.
     */
    private JMenuItem buildMenuItem(String text, ActionListener listener) {
        final JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }

    /**
     * Initializes the menu bar of the system with items for saving/loading maps, generating simulations,
     * viewing previous selected routes, getting the user guide, and changing the look and feel of the GUI.
     *
     * @return the menu bar of the GUI.
     */
    private JMenuBar buildMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        fileMenu.removeAll();
        viewMenu.removeAll();
        fileMenu.add(openMapItem());
        fileMenu.add(loadMaps());
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(simMenu());
        menuBar.add(otherMenu());
        return menuBar;
    }

    /**
     * Menu for simulating. Has a button to generate any random simulation, and another that prompts the user for
     * a seed number that will be the simulation's seed.
     *
     * @return the menu for creating simulations.
     */
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


    /**
     * Menu item for saving a map to the system from a text file. If successful, the map will be displayed in the GUI.
     *
     * @return a menu item for prompting a file input from the user representing a CityMap.
     */
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
                    this.setJMenuBar(buildMenuBar());
                    setCityMap();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return openMap;
    }

    /**
     * Nested menu for loading a previously saved map. When hovering, it will display the different maps that have been
     * saved, and selected them will change the map in the GUI.
     *
     * @return menu (meant to be nested in File) that loads a previously saved map.
     */
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
                        setCityMap();
                    }
                });
                loadMap.add(mapItem);
            }
        }
        return loadMap;
    }

    /**
     * Menu for miscellaneous items, like a menu to change the appearance of the GUI, or the user manual.
     *
     * @return a menu for items that don't fit in any category.
     */
    private JMenu otherMenu() {
        JMenu other = new JMenu("Other...");
        other.add(lookAndFeels());
        return other;
    }

    /**
     * Menu that sets the look and feel of the GUI depending on which is selected. The look and feel when the app starts
     * is "System"
     *
     * @return menu that sets the look and feel of the GUI.
     */
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

    /**
     * Sets the look and feel of the GUI, and updates the GUI to reflect the changes.
     *
     * @param theLAFName the class name of the look and feel.
     */
    private void setLookAndFeel(String theLAFName) {
        try {
            UIManager.setLookAndFeel(theLAFName);
        } catch (Exception e) {
            myDash.dashLog("Error setting Look and Feel: " + e.getMessage());
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Re-initializes the mapPanel when loading a different CityMap to completely refresh the map.
     */
    private void setCityMap() {
        this.remove(myMapPanel);
        this.myMapPanel = new MapPanel(myCar);
        this.add(myMapPanel);
        revalidate();
    }


    /**
     * Sets the routes into the view menu as a checkList, allowing users to view multiple routes at the same time.
     *
     * @param theRoutes
     */
    private void setRoutes(final Route[] theRoutes) {
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
    private void setRouteSingular(final Route[] theRoutes) {
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
    private void appendRouteView(final Route theRoute, boolean isLoaded) {
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
     * "getSelectedIntersectionStart" responds to the dashboard prompting to set their start intersection to the
     * currently selected intersection in the map GUI. "getSelectedIntersectionEnd" does the same with the destination.
     * "newRoutesComputed" is deprecated, but sets the "View" menu to the current routes.
     * "newRouteVisible" indicates that a new route has been set to visible and should be displayed in the dashboard.
     * "loadThisRoute" handles loading routes to the UI one at a time, adding a single route to the "View" menu
     * "loadThisSavedRoute" does the same thing as "loadThisRoute" for routes loaded from the database.
     * "loadedSimulation" signals the system's current simulator changing, and prompts the GUI to update the map.
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
