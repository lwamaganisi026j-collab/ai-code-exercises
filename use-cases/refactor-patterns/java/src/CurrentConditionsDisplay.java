public class CurrentConditionsDisplay implements WeatherObserver {
    private String lastMessage = "";

    @Override
    public void update(float temperature, float humidity, float pressure) {
        lastMessage = String.format("Current conditions: %.1f°F, %.1f%% humidity", temperature, humidity);
    }

    public String getLastMessage() { return lastMessage; }
}
