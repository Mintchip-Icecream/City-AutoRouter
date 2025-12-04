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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class RouterGUI extends JFrame {

    private MapFrame mapFrame;
    private final Controller myCar;
    private final Dashboard myDash;
    private  JMenu viewMenu;
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

        viewMenu = new JMenu("View");
        this.setJMenuBar(buildMenuBar());
        PropertyChangeListener pcl = new GUIListener();
        this.addPropertyChangeListener(pcl);

        this.mapFrame = new MapFrame(theController);
        this.add(this.mapFrame);

        this.myDash = new Dashboard(theController);
        this.myDash.setPCL(pcl);
        this.add(this.myDash, BorderLayout.EAST);

//        setCityMap(myCar.getMap());
    }

    private JMenuBar buildMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu());
        menuBar.add(viewMenu);

        return menuBar;
    }

    private JMenu fileMenu() {
        JMenu file = new JMenu("File");
        file.add(openMapItem());
        file.add(loadMaps());
        return file;
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

    public void setCityMap(final CityMap theCityMap) {
//        this.mapFrame.setCityMap(theCityMap);
        this.remove(mapFrame);
        this.mapFrame = new MapFrame(myCar);
        this.add(mapFrame);
        repaint();
    }
    public void setCurrentIntersection(Intersection theIntersection) {
        this.myCurrentIntersection = theIntersection;
    }
    public Intersection getCurrentIntersection() {
        return this.myCurrentIntersection;
    }

    public void setRoutes(final Route[] theRoutes) {
        viewMenu.removeAll();
        Color[] colors = {new Color(0xF00000), new Color(0x00F000)};
        this.mapFrame.refreshRoutes();
        for(int i = 0; i < theRoutes.length; i++) {
            final Route route = theRoutes[i];
            this.mapFrame.addRoute(route, colors[i]);

            final JCheckBoxMenuItem visibility = new JCheckBoxMenuItem("Route " + (i + 1), false);
            visibility.addActionListener(theEvent -> {
                if (this.mapFrame.setRouteVisibility(route, visibility.getState())) {
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
        this.mapFrame.refreshRoutes();
        ButtonGroup bGroup = new ButtonGroup();
        for(int i = 0; i < theRoutes.length; i++) {
            final Route route = theRoutes[i];
            this.mapFrame.addRoute(route, colors[i]);
            final JRadioButtonMenuItem theOption = new JRadioButtonMenuItem("Route " + (i+1));
            theOption.addActionListener(theEvent -> {
                if (this.mapFrame.setRouteVisibility(route, theOption.isSelected())) {
                    myDash.setRoute(route);
                }
            });
            viewMenu.add(theOption);
        }
    }

    public void appendRouteView(final Route theRoute) {
        JRadioButtonMenuItem theOption = new JRadioButtonMenuItem("Loaded Route: " +
                theRoute.getRouteIDs()[0] + " to " + theRoute.getRouteIDs()[theRoute.getRouteIDs().length-1]);
        theOption.addActionListener(theEvent -> {
            if (this.mapFrame.setRouteVisibility(theRoute, theOption.isSelected())) {
                myDash.setRoute(theRoute);
            }
        });
        viewMenu.add(theOption);
    }


    private class GUIListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("getSelectedIntersectionStart")) {
                myDash.setStart(mapFrame.getCurrentIntersection());
                mapFrame.setEndpoints(mapFrame.getCurrentIntersection(), (Intersection) evt.getNewValue());
            }
            if (evt.getPropertyName().equals("getSelectedIntersectionEnd")) {
                System.out.println(mapFrame.getCurrentIntersection().getID());
                myDash.setEnd(mapFrame.getCurrentIntersection());
                mapFrame.setEndpoints((Intersection) evt.getNewValue(), mapFrame.getCurrentIntersection());
            }
            if (evt.getPropertyName().equals("newRoutesComputed")) {
//                setRoutes((Route[]) evt.getNewValue());
                setRouteSingular((Route[]) evt.getNewValue());
            }
            if (evt.getPropertyName().equals("newRouteVisible")) {
                Route r = (Route) evt.getNewValue();
                System.out.println(r.toDirections());
                myDash.setRoute((Route) evt.getNewValue());
            }
            if (evt.getPropertyName().equals("loadThisRoute")) {
                Route r = (Route) evt.getNewValue();
                myDash.setRoute((Route) evt.getNewValue());
            }
        }
    }
}
