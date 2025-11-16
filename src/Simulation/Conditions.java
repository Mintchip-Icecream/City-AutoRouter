package Simulation;

/**
 * Data class that contains the risk levels of certain road or intersection conditions from 0 (representing no risk)
 * to 100 (representing very high risk in specific condition). Current implemented conditions are the weather risk,
 * blockage/obstacle risk, and the traffic density/risk.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class Conditions {
    private final double weatherFactor;
    private final double blockageSeverity;
    private final double trafficDensity;

    public Conditions(final double theWeatherRisk, final double theBlockageRisk, final double theTrafficRisk) {
        if (theWeatherRisk > 1.0 || theWeatherRisk < 0) {
            throw new IllegalArgumentException("All input data must be between 0 to 1");
        } else if (theBlockageRisk > 1.0 || theBlockageRisk < 0) {
            throw new IllegalArgumentException("All input data must be between 0 to 1");
        } else if (theTrafficRisk > 1.0 || theTrafficRisk < 0) {
            throw new IllegalArgumentException("All input data must be between 0 to 1");
        }
        this.weatherFactor = theWeatherRisk;
        this.blockageSeverity = theBlockageRisk;
        this.trafficDensity = theTrafficRisk;
    }

    public final double getObstacleSeverity() {
        return blockageSeverity;
    }

    public final double getTrafficDensity() {
        return trafficDensity;
    }

    public final double getWeatherFactor() {

        return weatherFactor;
    }
}
