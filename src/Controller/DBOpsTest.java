package Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import Map.CityMap;

import Routing.Route;
import Routing.RouteManager;

import Simulation.EnvironmentSimulator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Test class for the DBOps class. Each tests the critical functions, like if we can save routes/maps/sims,
 * retrieve a route/map/sim ID, and use that ID to load a route/map/sim ID. We use a testDB, which uses the exact
 * same schema as our system database.
 */
class DBOpsTest {
    private static final int RNG_SEED = 100;
    /**
     * Our connection to the test DB, which we use to execute queries and modify the DBOps class with.
     */
    private static Connection myConnection;
    /**
     * The database manager class, which is modified in this class to use the test DB instead.
     */
    private static DBOps myDB;
    /**
     * The map instance, which uses the testMap.txt file to give a small and easy to test map.
     */
    private static CityMap myMap;
    /**
     * Simulation with a fixed seed for consistency using the test map.
     */
    private static EnvironmentSimulator mySim;
    /**
     * Route manager for creating routes on the test map.
     */
    private static RouteManager myRouter;
    /**
     * The row ID of the map that's saved before every test.
     */
    private int myMapID;

    /**
     * Loads a map that will be the mapID of the routes and sims. We assume that saving the txt file will return
     * when loaded an equivalent map to if we initialized a map directly with the txt file, which is proven
     * with saveMap(). We need to load it every time because mapIDs are auto-incremented, so this prevents
     * a scenario where we save with mapID = 1, but no intersections with mapID = 1 exists because they were wiped,
     * since SQL tends to save the auto-increment number when deleting records.
     *
     * @throws IOException if the file we're trying to create the map from doesn't exist.
     */
    @BeforeEach
    void setUp() throws IOException {
        myDB.saveMap(Files.readString(Path.of("src/testMap.txt")), "testMap");
        Integer[] ints = myDB.getMaps().keySet().toArray(new Integer[0]);
        myMapID = ints[ints.length - 1];
    }

    /**
     * Deletes all records from the test db we added.
     *
     * @throws SQLException if an error occurs while trying to execute the deletion.
     */
    @AfterEach
    void tearDown() throws SQLException {
        Statement stmt = myConnection.createStatement();
        String stmtString = "DELETE FROM Map; DELETE FROM Intersections; DELETE FROM Roads; DELETE FROM ROUTES; " +
                "DELETE FROM Simulation; DELETE FROM Routes; DELETE FROM RouteSequence; DELETE FROM InterConditions";
        stmt.executeUpdate(stmtString);
        myConnection.commit();
        System.out.println("Wiped Records from Database");
    }

    /**
     * Initializes the DB connection and sets the DBOps singletone to utilize the testdb instead.
     *
     * @throws SQLException SQL failing to connect to the database file.
     * @throws IOException File not being found for src/testMap.txt
     */
    @BeforeAll
    static void setUpBeforeClass() throws SQLException, IOException {
        myConnection = DriverManager.getConnection("jdbc:sqlite::resource:database/testdb.db");
        myConnection.setAutoCommit(false);
        myDB = DBOps.getInstance();
        myDB.setConnection(myConnection);
        myMap = new CityMap(Files.readString(Path.of("src/testMap.txt")));
        mySim = new EnvironmentSimulator(myMap, RNG_SEED);
        myRouter = new RouteManager(myMap, mySim);
    }

    /**
     * Resets the connection back to the standard database so that other classes in the runtime don't fail.\
     *
     * @throws SQLException SQL failing to connect to the database file.
     */
    @AfterAll
    static void resetConnection() throws SQLException {
        myConnection = DriverManager.getConnection("jdbc:sqlite::resource:database/datadb.db");
        myConnection.setAutoCommit(false);
        myDB.setConnection(myConnection);
    }

    /**
     * Saves a map that uses the same file as myMap, then tests if they're the same.
     */
    @Test
    void saveMap() {
        CityMap loadedMap = new CityMap(myMapID);
        assertEquals(loadedMap, myMap);
    }

    /**
     * Saves a simulation, then loads it and checks if the simulations are the same.
     */
    @Test
    void saveSim() {
        myDB.saveSim(mySim, myMapID);
        CityMap loadedMap = new CityMap(myMapID);
        Integer[] simInts = myDB.getSimulations().keySet().toArray(new Integer[0]);
        int simID = simInts[simInts.length - 1];
        EnvironmentSimulator loadedSim = new EnvironmentSimulator(simID, loadedMap);
        // if intersections are equal, then roads are equal because there's no difference in the process of road creation.
        assertEquals(loadedSim.getIntersectionConditions(), mySim.getIntersectionConditions());
    }

    /**
     * Saves a route, then loads it and checks if the simulations are the same.
     */
    @Test
    void saveRoute() {
        Route theRoute = myRouter.getBestRoutes(myMap.getIntersection(1),
                myMap.getIntersection(5), 0.1, 1)[0];
        myDB.saveRoute(theRoute, myMapID);
        Integer[] ints = myDB.getRoutes().keySet().toArray(new Integer[0]);
        int routeID = ints[ints.length - 1];
        int[] routeList = myDB.loadRoute(routeID);
        Route loadedRoute = myRouter.loadRoute(routeList, myMap);
        assertEquals(loadedRoute, theRoute);
    }
}