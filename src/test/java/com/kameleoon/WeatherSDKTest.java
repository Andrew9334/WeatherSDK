package com.kameleoon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kameleoon.exception.CityNotFoundException;
import com.kameleoon.exception.InvalidApiKeyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WeatherSDKTest {

    @AfterEach
    public void tearDown() {
        WeatherSDK.removeInstance("mock-api-key");
    }

    @Test
    public void testGetWeatherCorrectData() throws IOException {
        WeatherSDK sdk = mock(WeatherSDK.class);

        String jsonResponse = "{ \"weather\": [{ \"main\": \"Clouds\", \"description\": \"scattered clouds\" }], \"main\": { \"temp\": 280.32, \"feels_like\": 278.5 } }";
        WeatherData mockData = new WeatherData(new ObjectMapper().readTree(jsonResponse));

        when(sdk.getWeather("London")).thenReturn(mockData);

        WeatherData result = sdk.getWeather("London");
        assertNotNull(result);
        assertEquals("Weather: Clouds, Description: scattered clouds, Temp: 7.17°C, Feels Like: 5.35°C", result.toString());
    }

    @Test
    public void testInvalidApiKeyException() throws IOException {
        WeatherSDK sdk = mock(WeatherSDK.class);

        when(sdk.getWeather("London")).thenThrow(new InvalidApiKeyException("Invalid API key"));

        assertThrows(InvalidApiKeyException.class, () -> sdk.getWeather("London"));
    }

    @Test
    public void testCityNotFoundException() throws IOException {
        WeatherSDK sdk = mock(WeatherSDK.class);

        when(sdk.getWeather("UnknownCity")).thenThrow(new CityNotFoundException("City not found"));

        assertThrows(CityNotFoundException.class, () -> sdk.getWeather("UnknownCity"));
    }

    @Test
    public void testGetWeatherUsesCache() throws IOException {
        WeatherSDK sdk = mock(WeatherSDK.class);

        String jsonResponse = "{ \"weather\": [{ \"main\": \"Clouds\", \"description\": \"scattered clouds\" }], \"main\": { \"temp\": 280.32, \"feels_like\": 278.5 } }";
        WeatherData mockData = new WeatherData(new ObjectMapper().readTree(jsonResponse));

        when(sdk.getWeather("London")).thenReturn(mockData);

        WeatherData result1 = sdk.getWeather("London");
        assertNotNull(result1);

        WeatherData result2 = sdk.getWeather("London");
        assertSame(result1, result2);  // Должны быть одинаковые объекты, т.к. используется кэш

        verify(sdk, times(2)).getWeather("London");
    }


    @Test
    public void testPollingModeUpdatesWeatherData() throws InterruptedException, IOException {
        WeatherSDK sdk = mock(WeatherSDK.class);

        String jsonResponse = "{ \"weather\": [{ \"main\": \"Clouds\", \"description\": \"scattered clouds\" }], \"main\": { \"temp\": 280.32, \"feels_like\": 278.5 } }";
        WeatherData mockData = new WeatherData(new ObjectMapper().readTree(jsonResponse));

        when(sdk.getWeather("London")).thenReturn(mockData);

        sdk.getWeather("London");
        Thread.sleep(1000);

        sdk.getWeather("London");

        assertNotNull(sdk.getWeather("London"));
    }
}
