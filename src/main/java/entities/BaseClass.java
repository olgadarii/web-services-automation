package entities;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BaseClass {

    private static final Logger logger = LogManager.getLogger(BaseClass.class);
    private static final Properties config = loadConfig();

    protected static final String BASE_ENDPOINT = config.getProperty("base_url", "https://api.github.com");
    protected static final String TOKEN = config.getProperty("token", "");

    protected CloseableHttpClient client;
    protected CloseableHttpResponse response;

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = BaseClass.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
        return props;
    }

    protected static String getConfigProperty(String key) {
        return config.getProperty(key, "");
    }

    @BeforeTest
    public void suiteSetup() {
        logger.info("Test suite starting — base endpoint: " + BASE_ENDPOINT);
    }

    @AfterTest
    public void suiteTeardown() {
        logger.info("Test suite finished");
    }

    @BeforeMethod
    public void setup() {
        client = HttpClientBuilder.create().build();
        logger.info("HTTP client initialized");
    }

    @AfterMethod
    public void closeResources() throws IOException {
        if (response != null) {
            response.close();
        }
        if (client != null) {
            client.close();
        }
        logger.info("HTTP client and response closed");
    }

    protected HttpGet createGetRequest(String url) {
        logger.info("Creating GET request to: " + url);
        return new HttpGet(url);
    }

    protected HttpPost createPostRequest(String url, String jsonBody) {
        logger.info("Creating POST request to: " + url);
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        return request;
    }

    protected HttpDelete createDeleteRequest(String url) {
        logger.info("Creating DELETE request to: " + url);
        return new HttpDelete(url);
    }

    protected HttpOptions createOptionsRequest(String url) {
        logger.info("Creating OPTIONS request to: " + url);
        return new HttpOptions(url);
    }

    protected HttpPut createPutRequest(String url, String jsonBody) {
        logger.info("Creating PUT request to: " + url);
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        return request;
    }

    protected void addTokenAuth(HttpRequestBase request) {
        request.setHeader(HttpHeaders.AUTHORIZATION, "token " + TOKEN);
    }

    protected static void assertEqualsIgnoreCase(String actual, String expected) {
        Assert.assertTrue(
            actual != null && actual.equalsIgnoreCase(expected),
            "Expected (ignore case): '" + expected + "' but got: '" + actual + "'"
        );
    }
}
