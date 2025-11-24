package DB;

import Map.CityMap;
import Map.Intersection;
import Map.Road;
import Routing.Route;
import Simulation.Conditions;
import Simulation.EnvironmentSimulator;

import java.sql.*;
import java.util.ArrayList;

public class DBOps {
    private Connection myConnection;
    private static DBOps uniqueInstance = new DBOps();

    private DBOps() {
        try {
            myConnection = DriverManager.getConnection("jdbc:sqlite:database/datadb.db");
            myConnection.setAutoCommit(false);
            System.out.println("foundConnection!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static synchronized DBOps getInstance() {
        return uniqueInstance;
    }

    // plan: Route takes a txt, generates a map from it,
    public synchronized void saveMap(String theMapString, String theMapName) {
        try {
            PreparedStatement mapStmt = myConnection.prepareStatement("INSERT INTO Map (mapName) VALUES (?)");
            mapStmt.setString(1, theMapName);
            mapStmt.executeUpdate();
            ResultSet rs = mapStmt.getGeneratedKeys();
            int mapID = rs.getInt(1);
            CityMap theMap = new CityMap(theMapString);
            PreparedStatement interStmt = myConnection.prepareStatement("INSERT INTO Intersections (interID, " +
                    "mapID, isLocation) VALUES (?, ?, ?)");
            for (Intersection i : theMap.getAllIntersections()) {
                interStmt.setInt(1, i.getID());
                interStmt.setInt(2, mapID);
                interStmt.setInt(3, i.isLocation()? 1 : 0);
                interStmt.addBatch();
            }
            interStmt.executeBatch();

            PreparedStatement roadStmt = myConnection.prepareStatement("INSERT INTO Roads (sourceID, destinationID, " +
                    "mapID, roadLength, speedLimit, cardinalDirection) VALUES (?, ?, ?, ?, ?, ?)");
            for (Road r : theMap.getAllRoads()) {
                roadStmt.setInt(1, r.getSource().getID());
                roadStmt.setInt(2, r.getDestination().getID());
                roadStmt.setInt(3, mapID);
                roadStmt.setDouble(4, r.getLength());
                roadStmt.setDouble(5, r.getSpeedLimit());
                roadStmt.setString(6, r.getDirection().toString().toUpperCase());
                roadStmt.addBatch();
            }
            roadStmt.executeBatch();
            myConnection.commit();
            System.out.println("Saving was successful");
        } catch (SQLException e) {
            if (myConnection != null) {
                try {
                    System.err.print("Rolling Back Transaction");
                    myConnection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    public synchronized void saveSim(EnvironmentSimulator theSim, int theMapID) {
        try {
            PreparedStatement simStmt = myConnection.prepareStatement("INSERT INTO Simulation (rngSeed) VALUES (?)");
            simStmt.setLong(1, theSim.getSeed());
            simStmt.executeUpdate();
            ResultSet rs = simStmt.getGeneratedKeys();
            int simID = rs.getInt(1);
            PreparedStatement conStmt = myConnection.prepareStatement("INSERT INTO InterConditions (simID, " +
                    "interID, mapID, weatherRisk, obstacleRisk, trafficRisk) VALUES (?, ?, ?, ?, ?, ?)");
            for (java.util.Map.Entry<Intersection, Conditions> e: theSim.getIntersectionConditions().entrySet()) {
                Intersection i = e.getKey();
                Conditions con = e.getValue();
                conStmt.setInt(1, simID);
                conStmt.setInt(2, i.getID());
                conStmt.setInt(3, theMapID);
                conStmt.setDouble(4, con.getWeatherFactor());
                conStmt.setDouble(5, con.getObstacleSeverity());
                conStmt.setDouble(6, con.getTrafficDensity());
                conStmt.addBatch();
            }
            conStmt.executeBatch();
            myConnection.commit();
            System.out.println("Successfully saved simulation");
        } catch (SQLException e) {
            if (myConnection != null) {
                try {
                    System.err.print("Rolling Back Transaction");
                    myConnection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    public synchronized void saveRoute(final Route theRoute, final int theMapID) {
        try {
            PreparedStatement savedRoute = myConnection.prepareStatement("INSERT INTO Routes (lastUsed) VALUES (CURRENT_TIMESTAMP)");
            savedRoute.executeUpdate();
            ResultSet rs = savedRoute.getGeneratedKeys();
            int routeID = rs.getInt(1);
            Intersection[] intList = theRoute.getRoute();
            PreparedStatement routeStmt = myConnection.prepareStatement("INSERT INTO RouteSequence (routeID, " +
                    "intersectionID, mapID, sequenceIndex) VALUES (?, ?, ?, ?)");
            for (int i = 0; i < intList.length; i++) {
                Intersection inter = intList[i];
                routeStmt.setInt(1, routeID);
                routeStmt.setInt(2, inter.getID());
                routeStmt.setInt(3, theMapID);
                routeStmt.setInt(4, i);
                routeStmt.addBatch();
            }
            routeStmt.executeBatch();
            myConnection.commit();
            System.out.println("Successfully saved route");
        } catch (SQLException e) {
            if (myConnection != null) {
                try {
                    System.err.print("Rolling Back Transaction");
                    myConnection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    public synchronized int[] loadRoute(final int theRouteID) {
        try {
            PreparedStatement routeUpdate = myConnection.prepareStatement("UPDATE Routes SET lastUsed = CURRENT_TIMESTAMP WHERE routeID = ?");
            routeUpdate.setInt(1, theRouteID);
            routeUpdate.executeUpdate();
            myConnection.commit();
            PreparedStatement rtStmt = myConnection.prepareStatement("SELECT * FROM RouteSequence WHERE routeID = ? " +
                    "ORDER BY sequenceIndex ASC");
            rtStmt.setInt(1, theRouteID);
            ResultSet rs = rtStmt.executeQuery();
            ArrayList<Integer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getInt(2));
            }
            return result.stream().mapToInt(i -> i).toArray();
        } catch (SQLException e) {
            if (myConnection != null) {
                try {
                    System.err.print("Rolling Back Transaction");
                    myConnection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ResultSet intersectionList(final int theMapID) {
        try {
            PreparedStatement interStmt = myConnection.prepareStatement("SELECT * FROM Intersections where mapID = ?");
            interStmt.setInt(1, theMapID);
            return interStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ResultSet roadList(final int theMapID) {
        try {
            PreparedStatement roadStmt = myConnection.prepareStatement("SELECT * FROM Roads where mapID = ?");
            roadStmt.setInt(1, theMapID);
            return roadStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ResultSet loadSimTuple(final int theSimID) {
        try {
            PreparedStatement simStmt = myConnection.prepareStatement("SELECT * FROM Simulation where simID = ?");
            simStmt.setInt(1, theSimID);
            return simStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized ResultSet loadSim(final int theSimID) {
        try {
            PreparedStatement simStmt = myConnection.prepareStatement("UPDATE Simulation SET lastUsed = CURRENT_TIMESTAMP WHERE simID = ?");
            simStmt.setInt(1, theSimID);
            simStmt.executeUpdate();
            myConnection.commit();
            PreparedStatement conStmt = myConnection.prepareStatement("SELECT * FROM InterConditions WHERE simID = ?");
            conStmt.setInt(1, theSimID);
            return conStmt.executeQuery();
        } catch (SQLException e) {
            if (myConnection != null) {
                try {
                    System.err.print("Rolling Back Transaction");
                    myConnection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return null;
    }
}