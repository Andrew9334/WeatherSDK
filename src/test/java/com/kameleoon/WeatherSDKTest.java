package com.kameleoon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kameleoon.exception.CityNotFoundException;
import com.kameleoon.exception.InvalidApiKeyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeatherSDKTest {

    @AfterEach
    public void tearDown() {
        WeatherSDK.removeInstance("mock-api-key");
    }

    @Test
    public void testGetWeather_CorrectData() throws IOException {
        WeatherSDK sdk = mock(WeatherSDK.class);

        String jsonResponse = "{ \"weather\": [{ \"main\": \"Clouds\", \"description\": \"scattered clouds\" }], \"main\": { \"temp\": 280.32, \"feels_like\": 278.5 } }";
        WeatherData mockData = new WeatherData(new ObjectMapper().readTree(jsonResponse));

        when(sdk.getWeather("London")).thenReturn(mockData);

        WeatherData result = sdk.getWeather("London");
        assertNotNull(result);
        assertEquals("Weather: Clouds, Temp: 7.17°C, Feels Like: 5.35°C", result.toString());
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

}
