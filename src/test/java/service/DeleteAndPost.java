package service;

import entities.BaseClass;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DeleteAndPost extends BaseClass {

    @Test(priority = 0)
    public void createRepoReturns201() throws IOException {
        HttpPost request = createPostRequest(
                BASE_ENDPOINT + "/user/repos",
                "{\"name\": \"hello-world\"}"
        );

        String username = getConfigProperty("username");
        String password = getConfigProperty("password");
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + TOKEN);

        response = client.execute(request);

        int actualStatusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(actualStatusCode, 201);
    }

    @Test(priority = 10)
    public void deleteIsSuccessful() throws IOException {
        HttpDelete request = createDeleteRequest(BASE_ENDPOINT + "/repos/olgadarii/hello-world");
        addTokenAuth(request);

        response = client.execute(request);

        int actualStatusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(actualStatusCode, 204);
    }
}
