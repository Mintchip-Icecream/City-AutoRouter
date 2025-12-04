package Simulation;

import Map.CityMap;
import Map.Intersection;
import Map.Road;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentSimulatorTest {
    /**
     * A consistent RNG seed to initialize simulations under.
     */
    private static final long RNG_SEED = 100L;
    /**
     * A small test map with some edge cases (intersections that have no connections, separate "islands" or
     * intersections that are connected to each other but outside the rest of the map.)
     */
    private static CityMap myTestMap;
    /**
     * The default map with 74 intersections, the map loaded from the DB at ID = 1.
     */
    private static CityMap myDefaultMap;
    /**
     * The default condition of a road or intersection, with 0 risk in all conditions.
     */
    private static final Conditions myDefaultCondition = new Conditions(0, 0, 0);

    /**
     * Sets up the maps we'll use in the test
     *
     * @throws IOException
     */
    @BeforeAll
    static void initializeEnvironment() throws IOException {
        myTestMap = new CityMap(Files.readString(Path.of("src/testMap.txt")));
        myDefaultMap = new CityMap();
    }

    /**
     * Creates a lot of different simulations and checks for strange behavior like giving default conditions
     * when asking for the intersection list.
     */
    @Test
    void simulationCreationNoDefaultIntersections() {
        Random random = new Random();
        for (int iterations = 0; iterations < 100; iterations++) {
            long seed = random.nextLong();
            EnvironmentSimulator theSim = new EnvironmentSimulator(myDefaultMap, seed);
            for (java.util.Map.Entry<Intersection, Conditions> entry : theSim.getIntersectionConditions().entrySet()) {
                assertNotEquals(myDefaultCondition, entry.getValue());
            }
        }
    }

    /**
     * Creates a lot of different simulations and checks for strange behavior like giving default conditions
     * when asking for the road list, which suggests improper road initialization behavior.
     */
    @Test
    void simulationCreationNoDefaultRoads() {
        Random random = new Random();
        for (int iterations = 0; iterations < 100; iterations++) {
            long seed = random.nextLong();
            EnvironmentSimulator theSim = new EnvironmentSimulator(myDefaultMap, seed);
            for (java.util.Map.Entry<Road, Conditions> entry : theSim.getRoadConditions().entrySet()) {
                assertNotEquals(myDefaultCondition, entry.getValue());
            }
        }
    }

    /**
     * Creates a lot of different simulations and checks for strange behavior like impossible conditions
     * like conditions below 0 and 1.
     */
    @Test
    void simulationCreationNoNegativesOrAbove100Percent() {
        Random random = new Random();
        for (int iterations = 0; iterations < 100; iterations++) {
            long seed = random.nextLong();
            EnvironmentSimulator theSim = new EnvironmentSimulator(myDefaultMap, seed);
            for (java.util.Map.Entry<Intersection, Conditions> entry : theSim.getIntersectionConditions().entrySet()) {
                Conditions con = entry.getValue();
                boolean isNotPossibleCondition = (con.getObstacleSeverity() < 0 || con.getObstacleSeverity() > 1.0) ||
                        (con.getTrafficDensity() < 0 || con.getTrafficDensity() > 1.0) ||
                        (con.getWeatherFactor() < 0 || con.getWeatherFactor() > 1.0);
                assertFalse(isNotPossibleCondition);
            }
        }
    }

    /**
     * Creates a lot of different simulations and checks for strange behavior like extreme differences between
     * adjacent intersections (nearly 100% risk in one intersection then a light condition adjacent to it).
     * Before we tested if each intersection had a safety difference of 0.333, but that ignores scenarios like
     * if an intersection was just barely outside an event. But this testing did find out that we needed to
     * reduce how much overlapping events added to each other and make the severity of the even proportional to
     * how big or small the event takes place.
     */
    @Test
    void simulationCreationNoExtremeDifferences() {
        Random random = new Random();
        for (int iterations = 0; iterations < 100; iterations++) {
            long seed = random.nextLong();
            EnvironmentSimulator theSim = new EnvironmentSimulator(myDefaultMap, seed);
            for (Road r : theSim.getRoadConditions().keySet()) {
                Conditions sourceCon = theSim.getCondition(r.getSource());
                Conditions destCon = theSim.getCondition(r.getDestination());
                boolean extremeWeather = Math.abs(sourceCon.getWeatherFactor() - destCon.getWeatherFactor()) > 0.666;
                boolean extremeBlockage = Math.abs(sourceCon.getObstacleSeverity() - destCon.getObstacleSeverity()) > 0.666;
                boolean extremeTraffic = Math.abs(sourceCon.getTrafficDensity() - destCon.getTrafficDensity()) > 0.666;
                assertFalse(extremeWeather, "Weather: " +
                        (sourceCon.getWeatherFactor() - destCon.getWeatherFactor()) +
                        " at (" + r.getSource().getID() + ", " + r.getDestination().getID() + ")");
                assertFalse(extremeTraffic, "Traffic: " +
                        (sourceCon.getTrafficDensity() - destCon.getTrafficDensity()) +
                        " at (" + r.getSource().getID() + ", " + r.getDestination().getID() + ")");
                assertFalse(extremeBlockage, "Blockage: " +
                        (sourceCon.getObstacleSeverity() - destCon.getObstacleSeverity()) +
                        " at (" + r.getSource().getID() + ", " + r.getDestination().getID() + ")");
            }
        }
    }

    /**
     * Tests if we get a different default condition from the conditions when passing an invalid intersection
     * (intersection from a different map), or if we get the default condition like expected.
     */
    @Test
    void getConditionsDefault() {
        EnvironmentSimulator newSim = new EnvironmentSimulator(myDefaultMap, RNG_SEED);
        assertEquals(myDefaultCondition, newSim.getCondition(myTestMap.getIntersection(2)));
    }

    /**
     * Tests if the seed environment simulator gets the correct seed, and if it matches what it's initialized to.
     */
    @Test
    void getSeed() {
        Random rand = new Random();
        long rngSeed = rand.nextLong();
        EnvironmentSimulator newSim = new EnvironmentSimulator(myTestMap, rngSeed);
        assertEquals(rngSeed, newSim.getSeed());
    }

    /**
     * Tests if the simulator can validate if a map is equal to the map it has.
     */
    @Test
    void compareMap() {
        EnvironmentSimulator theSim = new EnvironmentSimulator(myTestMap, RNG_SEED);
        assertTrue(theSim.compareMap(myTestMap));
    }

    /**
     * Tests if the simulator can return false when comparing a map that isn't equal to the map it's initialized with.
     */
    @Test
    void compareMapNegative() {
        EnvironmentSimulator theSim = new EnvironmentSimulator(myTestMap, RNG_SEED);
        assertFalse(theSim.compareMap(myDefaultMap));
    }

    /**
     * Tests if the simulator can validate if a map that doesn't have the exact same memory address is equal to the
     * map it's initialized under.
     * @throws IOException
     */
    @Test
    void compareMapSameFile() throws IOException {
        EnvironmentSimulator theSim = new EnvironmentSimulator(myTestMap, RNG_SEED);
        CityMap theNewMap = new CityMap(Files.readString(Path.of("src/testMap.txt")));
        assertTrue(theSim.compareMap(theNewMap));
    }
}