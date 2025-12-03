package Controller;

import static org.junit.jupiter.api.Assertions.*;

import Map.CityMap;
import Routing.Route;
import Routing.RouteManager;
import Simulation.EnvironmentSimulator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

/**
 * Test class for the DBOps class. Each tests the critical functions, like if we can save routes/maps/sims,
 * retrieve a route/map/sim ID, and use that ID to load a route/map/sim ID. We use a testDB, which uses the exact
 * same schema as our system database.
 */
class DBOpsTest {
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
     * @throws IOException
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
     * @throws SQLException
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
     * @throws SQLException
     */
    @BeforeAll
    static void setUpBeforeClass() throws SQLException, IOException {
        myConnection = DriverManager.getConnection("jdbc:sqlite:database/testdb.db");
        myConnection.setAutoCommit(false);
        myDB = DBOps.getInstance();
        myDB.setConnection(myConnection);
        myMap = new CityMap(Files.readString(Path.of("src/testMap.txt")));
        mySim = new EnvironmentSimulator(myMap, 100);
        myRouter = new RouteManager(myMap, mySim);
    }

    @AfterAll
    static void resetConnection() throws SQLException {
        myConnection = DriverManager.getConnection("jdbc:sqlite:database/datadb.db");
        myConnection.setAutoCommit(false);
        myDB.setConnection(myConnection);
    }

    /**
     * Saves a map that uses the same file as myMap, then tests if they're the same.
     *
     * @throws IOException
     */
    @Test
    void saveMap() throws IOException {
        CityMap loadedMap = new CityMap(myMapID);
        assertEquals(loadedMap, myMap);
    }

    /**
     * Saves a simulation, then loads it and checks if the simulations are the same.
     *
     * @throws IOException
     * @throws SQLException
     */
    @Test
    void saveSim() throws IOException, SQLException {
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
     *
     * @throws IOException
     */
    @Test
    void saveRoute() throws IOException {
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