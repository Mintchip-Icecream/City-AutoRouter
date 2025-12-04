
package Routing;

import Map.CityMap;
import Simulation.EnvironmentSimulator;
import Simulation.SafetyChecker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Route Manager, our API for the Routing Package.Tests the main functionality, like evaluating route
 * length with or without routes, edge cases with simulations, and the correctness of the getBestRoutes() methods.
 * This test class doesn't test the non-simulated Router method for computing routes.
 */
class RouteManagerTest {
    /**
     * The route manager class for creating routes.
     */
    private RouteManager myRouter;
    /**
     * A small test map with some edge cases (intersections that have no connections, separate "islands" or
     * intersections that are connected to each other but outside the rest of the map.)
     */
    private static CityMap myTestMap;
    /**
     * The default map with 74 intersections, the map loaded from the DB at ID = 1. Not default anymore but this is what
     * we'll test with since we made some tweaks to it.
     */
    private static CityMap myDefaultMap;
    /**
     * A fixed-seed simulation initialized on the test map.
     */
    private static EnvironmentSimulator myTestSim;
    /**
     * A fixed-seed simulation initialized on the default map. Necessary because we're more likely to have
     * multiple routes computed in the getBestRoutes() method.
     */
    private static EnvironmentSimulator myDefaultMapSim;

    /**
     * Refreshed the router to be a router between the test map and the test sim.
     */
    @BeforeEach
    void setupRouter() {
        myRouter = new RouteManager(myTestMap, myTestSim);
    }

    /**
     * Sets up the maps and simulations we'll use in testing.
     *
     * @throws IOException
     */
    @BeforeAll
    static void setupMap() throws IOException {
        myTestMap = new CityMap(Files.readString(Path.of("src/testMap.txt")));
        myDefaultMap = new CityMap(1);
        myTestSim = new EnvironmentSimulator(myTestMap, 100L);
        myDefaultMapSim = new EnvironmentSimulator(myDefaultMap, 100L);
    }

    /**
     * Tests if route has default conditions when an incompatible simulation is given during length evaluation.
     */
    @Test
    void setSimulationDifferentMapDefaults() {
        Route route = myRouter.getBestRoutes(myTestMap.getIntersection(1),
                myTestMap.getIntersection(5), 0.05, 1)[0];
        double defaultLength = myRouter.routeLength(route);
        double simLength = myRouter.routeLength(route, myDefaultMapSim);
        assertEquals(defaultLength, simLength);
    }

    /**
     * Tests if route has default conditions when an incompatible simulation is given during route creation.
     * We know that the safest route from 1 to 60 is different from the fastest route. If the simulator
     * gives the defaults when it doesn't find an intersection in the map, the fastest route should be given instead.
     * This also tests the threshold exceeding 1.0 branch
     */
    @Test
    void setSimulationDifferentMapRouteEffect() {
        myRouter = new RouteManager(myDefaultMap, myDefaultMapSim);
        Route route = myRouter.getBestRoutes(myDefaultMap.getIntersection(1),
                myDefaultMap.getIntersection(58), 0.05, 5)[0];
        myRouter.setSimulation(myTestSim);
        Route otherRoute = myRouter.getBestRoutes(myDefaultMap.getIntersection(1),
                myDefaultMap.getIntersection(58), 0.05, 1)[0];
        assertNotEquals(route, otherRoute);
    }

    /**
     * Tests a short route that we know the answer of, and if our routeManager computes the correct route.
     */
    @Test
    void getBestRoutes() {
        Route knownRoute = new Route(new int[] {1, 2, 5}, myTestMap);
        Route testRoute = myRouter.getBestRoutes(myTestMap.getIntersection(1),
                myTestMap.getIntersection(5), 0.1, 1)[0];
        assertEquals(knownRoute, testRoute);
    }

    /**
     * Tests if an invalid route (2 intersections not connected) gives an empty route list
     */
    @Test
    void invalidRoute() {
        Route[] testRoute = myRouter.getBestRoutes(myTestMap.getIntersection(1),
                myTestMap.getIntersection(6), 0.1, 1);
        assertTrue(testRoute.length < 1);
    }

    /**
     * Tests if an invalid route (2 intersections not connected) gives a null route list
     */
    @Test
    void invalidRouteSameIntersections() {
        assertThrows(IllegalArgumentException.class,
                () ->{myRouter.getBestRoutes(myTestMap.getIntersection(1),
                        myTestMap.getIntersection(1), 0.1, 1);;},
                "IllegalArgumentException should have been thrown for invalid ");
    }

    /**
     * Tests if the first route computed is safer than the second route when computing best routes.
     * Compare if the maximum risk is lower than the 2nd route in the list. We know that there are
     * two routes given when computing 1 to 58.
     */
    @Test
    void getSafestRoutesFirst() {
        myRouter = new RouteManager(myDefaultMap, myDefaultMapSim);
        Route[] route = myRouter.getBestRoutes(myDefaultMap.getIntersection(1),
                myDefaultMap.getIntersection(58), 0.05, 5);
        assertTrue(SafetyChecker.routeSafety(route[0], myDefaultMapSim) <
                SafetyChecker.routeSafety(route[1], myDefaultMapSim));
    }

    /**
     * Tests if the first route computed is slower than the second route when computing best routes.
     * Compare if the maximum risk is lower than the 2nd route in the list. We know that there are
     * two routes given when computing 1 to 58.
     */
    @Test
    void getFastestRoutesLast() {
        myRouter = new RouteManager(myDefaultMap, myDefaultMapSim);
        Route[] route = myRouter.getBestRoutes(myDefaultMap.getIntersection(1),
                myDefaultMap.getIntersection(58), 0.05, 5);
        assertTrue(myRouter.routeLength(route[0], myDefaultMapSim) >
                myRouter.routeLength(route[route.length - 1], myDefaultMapSim));
    }

    /**
     * Tests if we get empty when our max threshold with getBestRoutes is too low. We know with the default map
     * and sim, the safest route from 1->58 is 0.333
     */
    @Test
    void GetBestRoutesWithMax() {
        myRouter = new RouteManager(myDefaultMap, myDefaultMapSim);
        Route[] route = myRouter.getBestRoutes(myDefaultMap.getIntersection(1),
                myDefaultMap.getIntersection(58), 0.05, 5, 0.1);
        assertTrue(route.length < 1);
    }

    /**
     * Tests if it cuts off when the result limiter is reached. We know with the default map
     * and sim, there's two routes from 1 to 58.
     */
    @Test
    void GetBestRoutesResultLimiter() {
        myRouter = new RouteManager(myDefaultMap, myDefaultMapSim);
        Route[] route = myRouter.getBestRoutes(myDefaultMap.getIntersection(1),
                myDefaultMap.getIntersection(60), 0.05, 1, 0.357);
        assertEquals(1, route.length);
    }

}