package service;

import dto.NotFound;
import dto.RateLimit;
import dto.User;
import entities.BaseClass;
import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.Test;
import utils.ResponseUtils;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class BodyTestWithSimpleMap extends BaseClass {

    @Test
    public void returnsCorrectLogin() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/users/olgadarii");
        addTokenAuth(get);

        response = client.execute(get);

        User user = ResponseUtils.unmarshall(response, User.class);
        assertEquals(user.getLogin(), "olgadarii");
    }

    @Test
    public void returnsCorrectId() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/users/olgadarii");
        addTokenAuth(get);

        response = client.execute(get);

        User user = ResponseUtils.unmarshallGeneric(response, User.class);
        assertEquals(user.getId(), 29649900);
    }

    @Test
    public void notFoundMessageIsCorrect() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/nonexistingendpoint");
        addTokenAuth(get);

        response = client.execute(get);

        NotFound notFoundMessage = ResponseUtils.unmarshallGeneric(response, NotFound.class);
        assertEquals(notFoundMessage.getMessage(), "Not Found");
    }

    @Test
    public void correctRateLimitsAreSet() throws IOException {
        HttpGet get = createGetRequest(BASE_ENDPOINT + "/rate_limit");

        response = client.execute(get);

        RateLimit rateLimits = ResponseUtils.unmarshallGeneric(response, RateLimit.class);
        assertEquals(rateLimits.getCoreLimit(), 60);
        assertEquals(rateLimits.getSearchLimit(), "10");
    }
}
