public class WeatherStationSmokeTest {
    public static void main(String[] args) {
        WeatherStation station = new WeatherStation();
        CurrentConditionsDisplay current = new CurrentConditionsDisplay();
        ForecastDisplay forecast = new ForecastDisplay();

        station.addObserver(current);
        station.addObserver(forecast);
        station.addObserver(current);
        station.setMeasurements(75.0f, 60.0f, 29.5f);

        check("Current conditions: 75.0°F, 60.0% humidity".equals(current.getLastMessage()), "current observer not notified");
        check("Forecast: Watch out for cooler, rainy weather".equals(forecast.getLastMessage()), "forecast observer not notified");

        station.removeObserver(current);
        station.setMeasurements(80.0f, 70.0f, 30.5f);
        check("Current conditions: 75.0°F, 60.0% humidity".equals(current.getLastMessage()), "removed observer was notified");

        System.out.println("WeatherStation Observer smoke tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
