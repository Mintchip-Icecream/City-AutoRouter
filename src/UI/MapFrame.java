package UI;

import Map.CityMap;
import Map.Intersection;
import Map.Road;
import Map.CardinalDirection;
import Routing.Route;

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

public class MapFrame extends JPanel {
    private static final Color BACKGROUND = new Color(0xFFFFFF);
    private static final Color TEXT = new Color(0x000000);
    private static final Color LINE = new Color(0x0000F0);
    private static final Color ENDPOINT = new Color(0xF000F0);

    private CityMap myCityMap;
    final private List<Route> myRoutes;
    final private Set<Route> myVisibleRoutes;
    final private Map<Route, Set<Road>> myRouteRoads;
    final private Map<Route, Color> myRouteColors;
    private Intersection myStart;
    private Intersection myEnd;

    private int myX = 50;
    private int myY = 100;
    private int myZoom = 32;
    private double myZoomFactor = 5;

    public MapFrame() {
        myRoutes = new LinkedList<>();
        myVisibleRoutes = new HashSet<>();
        myRouteRoads = new HashMap<>();
        myRouteColors = new HashMap<>();
        setBackground(BACKGROUND);
        MouseAdapter ma = new MapMouseAdapter();
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    public void setCityMap(final CityMap theCityMap) {
        myCityMap = theCityMap;
    }
    public void addRoute(final Route theRoute, final Color theColor) {
        myRoutes.add(theRoute);
        myVisibleRoutes.add(theRoute);
        Intersection[] intersections = theRoute.getRoute();
        Set<Road> roadSet = new HashSet<>(intersections.length);

        for (int i = 1; i < intersections.length; i++) {
            roadSet.add(CityMap.getRoad(intersections[i-1], intersections[i]));
        }

        myRouteRoads.put(theRoute, roadSet);
        myRouteColors.put(theRoute, theColor);
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
    private void drawLine(final Graphics2D theGraphics, final Point theFromPoint, final Point theToPoint, final int theOffset) {
        theGraphics.drawLine(
            myX + theFromPoint.x * myZoom + theOffset, myY + theFromPoint.y * myZoom + theOffset,
            myX + theToPoint.x * myZoom + theOffset, myY + theToPoint.y * myZoom + theOffset);
    }

    /**
     * Draws a circle at an intersection's map point
     * @param theGraphics           the graphics object to draw with
     * @param theIntersectionPos    the map point to draw the intersection at
     */
    private void drawIntersection(final Graphics2D theGraphics, final Point theIntersectionPos) {
        theGraphics.fillOval(myX + theIntersectionPos.x * myZoom - 4, myY + theIntersectionPos.y * myZoom - 4, 10, 10);
    }

    @Override
    public void paintComponent(final Graphics theGraphics) {
        super.paintComponent(theGraphics);
        final Graphics2D g2d = (Graphics2D) theGraphics;

        if(myCityMap == null) {
            return;
        }

        final Queue<Intersection> queue = new LinkedList<>();
        final Map<Intersection, Point> intersectionPositions = new HashMap<>();

        Intersection start = myCityMap.getIntersection(1);
        queue.add(start);
        intersectionPositions.put(start, new Point(0, 0));

        g2d.setStroke(new BasicStroke(2.0f));

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
                    intersectionPositions.put(other, offset(road.getDirection(current), currentPos));
                } else {
                    // previously seen intersection, draw a road back to it
                    g2d.setPaint(LINE);

                    final List<Color> lines = new LinkedList<>();
                    lines.add(LINE);

                    // draw a line for each route that crosses this road
                    for(Route route : myRoutes) {
                        if(!myVisibleRoutes.contains(route)) { continue; }
                        if(myRouteRoads.get(route).contains(road)) {
                            lines.add(myRouteColors.get(route));
                        }
                    }
                    final double baseOffset = lines.size() * 3 / 2.0;
                    for(int i = 0; i < lines.size(); i++) {
                        g2d.setPaint(lines.get(i));
                        drawLine(g2d, currentPos, intersectionPositions.get(other), (int) (baseOffset - i * 3));
                    }
                }
            }

            g2d.setPaint(LINE);
            if(current.equals(myStart) || current.equals(myEnd)) {
                g2d.setPaint(ENDPOINT);
            }
            drawIntersection(g2d, currentPos);
        }
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
    private static Point offset(final CardinalDirection theDirection, final Point thePoint) {
        return switch(theDirection) {
            case NORTH -> new Point(thePoint.x, thePoint.y - 1);
            case SOUTH -> new Point(thePoint.x, thePoint.y + 1);
            case EAST -> new Point(thePoint.x + 1, thePoint.y);
            case WEST -> new Point(thePoint.x - 1, thePoint.y);
        };
    }

    private void adjustView(final int theDeltaX, final int theDeltaY) {
        this.myX += theDeltaX;
        this.myY += theDeltaY;
        this.repaint();
    }

    private void adjustZoom(final double theDeltaZoom) {
        this.myZoomFactor = Math.clamp(this.myZoomFactor - theDeltaZoom / 4, 1.0, 8.0);
        this.myZoom = (int) Math.pow(2, this.myZoomFactor);
        this.repaint();
    }

    private class MapMouseAdapter extends MouseAdapter {
        private int myCurrentX = 0;
        private int myCurrentY = 0;

        MapMouseAdapter() { super(); }

        @Override
        public void mousePressed(final MouseEvent theEvent) {
            this.myCurrentX = theEvent.getX();
            this.myCurrentY = theEvent.getY();
        }

        @Override
        public void mouseDragged(final MouseEvent theEvent) {
            MapFrame.this.adjustView(theEvent.getX() - this.myCurrentX, theEvent.getY() - this.myCurrentY);
            this.myCurrentX = theEvent.getX();
            this.myCurrentY = theEvent.getY();
        }

        @Override
        public void mouseWheelMoved(final MouseWheelEvent theEvent) {
            MapFrame.this.adjustZoom(theEvent.getWheelRotation());
        }
    }
}
