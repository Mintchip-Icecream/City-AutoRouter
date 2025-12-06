package UI;

import Controller.Controller;
import Map.CityMap;
import Map.Intersection;
import Map.Road;
import Map.CardinalDirection;
import Routing.Route;
import Simulation.Conditions;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/**
 * The panel that handles the displaying of maps and routes.
 *
 * @author Emily Hart and June Flores
 * @version 12/5/25
 */
public class MapPanel extends JPanel {
    /**
     * Color of our background.
     */
    private static final Color BACKGROUND = new Color(0xF4EFEF);
    /**
     * Color of any text.
     */
    private static final Color TEXT = new Color(0x000000);
    /**
     * Color of a line that isn't conditioned.
     */
    private static final Color LINE = new Color(0x848497);
    /**
     * Color of an intersection that's the endpoint of a route.
     */
    private static final Color ENDPOINT = new Color(0xF000F0);
    /**
     * Color of any location intersection that isn't an endpoint of a route.
     */
    private static final Color LOCATION = new Color(0x262726);
    /**
     * Color of a line who's highest simulated condition is obstacles.
     */
    private static final Color OBS_DANGER = new Color(0xFD5B38);
    /**
     * Color of a line who's highest simulated condition is traffic.
     */
    private static final Color TRAF_DANGER = new Color(0xFFA235);
    /**
     * Color of a line who's highest simulated condition is weather.
     */
    private static final Color WEATHER_DANGER = new Color(0x1F9AFF);
    /**
     * The stroke size of a regular map road.
     */
    private static final Stroke MAP_STROKE = new BasicStroke(1.25f);
    /**
     * The stroke size of a highlighted route road.
     */
    private static final Stroke ROUTE_STROKE = new BasicStroke(2.25f);
    /**
     * The most zoomed in that the user can set the map before it stops them from zooming in further.
     */
    private static final double MINIMUM_ZOOM = 16.0;
    /**
     * The collection of routes saved into the mapPanel.
     */
    private final List<Route> myRoutes;
    /**
     * The routes that are visible in the map.
     */
    private final Set<Route> myVisibleRoutes;
    /**
     * The roads that belong to a route.
     */
    private final Map<Route, Set<Road>> myRouteRoads;
    /**
     * The colors of a route for drawing, we want fairly unique colors for the route.
     */
    private final Map<Route, Color> myRouteColors;
    /**
     * The starting point intersection of the newest visible route.
     */
    private Intersection myStart;
    /**
     * The destination intersection of the newest visible route.
     */
    private Intersection myEnd;
    /**
     * The intersection that's been most recently clicked by the user.
     */
    private Intersection myCurrentIntersection;
    /**
     * The collection of intersections and their respective points on the map.
     */
    private Map<Intersection, Point> myIntersections;
    /**
     * The controller of the system that we can interface with to access the backend logic of the system.
     */
    private final Controller myCar;
    /**
     * The X value of the current map, which allows the location to stay consistent while a user drags the map.
     */
    private int myX = 50;
    /**
     * The Y value of the current map, which allows the location to stay consistent while a user drags the map.
     */
    private int myY = 100;
    /**
     * The current zoom value of the map.
     */
    private int myZoom = 16;
    /**
     * Determines the speed of zooming in or out, higher numbers means that the zoom value changes very extremely.
     */
    private double myZoomFactor = 5;

    /**
     * Initializes the needed components of the map.
     *
     * @param theController The controller that represents the state of the system.
     */
    public MapPanel(Controller theController) {
        myRoutes = new LinkedList<>();
        myVisibleRoutes = new HashSet<>();
        myRouteRoads = new HashMap<>();
        myRouteColors = new HashMap<>();
        myCar = theController;
        setBackground(BACKGROUND);
        MouseAdapter ma = new MapMouseAdapter();
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    /**
     * Returns the intersection that's currently selected by the user.
     *
     * @return the currently selected intersection by the user, may be null if a user hasn't clicked any.
     */
    public Intersection getCurrentIntersection() {
        return myCurrentIntersection;
    }

    /**
     * Displays multiple routes on screen, which are highlighted on the map with random colors.
     *
     * @param theRoutes the routes we'll set on the screen.
     */
    public void setRoutes(final Route[] theRoutes) {
        myRoutes.clear();
        myVisibleRoutes.clear();
        myRouteRoads.clear();
        myRouteColors.clear();
        final Random rand = new Random(0);

        for(final Route route : theRoutes) {
            myRoutes.add(route);
            myVisibleRoutes.add(route);
            Intersection[] intersections = route.getRoute();
            Set<Road> roadSet = new HashSet<>(intersections.length);

            for (int i = 1; i < intersections.length; i++) {
                roadSet.add(CityMap.getRoad(intersections[i-1], intersections[i]));
            }

            myRouteRoads.put(route, roadSet);
            myRouteColors.put(route, Color.getHSBColor(rand.nextFloat(), rand.nextFloat(0.5f, 0.8f), 0.9f));
        }
    }

    /**
     * Checks if the route is already visible or in our stored route collection.
     *
     * @param theRoute the route we want to see is already saved to the map instance.
     * @return true if the route is already contained in the map UI.
     */
    public boolean hasRoute(final Route theRoute) {
        return myVisibleRoutes.contains(theRoute) || myRoutes.contains(theRoute);
    }

    /**
     * Adds a singular route with a random color, also clears all other routes and only displays this.
     *
     * @param theRoute the route we want to display.
     */
    public void addRoute(final Route theRoute) {
        final Random rand = new Random(0);
        myVisibleRoutes.clear();
        myRoutes.add(theRoute);
        myVisibleRoutes.add(theRoute);
        Intersection[] intersections = theRoute.getRoute();
        Set<Road> roadSet = new HashSet<>(intersections.length);

        for (int i = 1; i < intersections.length; i++) {
            roadSet.add(CityMap.getRoad(intersections[i-1], intersections[i]));
        }
        myRouteRoads.put(theRoute, roadSet);
        myRouteColors.put(theRoute, Color.getHSBColor(rand.nextFloat(), rand.nextFloat(0.5f, 0.8f), 0.9f));
    }

    /**
     * Sets the end points of the current route, which will be highlighted.
     *
     * @param theStart The starting intersection we want to be highlighted.
     * @param theEnd The ending intersection we want to be highlighted.
     */
    public void setEndpoints(final Intersection theStart, final Intersection theEnd) {
        myStart = theStart;
        myEnd = theEnd;
    }

    /**
     * Sets the visibility of a route.
     * @param theRoute      the route to configure
     * @param theVisibility whether or not this route should be visible
     */
    public boolean setRouteVisibility(final Route theRoute, final boolean theVisibility) {
        myVisibleRoutes.clear();
        if (theVisibility) {
            myVisibleRoutes.add(theRoute);
            Intersection[] inters = theRoute.getRoute();
            setEndpoints(inters[0], inters[inters.length-1]);
            revalidate();
        }
        repaint();
        return theVisibility;
    }

    /**
     * Draws a line between the two map points, adjusted by the current view location & zoom.
     *
     * @param theGraphics   the graphics object to draw with
     * @param theFromPoint  the start point of the line
     * @param theToPoint    the end point of the line
     * @param theOffset     an offset (in pixels) from the endpoints to draw the line from
     */
    private void drawLine(final Graphics2D theGraphics, final Point theFromPoint, final Point theToPoint, final int theOffset) {
        final double offset = theOffset * myZoom / 16.0;
        theGraphics.draw(new Line2D.Double(
                myX + theFromPoint.x * myZoom + offset, myY + theFromPoint.y * myZoom + offset,
                myX + theToPoint.x * myZoom + offset, myY + theToPoint.y * myZoom + offset));
    }

    /**
     * Draws a circle at an intersection's map point
     *
     * @param theGraphics           the graphics object to draw with
     * @param theIntersectionPos    the map point to draw the intersection at
     */
    private void drawIntersection(final Graphics2D theGraphics, final Point theIntersectionPos) {
//        theGraphics.fillOval(myX + theIntersectionPos.x * myZoom - 4, myY + theIntersectionPos.y * myZoom - 4, 10, 10);
        final double radius = myZoom / 4.0;
        theGraphics.fill(new Ellipse2D.Double(
                myX + theIntersectionPos.x * myZoom - radius + 1,
                myY + theIntersectionPos.y * myZoom - radius + 1,
                radius * 2, radius * 2));
    }


    /**
     * Draws the currently hovered intersection and it's ID
     * @param theGraphics the Graphics2D instance we'll draw on the GUI with.
     */
    private void drawIntersectionID(final Graphics2D theGraphics) {
        if (myCurrentIntersection == null) {
            return;
        }
        theGraphics.setPaint(TEXT);
        Point point = myIntersections.get(myCurrentIntersection);
        theGraphics.drawString("" + myCurrentIntersection.getID(),
                myX + point.x * myZoom - 4, myY + point.y * myZoom - 5);
    }

    @Override
    public void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);

        final Graphics2D g2d = (Graphics2D) theGraphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(myCar.getMap() == null) {
            return;
        }
        drawMap(myCar.getMap().getAllIntersections()[0], g2d);
        if (myCurrentIntersection != null) {
            drawIntersectionID(g2d);
        }
    }

    private void drawMap(Intersection theStart, Graphics2D theGraphics) {
        final Queue<Intersection> queue = new LinkedList<>();
        final Map<Intersection, Point> intersectionPositions = new HashMap<>();

        queue.add(theStart);
        intersectionPositions.put(theStart, new Point(0, 0));

        while(!queue.isEmpty()) {
            Intersection current = queue.poll();

            // draw intersection
            final Point currentPos = intersectionPositions.get(current);

            // visit all neighboring intersections
            for(Road road : current.getRoadList()) {
                final Intersection other = getOther(road, current);

                if(!intersectionPositions.containsKey(other)) {
                    // new intersection, queue it for drawing
                    queue.add(other);
                    // and assign its position on the map
                    intersectionPositions.put(other, offset(road.getDirection(current), currentPos, road));
                } else {
                    // previously seen intersection, draw a road back to it
                    theGraphics.setStroke(MAP_STROKE);

                    // draws the road line according to the environment simulation
                    Conditions roadCon = myCar.getEnvironment().getCondition(road);
                    double worstCondition = Math.max(roadCon.getWeatherFactor(),
                            Math.max(roadCon.getTrafficDensity(), roadCon.getObstacleSeverity()));
                    if (worstCondition < 0.333) {
                        theGraphics.setPaint(LINE);
                    } else if (worstCondition == roadCon.getWeatherFactor()){
                        theGraphics.setPaint(WEATHER_DANGER);
                    } else if (worstCondition == roadCon.getObstacleSeverity()) {
                        theGraphics.setPaint(OBS_DANGER);
                    } else {
                        theGraphics.setPaint(TRAF_DANGER);
                    }
                    drawLine(theGraphics, currentPos, intersectionPositions.get(other), 0);

                    final List<Color> lines = new LinkedList<>();

                    // draw a line for each route that crosses this road
                    for(Route route : myRoutes) {
                        if(!myVisibleRoutes.contains(route)) { continue; }
                        if(myRouteRoads.get(route).contains(road)) {
                            lines.add(myRouteColors.get(route));
                        }
                    }
                    theGraphics.setStroke(ROUTE_STROKE);
                    for(int i = 0; i < lines.size(); i++) {
                        theGraphics.setPaint(lines.get(i));
                        final int offset = ((i % 2) == 0 ? 1 : -1) * (i/2 + 1);
                        drawLine(theGraphics, currentPos, intersectionPositions.get(other), offset);
                    }
                }
            }
        }
        for (Map.Entry<Intersection, Point> entry : intersectionPositions.entrySet()) {
            final Intersection intersection = entry.getKey();
            if (intersection.equals(myStart) || intersection.equals(myEnd)) {
                theGraphics.setPaint(ENDPOINT);
                drawIntersection(theGraphics, entry.getValue());
            } else if (intersection.isLocation()) {
                theGraphics.setPaint(LOCATION);
                drawIntersection(theGraphics, entry.getValue());
            }
        }
        myIntersections = intersectionPositions;
    }

    /**
     * Returns the intersection opposite to the origin intersection. Assumes that the intersection is connected to this
     * road. Otherwise, we'll return the default intersection from calling Road.getDestination. This is needed because
     * we may be traversing to the road from the intersection at its "destination", and we'll just get the
     * intersection we already have by calling Road.getDestination.
     *
     * @param theRoad The road we want to see the other intersection of.
     * @param theOrigin The intersection we're traversing to the road from.
     * @return Either the destination of the road, or the source if we're traversing to the road from the destination.
     */
    private static Intersection getOther(final Road theRoad, final Intersection theOrigin) {
        if(theRoad.getSource().equals(theOrigin)) {
            return theRoad.getDestination();
        }
        return theRoad.getSource();
    }


    /**
     * Returns a new Point that is offset in the given direction.
     * @param theDirection  the direction to offset in
     * @param thePoint      the starting point
     * @return  a new Point object
     */
    private Point offset(final CardinalDirection theDirection, final Point thePoint, final Road theRoad) {

        return switch(theDirection) {
            case NORTH -> new Point(thePoint.x, thePoint.y - roadNormalized(theRoad));
            case SOUTH -> new Point(thePoint.x, thePoint.y + roadNormalized(theRoad));
            case EAST -> new Point(thePoint.x + roadNormalized(theRoad), thePoint.y);
            case WEST -> new Point(thePoint.x - roadNormalized(theRoad), thePoint.y);
        };
    }

    /**
     * Gets the length multiplier of a road. Longer roads will affect the road's length, but short roads may
     * look uniform. For some reason, we experimented with using the scaled-up Z scores of a road, but this method
     * of dividing the road's length by a small amount like 50 meters helps.
     *
     * @param theRoad the road we want to multiply the distance of on the map.
     * @return an integer multiplier for the roads.
     */
    private static int roadNormalized(Road theRoad) {
        int result = (int) Math.round(theRoad.getLength() / 50);
        if (result == 0) {
            return 1;
        }
        return result;
    }

    /**
     * Adjusts the X and Y values of the map to allow users to zoom in and drag their position to where they want to see.
     *
     * @param theDeltaX the change in X.
     * @param theDeltaY the change in Y.
     */
    private void adjustView(final int theDeltaX, final int theDeltaY) {
        this.myX += theDeltaX;
        this.myY += theDeltaY;
        this.repaint();
    }

    /**
     * Zooms in or out of the map.
     *
     * @param theDeltaZoom the change in the zoom as determined by the mouse scroll wheel.
     */
    private void adjustZoom(final double theDeltaZoom) {
        this.myZoomFactor = Math.clamp(this.myZoomFactor - theDeltaZoom / 4, 1.0, MINIMUM_ZOOM);
        this.myZoom = (int) Math.pow(2, this.myZoomFactor);
        this.repaint();
    }

    /**
     * MouseAdapter-extending class for handling mouse events on the map GUI.
     */
    private class MapMouseAdapter extends MouseAdapter {
        /**
         * Radius so that when selecting an intersection, you don't need to find the exact pixel it's in.
         */
        private static final int CLICK_RADIUS = 10; // radius around point for hover detection.
        /**
         * The X value of the mouse at the time of updating.
         */
        private int myCurrentX = 0;
        /**
         * The Y value of  the mouse at the time of updating.
         */
        private int myCurrentY = 0;

        MapMouseAdapter() { super(); }

        /**
         * Updates the mouse coordinates, and checks if an intersection is being pressed.
         *
         * @param theEvent the event to be processed
         */
        @Override
        public void mousePressed(final MouseEvent theEvent) {
            this.myCurrentX = theEvent.getX();
            this.myCurrentY = theEvent.getY();
            updateCurrentIntersection();
        }

        /**
         * Adjusts the viewing position of the map according to the change in position of the mouse.
         *
         * @param theEvent the event to be processed
         */
        @Override
        public void mouseDragged(final MouseEvent theEvent) {
            MapPanel.this.adjustView(theEvent.getX() - this.myCurrentX, theEvent.getY() - this.myCurrentY);
            this.myCurrentX = theEvent.getX();
            this.myCurrentY = theEvent.getY();
        }

        /**
         * Adjusts the zoom of the map according to how the mouse wheel is moved.
         *
         * @param theEvent the event to be processed
         */
        @Override
        public void mouseWheelMoved(final MouseWheelEvent theEvent) {
            MapPanel.this.adjustZoom(theEvent.getWheelRotation());
        }

        /**
         * Changes the selected intersection based on if it's a location and the mouse clicked it.
         */
        private void updateCurrentIntersection() {
            for (Intersection i : myIntersections.keySet()) {
                if (!i.isLocation()) {
                    continue;
                }
                if (atPoint(myIntersections.get(i))) {
                    myCurrentIntersection = i;
                    repaint();
                    break;
                }
            }
        }

        /**
         * Checks if the current x and y values of the mouse are in this point.
         *
         * @param thePoint the area we want to check the mouse's location
         * @return if the mouse click is on the point.
         */
        private boolean atPoint(Point thePoint) {
            double distance = Math.sqrt(Math.pow(myCurrentX - (myX + thePoint.x * myZoom - 4), 2) +
                    Math.pow(myCurrentY - (myY + thePoint.y * myZoom - 4), 2));
            return distance <= CLICK_RADIUS;
        }
    }
}
