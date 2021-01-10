package com.cgi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomOIDCProtocolMapperTest {

    @Test
    public void retrieveAttribute() {

        String att = new CustomOIDCProtocolMapper().retrieveAttribute(
                "https://httpbin.org/get",
                "url");

        assertEquals("https://httpbin.org/get", att);
    }

}