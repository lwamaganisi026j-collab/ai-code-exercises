import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherStationTest {
    @Test
    void notifiesRegisteredObservers() {
        WeatherStation station = new WeatherStation();
        CurrentConditionsDisplay current = new CurrentConditionsDisplay();
        ForecastDisplay forecast = new ForecastDisplay();

        station.addObserver(current);
        station.addObserver(forecast);
        station.setMeasurements(75.0f, 60.0f, 29.5f);

        assertEquals("Current conditions: 75.0°F, 60.0% humidity", current.getLastMessage());
        assertEquals("Forecast: Watch out for cooler, rainy weather", forecast.getLastMessage());
    }

    @Test
    void removingObserverStopsNotifications() {
        WeatherStation station = new WeatherStation();
        CurrentConditionsDisplay current = new CurrentConditionsDisplay();

        station.addObserver(current);
        station.setMeasurements(75.0f, 60.0f, 30.5f);
        station.removeObserver(current);
        station.setMeasurements(80.0f, 70.0f, 29.0f);

        assertEquals("Current conditions: 75.0°F, 60.0% humidity", current.getLastMessage());
    }

    @Test
    void duplicateObserverIsRegisteredOnlyOnce() {
        WeatherStation station = new WeatherStation();
        CurrentConditionsDisplay current = new CurrentConditionsDisplay();

        station.addObserver(current);
        station.addObserver(current);
        station.setMeasurements(82.0f, 70.0f, 29.2f);

        assertEquals("Current conditions: 82.0°F, 70.0% humidity", current.getLastMessage());
    }
}
