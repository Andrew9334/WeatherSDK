package com.kameleoon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kameleoon.exception.CityNotFoundException;
import com.kameleoon.exception.InvalidApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {
    private static final Logger logger = LoggerFactory.getLogger(WeatherSDK.class);

    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private final String apiKey;
    private final boolean pollingMode;
    private static WeatherSDK instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public WeatherSDK(String apiKey, boolean pollingMode) {
        this.apiKey = apiKey;
        this.pollingMode = pollingMode;
        logger.info("WeatherSDK initialized with API key: {}", apiKey);

        if (pollingMode) {
            scheduler.scheduleAtFixedRate(this::updateAllCities, 0, 10, TimeUnit.MINUTES);
            logger.info("Polling mode enabled, updating every 10 minutes.");
        }
    }

    public static WeatherSDK getInstance(String apiKey, boolean pollingMode) {
        logger.info("Creating new WeatherSDK instance...");
        return new WeatherSDK(apiKey, pollingMode);
    }

    public WeatherData getWeather(String city) throws IOException {
        logger.info("Fetching weather for city: {}", city);
        WeatherData data = fetchWeather(city);
        logger.info("Weather data received: {}", data);
        return data;
    }

    public static void removeInstance(String apiKey) {
        if (instance != null && instance.apiKey.equals(apiKey)) {
            logger.info("Removing instance with API key: {}", apiKey);
            instance = null;
        }
    }

    private WeatherData fetchWeather(String city) throws IOException {
        String urlStr = String.format(API_URL, city, apiKey);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        logger.info("Received HTTP response code: {}", responseCode);

        if (responseCode == 401) {
            logger.error("Invalid API key!");
            throw new InvalidApiKeyException("Invalid API key.");
        }
        if (responseCode == 404) {
            logger.error("City not found: {}", city);
            throw new CityNotFoundException("City not found");
        }
        if (responseCode != 200) {
            logger.error("Unexpected API error: {}", conn.getResponseMessage());
            throw new IOException("Failed to fetch weather: " + conn.getResponseMessage());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(conn.getInputStream());
        return new WeatherData(jsonNode);
    }

    private void updateAllCities() {
        logger.info("Updating weather for all cached cities...");
    }
}
