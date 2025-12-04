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

public class Dashboard extends JPanel {
    private final static Color DARK_BACKGROUND = new Color(21, 25, 28);
    private final static Color VERY_DARK = new Color(5, 5, 5);
    private final static Color DARK_GREY = new Color(53, 53, 60);
    private final static Color LIGHT_GREY = new Color(156, 156, 182);
    private Controller myCar;
    private String currentMessage;
    private JTextArea dialogueField;
    private Intersection theStart;
    private Intersection theEnd;
    private Route myRoute;
    private JPanel multiButtonContainer;
    private final PropertyChangeSupport myPCS;
    private OptionContainer myOptions;

    public Dashboard(Controller theController) {
        super();
        this.myPCS = new PropertyChangeSupport(this);
        this.myCar = theController;
        setupPanel();
    }


    private void setupPanel() {
        this.setBackground(DARK_BACKGROUND);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.myOptions = new OptionContainer();
        dialogueField = InitDialogueField();
        JPanel textHolder = new JPanel();
        textHolder.setOpaque(false);
        textHolder.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
        textHolder.setBackground(LIGHT_GREY);
        textHolder.add(dialogueField);
        setRigidLine();
        this.add(textHolder);
        setRigidLine();
        this.add(loadGetRouteBox());
        setRigidLine();
        this.add(myOptions);
        setRigidLine();
    }

    private void setRigidLine() {
        add(Box.createRigidArea(new Dimension(0, 5)));
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
        routeBox.add(setIntersection(true));
        routeBox.add(setIntersection(false));
        routeBox.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        return routeBox;
    }

    /**
     * Creates a JPanel container with buttons for loading and setting routes.
     *
     * @return a JPanel container with buttons for loading and setting routes.
     */
    private JPanel loadingRouteBox() {
        JPanel routeBox = new JPanel();
        routeBox.setOpaque(false);
        routeBox.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        routeBox.add(generateRoute());
        routeBox.add(loadSavedRoutes());
        return routeBox;
    }

    void setRoute(Route theRoute) {
        myRoute = theRoute;
        System.out.println(myRoute.toDirections());
        dialogueField.setText(myRoute.toDirections());
    }

    void setPCL(PropertyChangeListener thePCL) {
        myPCS.addPropertyChangeListener(thePCL);
    }

    void setStart(Intersection theIntersection) {
        this.theStart = theIntersection;
    }

    void setEnd(Intersection theIntersection) {
        this.theEnd = theIntersection;
    }

    /**
     * Text field for containing the
     * @return the text field for containing routes and errors.
     */
    private JTextArea InitDialogueField() {
        JTextArea theField = new JTextArea(5, 30);
        theField.setLineWrap(true);
        return theField;
    }

    private void setButtonStyle(JButton theButton) {
        theButton.setForeground(Color.WHITE);
        theButton.setBackground(DARK_GREY);

        theButton.setOpaque(true);
        theButton.setBorderPainted(false);
    }

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

    private JButton generateRoute() {
        JButton theButton = new JButton();
        setButtonStyle(theButton);
        theButton.setText("Get Route");
        theButton.addActionListener(e -> {
            if (theEnd != null && theStart != null && !theEnd.equals(theStart)) {
                Route[] routes = myCar.computeRoute(theStart, theEnd);
                System.out.println("routeLength=" + routes.length);
                if (routes.length >= 1) {
                    dialogueField.setText("Found " + routes.length + " routes for you!");
                    myPCS.firePropertyChange("newRoutesComputed", null, routes);
                    myOptions.newRouteList(routes);
                }
            } else {
                dialogueField.setText("Invalid Route: Missing or same location");
            }
        });
        return theButton;
    }

    private JButton loadSavedRoutes() {
        JButton theButton = new JButton();
        setButtonStyle(theButton);
        theButton.setText("Load Route");
        theButton.addActionListener(e -> {
            Map<Integer, int[]> routes = myCar.getRoutes();
            myOptions.routeMapping(routes);
        });
        return theButton;
    }

    /**
     * Class for holding an array of options a user can choose from, like loaded routes, simulations, etc.
     */
    private class OptionContainer extends JPanel {
        private OptionContainer() {
            this.setBackground(DARK_GREY);
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setPreferredSize(new Dimension(100, 100));
            this.setOpaque(true);
            this.setVisible(true);
        }

        private void routeMapping(Map<Integer, int[]> theRoutes) {
            this.removeAll();
            for (Integer i : theRoutes.keySet()) {
                int[] startToEnd = theRoutes.get(i);
                JButton newButton = new JButton("From " + startToEnd[0] + " to " + startToEnd[1]);
                newButton.addActionListener(e -> {
                    Route theRoute = myCar.loadRoute(i);
                    myPCS.firePropertyChange("loadThisRoute", null, theRoute);
                });
                add(newButton);
            }
            repaint();
        }

        /**
         * Adds a button for a route to the panel.
         *
         * @param theRoute the route to add.
         * @param theAdditionalText any text at the beginning we want to add.
         */
        private void addRouteButton(Route theRoute, String theAdditionalText) {
            JButton newButton = new JButton(theAdditionalText +
                    SafetyChecker.truncateNum(myCar.routeSafety(theRoute), 2) + "% Danger and " +
                    SafetyChecker.truncateNum(myCar.routeTime(theRoute), 2) + " minutes long");
            newButton.addActionListener(e -> {
               myPCS.firePropertyChange("loadThisRoute", null, theRoute);
            });
            newButton.setBackground(LIGHT_GREY);
            add(newButton);
        }

        private void newRouteList(Route[] theRoutes) {
            this.removeAll();
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
            repaint();
        }
    }

}
