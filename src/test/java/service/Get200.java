package service;

import entities.BaseClass;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;

public class Get200 extends BaseClass {

    private static final Logger logger = LogManager.getLogger(Get200.class);

    @Test
    public void baseUrlReturns200() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT);
        addTokenAuth(get);
        logger.info("Executing GET request to " + BASE_ENDPOINT);

        response = client.execute(get);

        int actualStatus = response.getStatusLine().getStatusCode();
        assertEquals(actualStatus, 200);
    }

    @Test
    public void rateLimitReturns200() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/rate_limit");

        response = client.execute(get);

        int actualStatus = response.getStatusLine().getStatusCode();
        assertEquals(actualStatus, 200);
    }

    @Test
    public void repositorySearchUrlReturns200() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/search/repositories?q=java");

        response = client.execute(get);

        int actualStatus = response.getStatusLine().getStatusCode();
        assertEquals(actualStatus, 200);
    }
}
