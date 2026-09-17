public class HeatIndexDisplay implements WeatherObserver {
    private String lastMessage = "";

    @Override
    public void update(float temperature, float humidity, float pressure) {
        float heatIndex = (temperature + humidity) / 2;
        lastMessage = String.format("Heat index: %.1f", heatIndex);
    }

    public String getLastMessage() { return lastMessage; }
}
