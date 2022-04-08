package com.example.project.Utilities;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void loadConfiguration() throws IOException {
        // checking if configuration is being loaded successfully
        assertTrue(Utils.loadConfiguration());
    }
}