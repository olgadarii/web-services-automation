package entities;

import org.apache.http.client.methods.*;

public interface ApiClient {

    HttpGet createGetRequest(String url);

    HttpPost createPostRequest(String url, String jsonBody);

    HttpDelete createDeleteRequest(String url);

    HttpOptions createOptionsRequest(String url);

    HttpPut createPutRequest(String url, String jsonBody);

    void addTokenAuth(HttpRequestBase request);
}
