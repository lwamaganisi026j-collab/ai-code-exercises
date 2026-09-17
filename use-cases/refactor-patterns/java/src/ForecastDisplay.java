public class ForecastDisplay implements WeatherObserver {
    private String lastMessage = "";

    @Override
    public void update(float temperature, float humidity, float pressure) {
        String prediction = pressure < 29.92f
                ? "Watch out for cooler, rainy weather"
                : "Improving weather on the way!";
        lastMessage = "Forecast: " + prediction;
    }

    public String getLastMessage() { return lastMessage; }
}
