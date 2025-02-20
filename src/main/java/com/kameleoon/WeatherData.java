package com.kameleoon;

import com.fasterxml.jackson.databind.JsonNode;

public class WeatherData {
    private final String main;
    private final String description;
    private final double temp;
    private final double feelsLike;

    public WeatherData(JsonNode jsonNode) {
        this.main = jsonNode.get("weather").get(0).get("main").asText();
        this.description = jsonNode.get("weather").get(0).get("description").asText();
        this.temp = jsonNode.get("main").get("temp").asDouble();
        this.feelsLike = jsonNode.get("main").get("feels_like").asDouble();
    }

    @Override
    public String toString() {
        return String.format("Weather: %s, Temp: %.2f°C, Feels Like: %.2f°C", main, temp - 273.15, feelsLike - 273.15);
    }
}
