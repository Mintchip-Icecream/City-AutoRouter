package Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CityMapTest {
    /**
     * A small test map with some edge cases (intersections that have no connections, separate "islands" or
     * intersections that are connected to each other but outside the rest of the map.)
     */
    private static CityMap myTestMap;
    /**
     * The default map with 74 intersections, the map loaded from the DB at ID = 1.
     */
    private static CityMap myDefaultMap;

    @BeforeAll
    static void setup() throws IOException {
        myTestMap = new CityMap(Files.readString(Path.of("src/testMap.txt")));
        myDefaultMap = new CityMap();
    }

    /**
     * Tests if having an overly short line leads to a throw
     */
    @Test
    void throwsInvalidTxtShortLine() {
        String text = "I 1 I 1 2 R 1 2 100 10 S";
        assertThrows(IllegalArgumentException.class,
                () ->{new CityMap(text);},
                "IllegalArgumentException should have been thrown for intersection line too short");
    }
    /**
     * Tests if having an overly short line leads to a throw, even if it's the last line
     */
    @Test
    void throwsInvalidTxtShortLineLast() {
        String text = "I 1 1 I 1 2 R 1 2 100 ";
        assertThrows(IllegalArgumentException.class,
                () ->{new CityMap(text);},
                "IllegalArgumentException should have been thrown for lines too short");
    }

    /**
     * Tests if CityMap throws with an invalid mapID passed.
     */
    @Test
    void throwsInvalidIDNo() {
        assertThrows(IllegalArgumentException.class,
                () ->{new CityMap(300);},
                "IllegalArgumentException should have been thrown for intersection line too short");
    }
    /**
     * Tests getting a road with null 1st Intersection, which should give null.
     * Also tests getting an invalid intersection.
     */
    @Test
    void getRoadNull1stInter() {
        assertNull(CityMap.getRoad(myTestMap.getIntersection(1),
                myTestMap.getIntersection(9)));
    }

    /**
     * Tests getting a road with null 2nd Intersection, which should give null
     */
    @Test
    void getRoadNull2ndInter() {
        assertNull(CityMap.getRoad(myTestMap.getIntersection(10),
                myTestMap.getIntersection(1)));
    }

    /**
     * Tests getting a road with unconnected intersections.
     */
    @Test
    void getRoadNoConnection() {
        assertNull(CityMap.getRoad(myTestMap.getIntersection(4),
                myTestMap.getIntersection(1)));
    }

    /**
     * Tests getting a road when the source is the 1st intersection passed.
     */
    @Test
    void getRoadSourceFirst() {
        assertNotNull(CityMap.getRoad(myTestMap.getIntersection(1),
                myTestMap.getIntersection(3)));
    }

    /**
     * Tests getting a road when the source is the 2nd intersection passed.
     */
    @Test
    void getRoadSourceSecond() {
        assertNotNull(CityMap.getRoad(myTestMap.getIntersection(3),
                myTestMap.getIntersection(1)));
    }

    @Test
    void getDefaultMapID() {
        assertEquals(1, CityMap.getDefaultMapID());
    }

    /**
     * Tests if getAllIntersections gives the same list as if we manually put the intersections into a list.
     */
    @Test
    void getAllIntersections() {
        List<Intersection> inters = new ArrayList<>();
        inters.add(myTestMap.getIntersection(1));
        inters.add(myTestMap.getIntersection(2));
        inters.add(myTestMap.getIntersection(3));
        inters.add(myTestMap.getIntersection(4));
        inters.add(myTestMap.getIntersection(5));
        inters.add(myTestMap.getIntersection(6));
        inters.add(myTestMap.getIntersection(7));
        inters.add(myTestMap.getIntersection(8));
        List<Intersection> testInters = Arrays.asList(myTestMap.getAllIntersections());
        boolean result = inters.containsAll(testInters) && testInters.containsAll(inters);
        assertTrue(result);
    }

    /**
     * Tests if getAllRoads gives the same list as if we manually put the roads into a list.
     */
    @Test
    void getAllRoads() {
        List<Road> roads = new ArrayList<>();
        roads.add(CityMap.getRoad(myTestMap.getIntersection(1), myTestMap.getIntersection(2)));
        roads.add(CityMap.getRoad(myTestMap.getIntersection(1), myTestMap.getIntersection(3)));
        roads.add(CityMap.getRoad(myTestMap.getIntersection(2), myTestMap.getIntersection(4)));
        roads.add(CityMap.getRoad(myTestMap.getIntersection(2), myTestMap.getIntersection(5)));
        roads.add(CityMap.getRoad(myTestMap.getIntersection(3), myTestMap.getIntersection(4)));
        roads.add(CityMap.getRoad(myTestMap.getIntersection(4), myTestMap.getIntersection(5)));
        roads.add(CityMap.getRoad(myTestMap.getIntersection(6), myTestMap.getIntersection(7)));
        List<Road> testRoad = Arrays.asList(myTestMap.getAllRoads());
        boolean result = roads.containsAll(testRoad) && testRoad.containsAll(roads);
        assertTrue(result);
    }

    /**
     * Tests if a map returns false when compared with null
     */
    @Test
    void testEqualsNull() {
        assertNotEquals(null, myTestMap);
    }

    /**
     * Tests if a map returns false when compared with non map object
     */
    @Test
    void testEqualsNonMap() {
        assertNotEquals(myTestMap.getIntersection(1), myTestMap);
    }

    /**
     * Tests if a map returns false when compared with different map
     */
    @Test
    void testEqualsDifferentMap() {
        assertNotEquals(myDefaultMap, myTestMap);
    }

    /**
     * Tests if a map returns true when compared with map initialized on same file.
     */
    @Test
    void testEquals() throws IOException {
        CityMap newMap = new CityMap(Files.readString(Path.of("src/testMap.txt")));
        assertEquals(newMap, myTestMap);
    }

}