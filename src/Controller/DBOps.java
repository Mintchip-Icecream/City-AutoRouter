package Controller;

import Map.CityMap;
import Map.Intersection;
import Map.Road;

import Routing.Route;

import Simulation.Conditions;
import Simulation.EnvironmentSimulator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Singleton class for handling the connections and queries to the database.
 *
 * @author June Flores
 * @version 11/30/2025
 */
public class DBOps {
    /**
     * The unique instance of the class.
     */
    private static final DBOps UNIQUE_INSTANCE = new DBOps();

    /**
     * The connection to the database that we make queries and transactions into.
     */
    private Connection myConnection;

    /**
     * Instantiates the DBOps class, setting the database connection to our sqlite datadb file.
     */
    private DBOps() {
        try {
            myConnection = DriverManager.getConnection("jdbc:sqlite:database/datadb.db");
            myConnection.setAutoCommit(false);
            System.out.println("foundConnection!");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Returns the instance of the class so that clients can access the class.
     * Used by the CityMap class to load the saved intersections.
     *
     * @return the unique instance of this class.
     */
    public static synchronized DBOps getInstance() {
        return UNIQUE_INSTANCE;
    }

    /**
     * Returns the ResultSet of the intersections that have the mapID given.
     *
     * @param theMapID the mapID that we want the intersections from.
     * @return the ResultSet of the intersections that have the mapID given.
     */
    public synchronized ResultSet intersectionList(final int theMapID) {
        try {
            PreparedStatement interStmt = myConnection.prepareStatement("SELECT interID, isLocation FROM Intersections where mapID = ?");
            interStmt.setInt(1, theMapID);
            return interStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the ResultSet of the roads that have the mapID given.
     * Used by the CityMap class to load the saved roads.
     *
     * @param theMapID the mapID that we want the roads from.
     * @return the ResultSet of the roads that have the mapID given.
     */
    public synchronized ResultSet roadList(final int theMapID) {
        try {
            PreparedStatement roadStmt = myConnection.prepareStatement("SELECT sourceID, destinationID, " +
                    "roadLength, speedLimit, cardinalDirection FROM Roads where mapID = ?");
            roadStmt.setInt(1, theMapID);
            return roadStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the row in the database that has the passed simID, needed for getting the RNG seed for the simulation.
     * Used by the Simulator to instantiate the RNG seed.
     *
     * @param theSimID the row ID of the simulation in the database.
     * @return The row of the simulation as a ResultSet.
     */
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

    /**
     * Loads the set of intersections and their conditions in the simulator and updates the Simulation's use date.
     * Used by the EnvironmentSimulator class to load the conditions.
     *
     * @param theSimID the row ID of the simulation we're loading
     * @return A ResultSet with (interID, weatherRisk, obstacleRisk, trafficRisk)
     * detailing the conditions in the simulation.
     */
    public synchronized ResultSet loadSim(final int theSimID) {
        try {
            PreparedStatement simStmt = myConnection.prepareStatement("UPDATE Simulation SET lastUsed = CURRENT_TIMESTAMP WHERE simID = ?");
            simStmt.setInt(1, theSimID);
            simStmt.executeUpdate();
            myConnection.commit();
            PreparedStatement conStmt = myConnection.prepareStatement("SELECT interID, weatherRisk, obstacleRisk, " +
                    "trafficRisk FROM InterConditions WHERE simID = ?");
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

    /**
     * Returns a list of map IDs and their respective name in descending order (most recently added map first)
     *
     * @return an ordered map of mapIDs and the name of each map. Null if an error occurs.
     */
    synchronized Map<Integer, String> getMaps() {
        try {
            PreparedStatement stmt = myConnection.prepareStatement("SELECT * FROM Map ORDER BY mapID DESC");
            ResultSet rs = stmt.executeQuery();
            Map<Integer, String> resultMap = new LinkedHashMap<>();
            while (rs.next()) {
                resultMap.put(rs.getInt(1), rs.getString(2));
            }
            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a set of routes as their Route ID and a 2 integer list representing the first and last intersections,
     * this is for display purposes, when actually loading a route to use, use the loadRoute method.
     *
     * @return a map of routes and their first and last intersections ordered by most recently used. Null if an error
     * occurs.
     */
    synchronized Map<Integer, int[]> getRoutes() {
        try {
            PreparedStatement rtStmt = myConnection.prepareStatement("SELECT routeID FROM Routes ORDER BY lastUsed DESC");
            ResultSet routes = rtStmt.executeQuery();
            Map<Integer, int[]> resultMap = new LinkedHashMap<>();

            while (routes.next()) {
                int[] localIDs = new int[2];
                int routeID = routes.getInt(1);
                PreparedStatement firstInt = myConnection.prepareStatement("SELECT intersectionID FROM " +
                        "RouteSequence WHERE routeID = ? ORDER BY sequenceIndex ASC LIMIT 1");
                firstInt.setInt(1, routeID);
                ResultSet firstInter = firstInt.executeQuery();
                if (firstInter.next()) {
                    localIDs[0] = firstInter.getInt(1);
                }
                PreparedStatement secondInt = myConnection.prepareStatement("SELECT intersectionID FROM " +
                        "RouteSequence WHERE routeID = ? ORDER BY sequenceIndex DESC LIMIT 1");
                secondInt.setInt(1, routeID);
                ResultSet lastInter =  secondInt.executeQuery();
                if (lastInter.next()) {
                    localIDs[1] = lastInter.getInt(1);
                }
                resultMap.put(routeID, localIDs);
            }
            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all the simulations by order of most recently used.
     *
     * @return a map of simulationIDs and the date they were used. Null if an error is found.
     */
    synchronized Map<Integer, String> getSimulations() {
        try {
            PreparedStatement stmt = myConnection.prepareStatement("SELECT simID, date(lastUsed) FROM Simulation ORDER BY lastUsed DESC");
            ResultSet rs = stmt.executeQuery();
            Map<Integer, String> resultMap = new LinkedHashMap<>();
            while (rs.next()) {
                resultMap.put(rs.getInt(1), rs.getString(2));
            }
            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves a map according to the map constructing .txt file, then returns the map that's constructed from the
     * file.
     *
     * @param theMapString The contents of the .txt file containing the map.
     * @param theMapName The name of the map that will be displayed when getting it from the database.
     * @return the CityMap instance that is saved according to the input text. Null if saving fails.
     */
    synchronized CityMap saveMap(String theMapString, String theMapName) {
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
            return theMap;
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

    /**
     * Saves an EnvironmentSimulator instance according to the mapID passed. If the map does not correlate to the
     * map instance in the simulation, then a foreign key constraint might be violated. But that's the job of the
     * controller to make sure it doesn't pass an EnvironmentSimulator with the wrong map.
     *
     * @param theSim The EnvironmentSimulator that will be saved to the database.
     * @param theMapID The mapID that the simulation will be associated with the simulation.
     */
    synchronized void saveSim(EnvironmentSimulator theSim, int theMapID) {
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

    /**
     * Saves a route that exists on a map. The intersections in the route should correlate the intersections
     * on the map whose ID is passed, otherwise a foreign key constraint may be violated and it won't save.
     *
     * @param theRoute The route that's saved.
     * @param theMapID The map that the route takes place in.
     */
    synchronized void saveRoute(final Route theRoute, final int theMapID) {
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

    /**
     * Loads the intersectionID list from a route saved in the DB.
     *
     * @param theRouteID the row ID of the route in the database.
     * @return the ordered intersectionID array of the route associated with the routeID.
     */
    synchronized int[] loadRoute(final int theRouteID) {
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

    /**
     * Redirects the DBOps instance to use the connection passed. Used by the DBOpsTest class to make the instance
     * use the test DB instead of the database that users will use.
     *
     * @param theConnection The connection to an SQL database that the DBOps instance will use.
     */
    synchronized void setConnection(Connection theConnection) {
        this.myConnection = theConnection;
    }
}