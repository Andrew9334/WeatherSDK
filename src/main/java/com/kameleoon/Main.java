package com.kameleoon;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            WeatherSDK sdk = WeatherSDK.getInstance("36f3011ccd5f433c43123f97049603cc", false);
            sdk.getWeather("London");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
