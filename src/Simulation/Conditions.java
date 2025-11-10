package Simulation;

public class Conditions {
    private final double weatherFactor;
    private final double blockageSeverity;
    private final double trafficDensity;

    public Conditions(double weatherRisk, double blockageRisk, double trafficDensity) {
        if (weatherRisk > 1.0 || weatherRisk < 0) {
            throw new IllegalArgumentException("All input dataw must be between 0 to 1");
        } else if (blockageRisk > 1.0 || blockageRisk < 0) {
            throw new IllegalArgumentException("All input datab must be between 0 to 1");
        } else if (trafficDensity > 1.0 || trafficDensity < 0) {
            throw new IllegalArgumentException("All input datal must be between 0 to 1");
        }
        this.weatherFactor = weatherRisk;
        this.blockageSeverity = blockageRisk;
        this.trafficDensity = trafficDensity;
    }

    public double getObstacleSeverity() {
        return blockageSeverity;
    }

    public double getTrafficDensity() {
        return trafficDensity;
    }

    public double getWeatherFactor() {
        return weatherFactor;
    }
}
