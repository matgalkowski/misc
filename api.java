import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class api {

    private static final String BASE_URL = "https://api.github.com";
    CloseableHttpClient client;
    CloseableHttpResponse response;

    @DataProvider
    private Object[][] endpoints() {
        return new Object[][]{
                {"/user"},
                {"/user/followers"},
                {"/notifications"}
        };
    }

    @BeforeMethod
    private void setup() {
        client = HttpClientBuilder.create().build();
    }

    @AfterMethod
    private void teardown() throws IOException {
        client.close();
        response.close();
    }

    @Test(dataProvider = "endpoints")
    private void checkStatusCode(String endpoint) throws IOException {
        HttpGet get = new HttpGet(BASE_URL + endpoint);
        response = client.execute(get);
        int status = response.getStatusLine().getStatusCode();

        Assert.assertEquals(status, 401);
    }

    @Test
    private void checkContentType() throws IOException {
        HttpGet get = new HttpGet(BASE_URL);
        response = client.execute(get);

        //Check whole content type
        Header contentType = response.getEntity().getContentType();
        Assert.assertEquals(contentType.getValue(), "application/json; charset=utf-8");

        //Check media type only
        ContentType ct = ContentType.getOrDefault(response.getEntity());
        Assert.assertEquals(ct.getMimeType(), "application/json");
    }

    @Test
    private void checkHeader() throws IOException {
        HttpGet get = new HttpGet(BASE_URL);
        response = client.execute(get);

        String headerValue = getHeader(response, "Server");

        Assert.assertEquals(headerValue, "GitHub.com");
    }

    @Test
    private void checkHeaderPresence() throws IOException {
        HttpGet get = new HttpGet(BASE_URL);
        response = client.execute(get);

        Assert.assertEquals(isHeaderPresent(response, "ETag"), true);
    }

    private static String getHeader(CloseableHttpResponse response, String headerName) {
        Header[] headers = response.getAllHeaders();
        List<Header> httpHeaders = Arrays.asList(headers);

        Header matchedHeader = httpHeaders.stream()
                                .filter(header -> headerName.equalsIgnoreCase(header.getName()))
                                .findFirst().orElseThrow(() -> new RuntimeException("Header not found: " + headerName));

        return matchedHeader.getValue();
    }

    private static boolean isHeaderPresent(CloseableHttpResponse response, String headerName) {
        List<Header> httpHeaders = Arrays.asList(response.getAllHeaders());

        return httpHeaders.stream().anyMatch((header -> header.getName().equalsIgnoreCase(headerName)));
    }

}
