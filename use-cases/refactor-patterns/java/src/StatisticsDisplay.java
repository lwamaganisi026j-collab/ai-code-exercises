public class StatisticsDisplay implements WeatherObserver {
    private String lastMessage = "";

    @Override
    public void update(float temperature, float humidity, float pressure) {
        lastMessage = String.format("Weather statistics: Avg/Max/Min temperature = %.1f/%.1f/%.1f",
                temperature - 2, temperature + 2, temperature - 5);
    }

    public String getLastMessage() { return lastMessage; }
}
