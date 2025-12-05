package Simulation;

import java.util.Objects;

/**
 * Data class that contains the risk levels of certain road or intersection conditions from 0 (representing no risk)
 * to 100 (representing very high risk in specific condition). Current implemented conditions are the weather risk,
 * blockage/obstacle risk, and the traffic density/risk.
 *
 * @author June Flores
 * @version 11/15/25
 */
public class Conditions {
    /**
     * A double between 0 and 1 representing the percentage risk of the weather, or how "bad" the weather is.
     */
    private final double weatherFactor;
    /**
     * A double between 0 and 1 representing the obstacle risk, or how much the road/intersection is blocked.
     */
    private final double blockageSeverity;
    /**
     * A double between 0 and 1 representing the percentage risk of the traffic, or how "bad" the traffic is.
     */
    private final double trafficDensity;


    /**
     * Constructs a condition, with each condition being represented by a number between 0-1 measure level of risk.
     *
     * @param theWeatherRisk A number between 0 and 1 representing the risk of the weather.
     * @param theBlockageRisk  A number between 0 and 1 representing the risk of the obstacles.
     * @param theTrafficRisk A number between 0 and 1 representing the risk of the traffic.
     */
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

    /**
     * Returns the severity of the obstacles on a scale between 0 and 1.
     *
     * @return the severity of the obstacles on a scale between 0 and 1.
     */
    public final double getObstacleSeverity() {
        return blockageSeverity;
    }

    /**
     * Returns the severity of the traffic on a scale between 0 and 1.
     *
     * @return the severity of the traffic on a scale between 0 and 1.
     */
    public final double getTrafficDensity() {
        return trafficDensity;
    }

    /**
     * Returns the severity of the weather on a scale between 0 and 1.
     *
     * @return the severity of the weather on a scale between 0 and 1.
     */
    public final double getWeatherFactor() {

        return weatherFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Conditions that = (Conditions) o;
        return Double.compare(weatherFactor, that.weatherFactor) == 0 && Double.compare(blockageSeverity, that.blockageSeverity) == 0 && Double.compare(trafficDensity, that.trafficDensity) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(weatherFactor, blockageSeverity, trafficDensity);
    }
}
