package com.incountry.residence.sdk.http;

import com.incountry.residence.sdk.tools.dao.impl.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApiResponseTest {

    @Test
    public void createApiResponseTest() {
        boolean error = false;
        boolean ignore = false;
        boolean canRetry = false;
        ApiResponse response = new ApiResponse(error, ignore, canRetry);
        assertNotNull(response);
        assertEquals(error, response.isError());
        assertEquals(ignore, response.isIgnored());
        assertEquals(canRetry, response.isCanRetry());
        assertEquals("ApiResponse{error=false, ignored=false, canRetry=false}", response.toString());

        error = true;
        ignore = true;
        canRetry = true;
        response = new ApiResponse(error, ignore, canRetry);
        assertNotNull(response);
        assertEquals(error, response.isError());
        assertEquals(ignore, response.isIgnored());
        assertEquals(canRetry, response.isCanRetry());
        assertEquals("ApiResponse{error=true, ignored=true, canRetry=true}", response.toString());
    }
}
