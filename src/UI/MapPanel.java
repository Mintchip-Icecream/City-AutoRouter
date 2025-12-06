package UI;

import Controller.Controller;
import Map.CityMap;
import Map.Intersection;
import Map.Road;
import Map.CardinalDirection;
import Routing.Route;
import Simulation.Conditions;

import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * A JPanel that renders the city map and any currently visible routes.
 *
 * @author Emily Hart
 * @version 12/3/25
 */
public class MapPanel extends JPanel {
    /** The background color of the map */
    private static final Color BACKGROUND = new Color(0xFFFFFF);
    /** The color of normal roads */
    private static final Color LINE = new Color(0x000000);
    /** The color of roads that have an obstacle danger */
    private static final Color OBS_DANGER = new Color(0xFD5B38);
    /** The color of roads that have traffic */
    private static final Color TRAF_DANGER = new Color(0xFFA235);
    /** The color of roads that have weather danger */
    private static final Color WEATHER_DANGER = new Color(0x1F9AFF);
    /** The color of intersections that are endpoints of the route */
    private static final Color ENDPOINT = new Color(0xF000F0);
    /** The color of intersections that are locations */
    private static final Color LOCATION = new Color(0x262726);
    /** The line stroke used for roads */
    private static final Stroke MAP_STROKE = new BasicStroke(1.25f);
    /** The line stroke used for route lines */
    private static final Stroke ROUTE_STROKE = new BasicStroke(2.25f);
    /** A constant for the minimum zoom level */
    private static final double MINIMUM_ZOOM = 16.0;

    /** The currently loaded and rendered city map */
    private CityMap myCityMap;
    /** A list of routes that have been computed */
    final private List<Route> myRoutes;
    /** The set of routes that are currently set to be rendered */
    final private Set<Route> myVisibleRoutes;
    /** A map from routes to the set of roads they traverse */
    final private Map<Route, Set<Road>> myRouteRoads;
    /** The mapping from routes to their assigned color */
    final private Map<Route, Color> myRouteColors;
    /** The current starting intersection of the routes */
    private Intersection myStart;
    /** The current destination intersection of the routes */
    private Intersection myEnd;
    /** The currently selected intersection */
    private Intersection myCurrentlyHoveredIntersection;
    /** The map of intersections to their point on the map (not affected by panning or zooming) */
    private Map<Intersection, Point> myIntersections;
    /** The controller used by this map panel */
    private final Controller myCar;

    /** The current X offset of the view area, in pixels */
    private int myX = 50;
    /** The current Y offset of the view area, in pixels */
    private int myY = 100;
    /** The current exponential zoom level, used for rendering */
    private int myZoom = 16;
    /** The current linear zoom factor, adjusted by the user */
    private double myZoomFactor = 5;

    /**
     * Constructs a MapPanel. The city map to be rendered is obtained from the given controller.
     * @param theController the controller to be used by this map panel
     */
    public MapPanel(Controller theController) {
        myRoutes = new LinkedList<>();
        myVisibleRoutes = new HashSet<>();
        myRouteRoads = new HashMap<>();
        myRouteColors = new HashMap<>();
        myCar = theController;
        myCityMap = myCar.getMap();
        setBackground(BACKGROUND);
        MouseAdapter ma = new MapMouseAdapter();
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    /**
     * Sets the routes available to the map panel for rendering. Routes are only rendered
     * if they are set to be visible.
     * @param theRoutes the array of Routes
     * @see MapPanel#setRouteVisibility
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
     * Sets the endpoint intersections for the current Routes.
     * @param theStart  the start intersection
     * @param theEnd    the destination intersection
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
    public void setRouteVisibility(final Route theRoute, final boolean theVisibility) {
        if(theVisibility) {
            myVisibleRoutes.add(theRoute);
        } else {
            myVisibleRoutes.remove(theRoute);
        }
        repaint();
    }

    /**
     * Draws a line between the two map points, adjusted by the current view location & zoom.
     * @param theGraphics   the graphics object to draw with
     * @param theFromPoint  the start point of the line
     * @param theToPoint    the end point of the line
     * @param theOffset     a diagonal offset (in pixels) from the endpoints to draw the line from
     */
    private void drawLine(final Graphics2D theGraphics, final Point theFromPoint,
                          final Point theToPoint, final int theOffset) {
        final double offset = theOffset * myZoom / 16.0;
        theGraphics.draw(new Line2D.Double(
            myX + theFromPoint.x * myZoom + offset, myY + theFromPoint.y * myZoom + offset,
            myX + theToPoint.x * myZoom + offset, myY + theToPoint.y * myZoom + offset));
    }

    /**
     * Draws a circle at an intersection's map point
     * @param theGraphics           the graphics object to draw with
     * @param theIntersectionPos    the map point to draw the intersection at
     */
    private void drawIntersection(final Graphics2D theGraphics, final Point theIntersectionPos) {
        final double radius = myZoom / 8.0;
        theGraphics.fill(new Ellipse2D.Double(
            myX + theIntersectionPos.x * myZoom - radius + 1,
            myY + theIntersectionPos.y * myZoom - radius + 1,
            radius * 2, radius * 2));
    }

    /**
     * Draws the currently hovered intersection and it's ID
     * @param theGraphics   the graphics object to draw with
     */
    private void drawIntersectionID(final Graphics2D theGraphics) {
        if (myCurrentlyHoveredIntersection == null) {
            return;
        }
        Point point = myIntersections.get(myCurrentlyHoveredIntersection);
        theGraphics.drawString("" + myCurrentlyHoveredIntersection.getID(),
                myX + point.x * myZoom - 4, myY + point.y * myZoom - 5);
    }

    /**
     * Computes positions for all intersections, and renders all roads, routes, and locations.
     * @param theGraphics   the AWT Graphics object to draw with
     */
    @Override
    public void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);
        if(myCityMap == null) {
            return;
        }

        final Graphics2D g2d = (Graphics2D) theGraphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (myCurrentlyHoveredIntersection != null) {
            drawIntersectionID(g2d);
        }
        final Queue<Intersection> queue = new LinkedList<>();
        final Map<Intersection, Point> intersectionPositions = new HashMap<>();

        Intersection start = myCityMap.getIntersection(1);
        queue.add(start);
        intersectionPositions.put(start, new Point(0, 0));

        while(!queue.isEmpty()) {
            Intersection current = queue.poll();
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
                    g2d.setStroke(MAP_STROKE);

                    // draws the road line according to the environment simulation
                    Conditions roadCon = myCar.getEnvironment().getCondition(road);
                    double worstCondition = Math.max(roadCon.getWeatherFactor(),
                            Math.max(roadCon.getTrafficDensity(), roadCon.getObstacleSeverity()));
                    if (worstCondition == roadCon.getTrafficDensity()) {
                        g2d.setPaint(TRAF_DANGER);
                    } else if (worstCondition == roadCon.getWeatherFactor()){
                        g2d.setPaint(WEATHER_DANGER);
                    } else if (worstCondition == roadCon.getObstacleSeverity()) {
                        g2d.setPaint(OBS_DANGER);
                    } else {
                        g2d.setPaint(LINE);
                    }
                    drawLine(g2d, currentPos, intersectionPositions.get(other), 0);

                    // draw a line for each route that crosses this road
                    final List<Color> lines = new LinkedList<>();
                    for(Route route : myRoutes) {
                        if(!myVisibleRoutes.contains(route)) { continue; }
                        if(myRouteRoads.get(route).contains(road)) {
                            lines.add(myRouteColors.get(route));
                        }
                    }
                    g2d.setStroke(ROUTE_STROKE);
                    for(int i = 0; i < lines.size(); i++) {
                        g2d.setPaint(lines.get(i));
                        final int offset = ((i % 2) == 0 ? 1 : -1) * (i/2 + 1);
                        drawLine(g2d, currentPos, intersectionPositions.get(other), offset);
                    }
                }
            }
        }

        // draw intersections after roads so they're layered on top of the lines
        for(Map.Entry<Intersection, Point> entry : intersectionPositions.entrySet()) {
            final Intersection intersection = entry.getKey();
            if(intersection.equals(myStart) || intersection.equals(myEnd)) {
                g2d.setPaint(ENDPOINT);
                drawIntersection(g2d, entry.getValue());
            } else if (intersection.isLocation()) {
                g2d.setPaint(LOCATION);
                drawIntersection(g2d, entry.getValue());
            }
        }
        myIntersections = intersectionPositions;
    }

    /**
     * Utility method to get the intersection at the other end of the given Road.
     * @param theRoad   the Road to get the other Intersection of
     * @param theOrigin the Intersection at one end of the road
     * @return  the Intersection at the opposite end of the road
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
     * @param theRoad       the road to use for the distance to offset by
     * @return  a new Point object
     */
    private static Point offset(final CardinalDirection theDirection, final Point thePoint, final Road theRoad) {
        return switch(theDirection) {
            case NORTH -> new Point(thePoint.x, thePoint.y - roadNormalized(theRoad));
            case SOUTH -> new Point(thePoint.x, thePoint.y + roadNormalized(theRoad));
            case EAST -> new Point(thePoint.x + roadNormalized(theRoad), thePoint.y);
            case WEST -> new Point(thePoint.x - roadNormalized(theRoad), thePoint.y);
        };
    }

    /**
     * Utility method to scale the distances between intersections to be roughly proportional
     * to their actual distance.
     * @param theRoad   the road to get the scaled distance of
     * @return  the distance
     */
    private static int roadNormalized(Road theRoad) {
        int result = (int) Math.round(theRoad.getLength() / 50);
        if (result == 0) {
            return 1;
        }
        return result;
    }

    /**
     * Adjusts the current offset of the view area.
     * @param theDeltaX the change in X offset, in pixels
     * @param theDeltaY the change in Y offset, in pixels
     */
    private void adjustView(final int theDeltaX, final int theDeltaY) {
        this.myX += theDeltaX;
        this.myY += theDeltaY;
        this.repaint();
    }

    /**
     * Adjusts the current zoom level for the map.
     * The actual zoom level (myZoom) scales exponentially so the change in scale appears
     * linear to the user.
     *
     * @param theDeltaZoom the linear zoom factor, adjusted directly by scrolling.
     */
    private void adjustZoom(final double theDeltaZoom) {
        this.myZoomFactor = Math.clamp(this.myZoomFactor - theDeltaZoom / 4, 1.0, MINIMUM_ZOOM);
        this.myZoom = (int) Math.pow(2, this.myZoomFactor);
        this.repaint();
    }

    /**
     * The MouseAdapter implementation for this MapPanel
     */
    private class MapMouseAdapter extends MouseAdapter {
        /** radius around point for hover detection. */
        private static final int HOVER_RADIUS = 10;
        /** the current mouse press X position in pixels */
        private int myCurrentX = 0;
        /** the current mouse press Y position in pixels */
        private int myCurrentY = 0;

        MapMouseAdapter() { super(); }

        /** Mouse press event handler, for panning the map and selecting intersections
         * @param theEvent  the mouse event */
        @Override
        public void mousePressed(final MouseEvent theEvent) {
            this.myCurrentX = theEvent.getX();
            this.myCurrentY = theEvent.getY();
            for (Intersection i : MapPanel.this.myIntersections.keySet()) {
                if (atPoint(myIntersections.get(i))) {
                    myCurrentlyHoveredIntersection = i;
                    repaint();
                    break;
                }
            }
        }

        /** Mouse drag event handler, for panning the map
         * @param theEvent  the mouse event */
        @Override
        public void mouseDragged(final MouseEvent theEvent) {
            MapPanel.this.adjustView(theEvent.getX() - this.myCurrentX, theEvent.getY() - this.myCurrentY);
            this.myCurrentX = theEvent.getX();
            this.myCurrentY = theEvent.getY();
        }

        /** Mouse wheel event handler, for zooming the map
         * @param theEvent  the mouse wheel event */
        @Override
        public void mouseWheelMoved(final MouseWheelEvent theEvent) {
            MapPanel.this.adjustZoom(theEvent.getWheelRotation());
        }

        /** Utility method to determine if the mouse click is within the radius of an intersection */
        private boolean atPoint(Point thePoint) {
            double distance = Math.sqrt(Math.pow(myCurrentX - (myX + thePoint.x * myZoom - 4), 2) +
                    Math.pow(myCurrentY - (myY + thePoint.y * myZoom - 4), 2));
            return distance <= HOVER_RADIUS;
        }
    }
}
