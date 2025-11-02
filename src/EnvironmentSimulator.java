import java.util.*;

public class EnvironmentSimulator {
    private final CityMap myMap;
    private final HashMap<Intersection, Conditions> myIntersections = new HashMap<>();
    private final HashMap<Road,Conditions> myRoads = new HashMap<>();
    private static final Conditions defaultCondition = new Conditions(0, 0, 0);
    private static final double LIGHT_BLOCKAGE = 0.333;
    private static final double LIGHT_WEATHER = 0.333;
    private static final double LIGHT_TRAFFIC = 0.333;
    private static final boolean DEBUG_MODE = true;

    public EnvironmentSimulator(CityMap theMap, long theRNGSeed, double theThreshold) {
        this.myMap = theMap;
        simulateConditions(theRNGSeed);
    }

    public Conditions getCondition(Intersection theIntersection) {
        if (myIntersections.containsKey(theIntersection)) {
            return myIntersections.get(theIntersection);
        }
        return defaultCondition;
    }

    public Conditions getCondition(Road theRoad) {
        if (myRoads.containsKey(theRoad)) {
            return myRoads.get(theRoad);
        }
        return defaultCondition;
    }

    // principle of simulating: we randomly set the radius of effects (so if it's rainy in 1 edge, it should be
    // rainy for a few kilometers more)

    private void applyCondition(Intersection inter1, double theWeather, double theBlockage, double theTraffic) {
        Conditions newCon = new Conditions(theWeather, theBlockage, theTraffic);
        myIntersections.put(inter1, newCon);
    }

    private void applyCondition(Road road1, double theWeather, double theBlockage, double theTraffic) {
        Conditions newCon = new Conditions(theWeather, theBlockage, theTraffic);
        myRoads.put(road1, newCon);
    }


    private void simulateConditions(long theRNGSeed) {
        HashMap<Intersection, Double> weatherFactors = new HashMap<>();
        HashMap<Intersection, Double> blockageFactors = new HashMap<>();
        HashMap<Intersection, Double> trafficFactors = new HashMap<>();
        double distance = totalMapDistance(); // we want at last 1/3 of the map to have fairly extreme
        Random rand = new Random(theRNGSeed);

        if (DEBUG_MODE) {
            System.out.println("Total Mileage of Road: " + distance);
            System.out.println("Simulating Weather...");
        }

        // set the approximate distance we want to be affected by each condition, then run a simulation on the map for it

        // set weather parameters
        double distanceAffectedByWeather = distance * rand.nextDouble(.1, .6);
        simulateSingleCondition(rand, distanceAffectedByWeather, LIGHT_WEATHER, 0.7, weatherFactors);
        fillOutCondition(rand, weatherFactors, LIGHT_WEATHER);

        if (DEBUG_MODE) {
            System.out.println("Printing Weather Conditions for all locations and intersections:");
            printMap(weatherFactors);
            System.out.println("Simulating traffic...");
        }

        // set traffic parameters
        double distanceAffectedByTraffic = distance * rand.nextDouble(.1, .6);
        simulateSingleCondition(rand, distanceAffectedByTraffic, LIGHT_TRAFFIC, 0.7, trafficFactors);
        fillOutCondition(rand, trafficFactors, LIGHT_TRAFFIC);

        if (DEBUG_MODE) {
            System.out.println("Printing Traffic Conditions for all locations and intersections:");
            printMap(trafficFactors);
            System.out.println("Simulating obstacles...");
        }

        double distanceAffectedByObstacles = distance * rand.nextDouble(.05, .5);
        simulateSingleCondition(rand, distanceAffectedByObstacles, LIGHT_BLOCKAGE, 0.8, blockageFactors);
        fillOutCondition(rand, blockageFactors, LIGHT_BLOCKAGE);

        if (DEBUG_MODE) {
            System.out.println("Printing Obstacle Conditions for all locations and intersections:");
            printMap(blockageFactors);
            System.out.println("Simulation Completed!");
        }

        setAllConditions(weatherFactors, blockageFactors, trafficFactors);

    }

    private void printMap(HashMap<Intersection, Double> theConditionMap) {
        int counter = 0;
        for (Intersection i: myMap.getAllIntersections()) {
            if ((counter % 3) == 0) {
                System.out.println();
            } else {
                System.out.print(" ");
            }
            System.out.print("(" + i.getID() + ", " + ((double)Math.round(theConditionMap.get(i)*100)/100) + ")");
            counter++;
        }
        System.out.println("\n");
    }

    private double totalMapDistance() {
        double result = 0;
        for (Road r: myMap.getAllRoads()) {
            result += r.getLength();
        }
        return result;
    }

    private void setAllConditions(HashMap<Intersection, Double> theWeather, HashMap<Intersection, Double> theObstacles,
                                  HashMap<Intersection, Double> theTraffic) {
        for (Intersection i: myMap.getAllIntersections()) {
            Conditions conditionList = new Conditions(theWeather.get(i), theObstacles.get(i), theTraffic.get(i));
            myIntersections.put(i, conditionList);
        }
        for (Road r: myMap.getAllRoads()) {
            Intersection source = r.getSource();
            Intersection dest = r.getDestination();
            double weatherFactor = (theWeather.get(source) + theWeather.get(dest))/2;
            double blockageFactor = (theObstacles.get(source) + theObstacles.get(dest))/2;
            double trafficFactor = (theTraffic.get(source) + theTraffic.get(dest))/2;
            Conditions conditionList = new Conditions(weatherFactor, blockageFactor, trafficFactor);
            myRoads.put(r, conditionList);
        }
    }

    // simulate the weather by randomly choosing intersections to start simulating weather
    // then travel a random radius around that intersection (which could be estimated)
    private void simulateSingleCondition(Random theRand, double theDistance, double theConditionOrigin,
                                 double theConditionBound, HashMap<Intersection, Double> ConditionMap) {
        /**
         * idea for process: select random intersection as the epicenter of a weather cluster and a random radius
         * then traverse around the radius until either the limit, which is theDistance, is reached, or we've traversed
         * all of the points using BFS around that road. when traversing to a road, we'll set the weather based on the
         * random weather factor of the road, and if there's conflicts (we've already set the weather factor), then we'll
         * add to the weather factor
         */
        double distanceTraversed = 0;
        Intersection[] interList = myMap.getAllIntersections();
        int problemZonesCreated = 0;

        while (distanceTraversed < theDistance) {
            // set up the epicenter of the condition event, don't set the condition to be too close to 1, it can still get to 1 other ways
            Intersection epicenter = interList[theRand.nextInt(0, interList.length)];
            double epiCondition = theRand.nextDouble(theConditionOrigin, theConditionBound); // mild to pretty severe weather at the center
            // we want to affect everything within this radius
            double radius = theRand.nextDouble(0, theDistance-(distanceTraversed));

            double searchDistance = makeConditionCluster(radius, epicenter, epiCondition, ConditionMap);
            distanceTraversed += searchDistance;
            problemZonesCreated++;
            if (DEBUG_MODE) {
                System.out.println("Epicenter: " + epicenter.getID() + ", " + epiCondition);
            }
        }
        if (DEBUG_MODE) {
            System.out.println("Problem zones created: " + problemZonesCreated);
            System.out.println("Distance Traveled While Simulating Condition: " + distanceTraversed + ", Distance Threshold: " + theDistance);
        }
    }

    private void fillOutCondition(Random theRand, HashMap<Intersection, Double> theMap, double theBound) {
        for (Intersection i : myMap.getAllIntersections()) {
            if (!theMap.containsKey(i)) {
                theMap.put(i, theRand.nextDouble(theBound));
            }
        }
    }

    private void addToCondition(Intersection inter1, double theAmount, HashMap<Intersection, Double> theMap) {
        if (theMap.containsKey(inter1)) {
            theMap.put(inter1, theMap.get(inter1) * (1 + theAmount));
            if (theMap.get(inter1) >= 1) {
                theMap.put(inter1, 1.0);
            }
        } else {
            theMap.put(inter1, theAmount);
        }
    }

    private double makeConditionCluster(double theRadius, Intersection theOrigin, double theOriginCondition,
                                       HashMap<Intersection, Double> theConditionMap) {
        // set up our bfs from the origin
        Queue<Intersection> bfsQueue =  new LinkedList<>();
        HashMap<Intersection, double[]> distances = new HashMap<>();
        HashMap<Intersection, Intersection> visited = new HashMap<>(); // <inter1, theprevious>
        double searchDistance = 0;
        distances.put(theOrigin, new double[] {0, 0, 0, 0});
        bfsQueue.add(theOrigin);
        visited.put(theOrigin, null);

        while (!bfsQueue.isEmpty() && searchDistance < theRadius) {
            Intersection currEdge = bfsQueue.poll();
            double totalDistance = distanceFromOrigin(distances.get(currEdge));
            // the line of code below could lead to a negative number, so set it to the absolute value
            double decay  = Math.abs(1.0 - distanceFromOrigin(distances.get(currEdge))/theRadius);
            addToCondition(currEdge, decay * theOriginCondition, theConditionMap);
            if (visited.get(currEdge) != null) {
                searchDistance += myMap.getRoad(currEdge, visited.get(currEdge)).getLength();
            }

            if (withinDistance(totalDistance, theRadius)) {
                // mark the visited nodes
                for (Road r : currEdge.getRoadList()) { // get the intersections this is connected to
                    Intersection nonOriginNode;
                    if (!r.getSource().equals(currEdge)) { // get whichever intersection in the road isn't our current
                        nonOriginNode = r.getSource();
                    } else {
                        nonOriginNode = r.getDestination();
                    }
                    visited.put(nonOriginNode, currEdge);
                    distances.put(nonOriginNode, addCartesianDistances(currEdge, nonOriginNode, distances.get(currEdge)));
                    bfsQueue.add(nonOriginNode);
                }
            } // if we're out of bounds, don't add the adjacent nodes
        }
        return searchDistance;
    }


    // get the distance matrix [North, South, East, West] of an intersection using previous distance matrix
    private double[] addCartesianDistances(Intersection theFrom, Intersection theTo, double[] distances) {
        Road r = myMap.getRoad(theFrom, theTo);
        double[] result = distances.clone();
        switch (r.getDirection(theFrom)) {
            case NORTH: result[0] += r.getLength();
                break;
            case SOUTH: result[1] += r.getLength();
                break;
            case EAST: result[2] += r.getLength();
                break;
            case WEST: result[3] += r.getLength();
                break;
        }
        return result;
    }

    private double distanceFromOrigin(double[] theDistances) {
        double theX = Math.abs(theDistances[0] - theDistances[1]);
        double theY = Math.abs(theDistances[2] - theDistances[3]);
        return Math.sqrt((theX*theX) + (theY*theY));
    }

    boolean withinDistance(double theDistance, double theRadius) {
        return theDistance <= theRadius;
    }
}
