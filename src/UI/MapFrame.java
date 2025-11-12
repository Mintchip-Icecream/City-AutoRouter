package UI;

import Map.CityMap;
import Map.Intersection;
import Map.Road;
import Map.CardinalDirection;
import Routing.Route;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MapFrame extends JPanel {
    private static final Color BACKGROUND = new Color(0xFFFFFF);
    private static final Color TEXT = new Color(0x000000);
    private static final Color LINE = new Color(0x0000F0);

    private CityMap myCityMap;
    private Route myRoute;

    private int myX = 50;
    private int myY = 100;
    private int myZoom = 32;
    private double myZoomFactor = 5;

    public MapFrame() {
        this.setBackground(BACKGROUND);
        MouseAdapter ma = new MapMouseAdapter();
        this.addMouseListener(ma);
        this.addMouseMotionListener(ma);
        this.addMouseWheelListener(ma);
    }

    public void setCityMap(final CityMap theCityMap) {
        this.myCityMap = theCityMap;
    }
    public void setRoute(final Route theRoute) {
        this.myRoute = theRoute;
    }

    private void drawString(Graphics2D theGraphics, String theString, Point thePoint) {
        theGraphics.drawString(theString, this.myX + thePoint.x * myZoom, this.myY + thePoint.y * myZoom);
    }

    private void drawLine(Graphics2D theGraphics, Point theFromPoint, Point theToPoint) {
        theGraphics.drawLine(myX + theFromPoint.x * myZoom, myY + theFromPoint.y * myZoom, myX + theToPoint.x * myZoom, myY + theToPoint.y * myZoom);
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

        while(!queue.isEmpty()) {
            Intersection current = queue.poll();

            // draw intersection
            final Point currentPos = intersectionPositions.get(current);
            g2d.setPaint(TEXT);
            drawString(g2d, "" + current.getID(), currentPos);

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
                    drawLine(g2d, currentPos, intersectionPositions.get(other));
                }
            }
        }
    }

    private static Intersection getOther(final Road theRoad, final Intersection theOrigin) {
        if(theRoad.getSource().equals(theOrigin)) {
            return theRoad.getDestination();
        }
        return theRoad.getSource();
    }

    private static Point offset(CardinalDirection theDirection, Point thePoint) {
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
        this.myZoomFactor = Math.clamp(this.myZoomFactor + theDeltaZoom / 4, 1.0, 8.0);
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
        public void mouseWheelMoved(MouseWheelEvent theEvent) {
            MapFrame.this.adjustZoom(theEvent.getWheelRotation());
        }
    }
}
