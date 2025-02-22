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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {
    private static final Logger logger = LoggerFactory.getLogger(WeatherSDK.class);

    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private final String apiKey;
    private static WeatherSDK instance;

    private final Map<String, WeatherData> cityCache = new LinkedHashMap<>(10, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, WeatherData> eldest) {
            return size() > 10;
        }
    };

    public WeatherSDK(String apiKey, boolean pollingMode) {
        this.apiKey = apiKey;
        logger.info("WeatherSDK initialized with API key: {}", apiKey);

        if (pollingMode) {
            startPolling();
        }
    }

    public static WeatherSDK getInstance(String apiKey, boolean pollingMode) {
        if (instance == null || !instance.apiKey.equals(apiKey)) {
            instance = new WeatherSDK(apiKey, pollingMode);
        }
        logger.info("Creating new WeatherSDK instance...");
        return instance;
    }

    public WeatherData getWeather(String city) throws IOException {
        logger.info("Fetching weather for city: {}", city);

        if (cityCache.containsKey(city)) {
            logger.info("Returning cached weather data for city: {}", city);
            return cityCache.get(city);
        }

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
        WeatherData weatherData = new WeatherData(jsonNode);

        cityCache.put(city, weatherData);

        return weatherData;
    }

    private void startPolling() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Polling mode: Updating weather for all cities...");
                Set<String> cities = cityCache.keySet();
                for (String city : cities) {
                    fetchWeather(city);
                }
            } catch (IOException e) {
                logger.error("Error during polling: {}", e.getMessage());
            }
        }, 0, 10, TimeUnit.MINUTES);
    }
}
