package service;

import entities.BaseClass;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.testng.annotations.Test;
import utils.ResponseUtils;

import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class ResponseHeaders extends BaseClass {

    @Test
    public void contentTypeIsJson() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT);

        response = client.execute(get);

        Header contentType = response.getEntity().getContentType();
        assertEquals(contentType.getValue(), "application/json; charset=utf-8");

        ContentType ct = ContentType.getOrDefault(response.getEntity());
        assertEquals(ct.getMimeType(), "application/json");
    }

    @Test
    public void serverIsGithub() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT);
        addTokenAuth(get);

        response = client.execute(get);

        String headerValue = ResponseUtils.getHeader(response, "Server");
        assertEqualsIgnoreCase(headerValue, "GitHub.com");
    }

    @Test
    public void xRateLimitIsSixty() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT);

        response = client.execute(get);

        String limitVal = ResponseUtils.getHeaderJava8Way(response, "X-RateLimit-Limit");
        assertEquals(limitVal, "60");
    }

    @Test
    public void headerIsPresent() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT);
        addTokenAuth(get);

        response = client.execute(get);

        Boolean headerIsPresent = ResponseUtils.headerIsPresent(response, "ETag");
        assertTrue(headerIsPresent);
    }
}
