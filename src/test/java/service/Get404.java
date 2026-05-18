package service;

import entities.BaseClass;
import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;

public class Get404 extends BaseClass {

    @Test
    public void nonExistingUrlReturns404() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/nonexistingurl");
        addTokenAuth(get);

        response = client.execute(get);

        int actualStatus = response.getStatusLine().getStatusCode();
        assertEquals(actualStatus, 404);
    }
}
