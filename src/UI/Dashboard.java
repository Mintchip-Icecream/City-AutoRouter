package UI;

import Controller.Controller;
import Map.Intersection;
import Routing.Route;
import Simulation.SafetyChecker;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Objects;

public class Dashboard extends JPanel {
    /**
     * A dark background color for the general background.
     */
    private final static Color DARK_BACKGROUND = new Color(21, 25, 28);
    /**
     * An almost black color for text or accents.
     */
    private final static Color VERY_DARK = new Color(5, 5, 5);
    /**
     * A less dark grey color for buttons and other foreground components.
     */
    private final static Color DARK_GREY = new Color(53, 53, 60);
    /**
     * Light grey color for text boxes and other items that may want to be white.
     */
    private final static Color LIGHT_GREY = new Color(156, 156, 182);
    /**
     * The instance of the controller.
     */
    private final Controller myCar;
    /**
     * The dialogue box of the dashboard, which serves as a sort of "console" for displaying messages in the system.
     */
    private final JTextArea dialogueField;
    /**
     * The property change support object for sending messages
     */
    private final PropertyChangeSupport myPCS;
    /**
     * The JPanel container for multi-option lists, like selecting a route.
     */
    private final OptionContainer myOptions;
    /**
     * The starting intersection for making routes.
     */
    private Intersection theStart;
    /**
     * The destination intersection for making routes.
     */
    private Intersection theEnd;
    /**
     * The currently displayed route, or the one passed in the UI.
     */
    private Route myRoute;

    /**
     * Initializes the Dashboard of the CAR GUI.
     *
     * @param theController the instance of the Controller object, assumed to be the same as the parent's controller.
     */
    public Dashboard(Controller theController) {
        super();
        this.myPCS = new PropertyChangeSupport(this);
        this.myCar = theController;
        this.myOptions = new OptionContainer();
        dialogueField = InitDialogueField();
        setupPanel();
    }

    /**
     * Prints a string into the dashboard's textbox or "console"
     *
     * @param theString the string that will be displayed.
     */
    public void dashLog(String theString) {
        dialogueField.setText(theString);
    }

    /**
     * Initializes the various components of the dashboard that don't need to be set to final.
     */
    private void setupPanel() {
        this.setBackground(DARK_BACKGROUND);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel textHolder = new JPanel();
        textHolder.setOpaque(false);
        textHolder.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
        textHolder.setBackground(LIGHT_GREY);
        textHolder.add(dialogueField);
        setRigidLine();
        this.add(saveSimRouteBox());
        setRigidLine();
        this.add(textHolder);
        setRigidLine();
        this.add(loadGetRouteBox());
        setRigidLine();
        JScrollPane scrollPane = new JScrollPane(myOptions, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.setBackground(DARK_BACKGROUND);
        this.add(scrollPane);
        setRigidLine();
    }

    /**
     * Adds a vertical gap between items in the main container to space out different items.
     */
    private void setRigidLine() {
        add(Box.createRigidArea(new Dimension(0, 5)));
    }

    /**
     * Button container, meant to be at the top of the dashboard, that saves the current route and simulation when
     * pressed.
     *
     * @return the button container containing the save route and save sim buttons.
     */
    private JPanel saveSimRouteBox() {
        JPanel result = new JPanel();
        result.setOpaque(false);
        result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
        result.add(saveCurrentRoute());
        result.add(Box.createRigidArea(new Dimension(10, 0)));
        result.add(saveCurrentSim());
        return result;
    }

    /**
     * Creates a JPanel container for route functionality. With a button for setting the start and ends of routes,
     * and computing routes and loading new ones.
     *
     * @return a JPanel container for route functionality.
     */
    private JPanel loadGetRouteBox() {
        JPanel result = new JPanel();
        result.setOpaque(false);
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.add(setupRouteBox());
        result.add(Box.createRigidArea(new Dimension(0, 10)));
        result.add(loadingRouteBox());
        return result;
    }

    /**
     * Creates a JPanel container with buttons for setting the starts and ends of intersections
     *
     * @return a JPanel container for holding buttons for route setting.
     */
    private JPanel setupRouteBox() {
        JPanel routeBox = new JPanel();
        routeBox.setOpaque(false);
        routeBox.setLayout(new BoxLayout(routeBox, BoxLayout.X_AXIS));
        routeBox.add(setIntersection(true));
        routeBox.add(Box.createRigidArea(new Dimension(10, 0)));
        routeBox.add(setIntersection(false));
//        routeBox.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
//        routeBox.setLayout(new GridLayout(1, 2, 5, 5));
        return routeBox;
    }

    /**
     * Creates a JPanel container with buttons for loading and setting routes along with simulation loading.
     *
     * @return a JPanel container with buttons for loading and setting routes.
     */
    private JPanel loadingRouteBox() {
        JPanel routeBox = new JPanel();
        routeBox.setOpaque(false);
        routeBox.setLayout(new BoxLayout(routeBox, BoxLayout.X_AXIS));
//        routeBox.setLayout(new GridLayout(1,2, 5, 5));
        routeBox.add(generateRoute());
        routeBox.add(Box.createRigidArea(new Dimension(10, 0)));
        routeBox.add(loadSavedRoutes());
        routeBox.add(Box.createRigidArea(new Dimension(10, 0)));
        routeBox.add(loadSims());
        return routeBox;
    }

    /**
     * Sets the currently displayed route to the passed route, prints the directions to the console textbox.
     *
     * @param theRoute theRoute we want to display.
     */
    void setRoute(Route theRoute) {
        myRoute = theRoute;
        dashLog(myRoute.toDirections());
    }

    /**
     * Sets the property change listener that this dashboard fires events to.
     *
     * @param thePCL the property change listener we want the dashboard to send messages to.
     */
    void setPCL(PropertyChangeListener thePCL) {
        myPCS.addPropertyChangeListener(thePCL);
    }

    /**
     * Sets the starting intersection, for making routes. Should be a location.
     *
     * @param theIntersection the intersection that will be the start of a route.
     */
    void setStart(Intersection theIntersection) {
        this.theStart = theIntersection;
    }

    /**
     * Sets the destination intersection, for making routes. Should be a location.
     *
     * @param theIntersection the intersection that will be the start of a route.
     */
    void setEnd(Intersection theIntersection) {
        this.theEnd = theIntersection;
    }

    /**
     * Text field for containing routes, errors, and any messages we want to display to the user.
     *
     * @return the text field for containing routes and errors.
     */
    private JTextArea InitDialogueField() {
        JTextArea theField = new JTextArea(5, 30);
        theField.setLineWrap(true);
        theField.setWrapStyleWord(true);
        return theField;
    }

    /**
     * Initializes a uniform style of button, which has a dark grey background and white text.
     *
     * @param theButton the button we want to set the style of.
     */
    private void setButtonStyle(JButton theButton) {
        theButton.setForeground(Color.WHITE);
        theButton.setBackground(DARK_GREY);

        theButton.setOpaque(true);
        theButton.setBorderPainted(false);
    }

    /**
     * Button for setting the intersections for routes. If isStart = true, then the button will change the intersection
     * that's currently selected in the map GUI to the start intersection, otherwise it sets the end intersection.
     *
     * @param isStart whether this button will set the start intersection, or the end intersection.
     * @return the button for setting the start and end intersections of routes.
     */
    private JButton setIntersection(boolean isStart) {
        JButton theButton = new JButton();
        setButtonStyle(theButton);
        if (isStart) {
            theButton.setText("Start Location: ");
        } else {
            theButton.setText("End Location: ");
        }
        theButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isStart) {
                    myPCS.firePropertyChange("getSelectedIntersectionStart", null, theEnd);
                    if (theStart != null) {
                        theButton.setText("Start location: " + theStart.getID());
                    }
                } else {
                    myPCS.firePropertyChange("getSelectedIntersectionEnd", null, theStart);
                    if (theEnd != null) {
                        theButton.setText("End Location: " + theEnd.getID());
                    }
                }
            }
        });
        return theButton;
    }

    /**
     * Button that generates the routes from the current start to the end intersections. The routes are computed
     * then sent to the options container to be displayed as a list.
     *
     * @return button that generates routes when the start and ends are set.
     */
    private JButton generateRoute() {
        JButton theButton = new JButton();
        setButtonStyle(theButton);
        theButton.setText("Get Route");
        theButton.addActionListener(e -> {
            if (theEnd != null && theStart != null && !theEnd.equals(theStart)) {
                Route[] routes = myCar.computeRoute(theStart, theEnd);
                System.out.println("routeLength=" + routes.length);
                if (routes.length >= 1) {
                    dashLog("Found " + routes.length + " routes for you!");
                    myOptions.newRouteList(routes);
                    revalidate();
                }
            } else {
                dashLog("Invalid Route: Missing or same location");
            }
        });
        return theButton;
    }

    /**
     * Button that loads all the routes saved in the database. The routes are retrieved then displayed into the
     * options container. The routes aren't complete, as it will have to be selected in the options container to be
     * turned into a route object to be displayed.
     *
     * @return button that loads the routes saved in the database.
     */
    private JButton loadSavedRoutes() {
        JButton theButton = new JButton();
        setButtonStyle(theButton);
        theButton.setText("Load Route");
        theButton.addActionListener(e -> {
            Map<Integer, int[]> routes = myCar.getRoutes();
            myOptions.routeMapping(routes);
            revalidate();
        });
        return theButton;
    }

    /**
     * Button that loads all the environment simulations saved in the database. The simulations are retrieved as their
     * simIDs and displayed on the options container, and whichever one is selected by the user will be completely
     * deserialized by the controller and displayed in the map GUI.
     *
     * @return button that loads the simulations saved in the database.
     */
    private JButton loadSims() {
        JButton simB = new JButton("Load Sim");
        setButtonStyle(simB);
        simB.addActionListener(e -> {
            Map<Integer, String> sims = myCar.getSimulations();
            myOptions.simMapping(sims);
            revalidate();
        });
        return simB;
    }

    /**
     * Button that saves the currently displayed route. Will display a dashboard log whether successful or not.
     *
     * @return button that saves the currently saved route.
     */
    private JButton saveCurrentRoute() {
        JButton theButton = new JButton();
        setButtonStyle(theButton);
        theButton.setText("Save Route");
        theButton.addActionListener(e -> {
            if (myRoute == null) {
                dashLog("Cannot save a route when none are displayed");
                return;
            }
            myCar.saveRoute(myRoute);
            dashLog("Route saved!");
            revalidate();
        });
        return theButton;
    }

    /**
     * Button that saves the current EnvironmentSimulator of the system. Whenever a user wants they can load it back.
     *
     * @return button that saves the current environment simulator.
     */
    private JButton saveCurrentSim() {
        JButton theButton = new JButton();
        setButtonStyle(theButton);
        theButton.setText("Save Simulation");
        theButton.addActionListener(e -> {
            myCar.saveSim();
            dashLog("Simulation saved");
        });
        return theButton;
    }

    /**
     * Class for holding a list of options a user can choose from, like loaded routes, simulations, etc.
     */
    private class OptionContainer extends JPanel {
        /**
         * Grid bag constraints object to specify the layout of objects in the system.
         */
        private GridBagConstraints myBag = new GridBagConstraints();

        /**
         * Initializes the container we'll put our list items in.
         */
        private OptionContainer() {
            this.setBackground(DARK_GREY);
            this.setLayout(new GridBagLayout());
            myBag.insets = new Insets(5, 5, 5, 5);
            this.setOpaque(true);
            this.setVisible(true);
        }

        /**
         * Transforms the map result from loading saved simulations into the options container.
         *
         * @param theSims the rowID'd simulations from the database.
         */
        private void simMapping(Map<Integer, String> theSims) {
            this.removeAll();
            initGridBagConstraints();
            for (Integer i : theSims.keySet()) {
                JButton newButton = new JButton("Environment at " + theSims.get(i));
                newButton.addActionListener(e -> {
                    myCar.loadSim(i);
                    myPCS.firePropertyChange("loadedSimulation", null, myCar.getEnvironment());
                });
                add(newButton, myBag);
                myBag.gridy++;
            }
        }

        /**
         * Transforms the map result from loading saved routes into the options container.
         *
         * @param theRoutes the rowID'd routes from the database.
         */
        private void routeMapping(Map<Integer, int[]> theRoutes) {
            this.removeAll();
            initGridBagConstraints();
            for (Integer i : theRoutes.keySet()) {
                int[] startToEnd = theRoutes.get(i);
                JButton newButton = new JButton("From " + startToEnd[0] + " to " + startToEnd[1]);
                newButton.addActionListener(e -> {
                    Route theRoute;
                    try {
                        theRoute = myCar.loadRoute(i);
                    } catch (RuntimeException err) {
                        dashLog("Couldn't load route, this route may be incompatible with current map!");
                        return;
                    }
                    myPCS.firePropertyChange("loadThisSavedRoute", null, theRoute);
                });
                add(newButton, myBag);
                myBag.gridy++;
            }
        }

        /**
         * Adds a button for a route to the panel.
         *
         * @param theRoute the route to add.
         * @param theAdditionalText any text at the beginning we want to add.
         */
        private void addRouteButton(Route theRoute, String theAdditionalText) {
            JButton newButton = new JButton(theAdditionalText +
                    SafetyChecker.truncateNum(myCar.routeSafety(theRoute) * 100, 2) + "% Danger, " +
                    SafetyChecker.truncateNum(myCar.routeTime(theRoute), 2) + " minutes ");
            newButton.addActionListener(e -> {
               myPCS.firePropertyChange("loadThisRoute", null, theRoute);
            });
            newButton.setBackground(LIGHT_GREY);
            add(newButton, myBag);
            myBag.gridy++;
        }

        /**
         * Creates a list of routes from controller's computeRoute() and adds it to the container.
         *
         * @param theRoutes the routes we'll make options out of.
         */
        private void newRouteList(Route[] theRoutes) {
            this.removeAll();
            initGridBagConstraints();
            if (theRoutes.length > 1) {
                Route route1 = theRoutes[0];
                addRouteButton(route1, "Safest Route: ");
                for (int i = 1; i < theRoutes.length - 1; i++) {
                    Route routeOpt = theRoutes[i];
                    addRouteButton(routeOpt, "");
                }
                Route route2 = theRoutes[theRoutes.length-1];
                addRouteButton(route2, "Shortest Route: ");
            } else {
                addRouteButton(theRoutes[0], "Only Route: ");
            }
        }

        /**
         * automatically sets the grid bag constraints to centered and stretches horizontally.
         */
        private void initGridBagConstraints() {
            myBag.gridx = 0;
            myBag.gridy = 0;
            myBag.fill = GridBagConstraints.HORIZONTAL;
            myBag.weightx = 1.0;
            myBag.anchor = GridBagConstraints.CENTER;
        }
    }

}
