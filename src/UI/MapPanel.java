package UI;

import Controller.Controller;
import Map.CityMap;
import Map.Intersection;
import Map.Road;
import Map.CardinalDirection;
import Routing.Route;
import Simulation.Conditions;
import Simulation.SafetyChecker;

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

public class MapPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0xFFFFFF);
    private static final Color TEXT = new Color(0x000000);
    private static final Color LINE = new Color(0x000000);
    private static final Color ENDPOINT = new Color(0xF000F0);
    private static final Stroke MAP_STROKE = new BasicStroke(1.25f);
    private static final Stroke ROUTE_STROKE = new BasicStroke(2.25f);
    private static final Color LOCATION = new Color(0x262726);
    private static final Color OBS_DANGER = new Color(0xFD5B38);
    private static final Color TRAF_DANGER = new Color(0xFFA235);
    private static final Color WEATHER_DANGER = new Color(0x1F9AFF);
    private static final double MINIMUM_ZOOM = 16.0;



    private CityMap myCityMap;
    final private List<Route> myRoutes;
    final private Set<Route> myVisibleRoutes;
    final private Map<Route, Set<Road>> myRouteRoads;
    final private Map<Route, Color> myRouteColors;
    private Intersection myStart;
    private Intersection myEnd;
    private Intersection myCurrentlyHoveredIntersection;
    private Map<Intersection, Point> myIntersections;
    private Controller myCar;

    private int myX = 50;
    private int myY = 100;
    private int myZoom = 16;
    private double myZoomFactor = 5;

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

    private void drawString(final Graphics2D theGraphics, final String theString, final Point thePoint) {
        theGraphics.drawString(theString, myX + thePoint.x * myZoom, myY + thePoint.y * myZoom);
    }

    /**
     * Draws a line between the two map points, adjusted by the current view location & zoom.
     * @param theGraphics   the graphics object to draw with
     * @param theFromPoint  the start point of the line
     * @param theToPoint    the end point of the line
     * @param theOffset     an offset (in pixels) from the endpoints to draw the line from
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
     * @param theGraphics
     */
    private void drawIntersectionID(final Graphics2D theGraphics) {
        if (myCurrentlyHoveredIntersection == null) {
            return;
        }
        Point point = myIntersections.get(myCurrentlyHoveredIntersection);
        theGraphics.drawString("" + myCurrentlyHoveredIntersection.getID(),
                myX + point.x * myZoom - 4, myY + point.y * myZoom - 5);
    }

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
    private static Point offset(final CardinalDirection theDirection, final Point thePoint, final Road theRoad) {
        return switch(theDirection) {
            case NORTH -> new Point(thePoint.x, thePoint.y - roadNormalized(theRoad));
            case SOUTH -> new Point(thePoint.x, thePoint.y + roadNormalized(theRoad));
            case EAST -> new Point(thePoint.x + roadNormalized(theRoad), thePoint.y);
            case WEST -> new Point(thePoint.x - roadNormalized(theRoad), thePoint.y);
        };
    }

    private static int roadNormalized(Road theRoad) {
        int result = (int) Math.round(theRoad.getLength() / 50);
        if (result == 0) {
            return 1;
        }
        return result;
    }

    private void adjustView(final int theDeltaX, final int theDeltaY) {
        this.myX += theDeltaX;
        this.myY += theDeltaY;
        this.repaint();
    }

    private void adjustZoom(final double theDeltaZoom) {
        this.myZoomFactor = Math.clamp(this.myZoomFactor - theDeltaZoom / 4, 1.0, MINIMUM_ZOOM);
        this.myZoom = (int) Math.pow(2, this.myZoomFactor);
        this.repaint();
    }

    private class MapMouseAdapter extends MouseAdapter {
        private int hoverRadius = 10; // radius around point for hover detection.
        private int myCurrentX = 0;
        private int myCurrentY = 0;

        MapMouseAdapter() { super(); }

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

        @Override
        public void mouseDragged(final MouseEvent theEvent) {
            MapPanel.this.adjustView(theEvent.getX() - this.myCurrentX, theEvent.getY() - this.myCurrentY);
            this.myCurrentX = theEvent.getX();
            this.myCurrentY = theEvent.getY();
        }

        @Override
        public void mouseWheelMoved(final MouseWheelEvent theEvent) {
            MapPanel.this.adjustZoom(theEvent.getWheelRotation());
        }

        private boolean atPoint(Point thePoint) {
            double distance = Math.sqrt(Math.pow(myCurrentX - (myX + thePoint.x * myZoom - 4), 2) +
                    Math.pow(myCurrentY - (myY + thePoint.y * myZoom - 4), 2));
            return distance <= hoverRadius;
        }
    }
}
