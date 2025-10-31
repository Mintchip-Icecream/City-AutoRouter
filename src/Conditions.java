public class Conditions {
    private double weatherFactor;
    private double blockageSeverity;
    private double trafficDensity;

    public Conditions(double weatherRisk, double blockageRisk, double trafficDensity) {
        if (weatherRisk > 1 || weatherRisk < 0) {
            throw new IllegalArgumentException("All input data must be between 0 to 1");
        } else if (blockageRisk > 1 || blockageRisk < 0) {
            throw new IllegalArgumentException("All input data must be between 0 to 1");
        } else if (trafficDensity > 1 || trafficDensity < 0) {
            throw new IllegalArgumentException("All input data must be between 0 to 1");
        }
        this.weatherFactor = weatherRisk;
        this.blockageSeverity = blockageRisk;
        this.trafficDensity = trafficDensity;
    }

    public double getBlockageSeverity() {
        return blockageSeverity;
    }

    public double getTrafficDensity() {
        return trafficDensity;
    }

    public double getWeatherFactor() {
        return weatherFactor;
    }
    public double getSafetyRisk() {
        return (weatherFactor/2) + (blockageSeverity/2);
    }
    public double getTimeDelay() {
        return (weatherFactor/2) + (trafficDensity/2);
    }
}
