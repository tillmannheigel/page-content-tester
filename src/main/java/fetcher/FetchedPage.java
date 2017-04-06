package fetcher;

import static fetcher.FetchedPage.DeviceType.DESKTOP;
import static fetcher.FetchedPage.DeviceType.MOBILE;
import static org.jsoup.Connection.Method;
import static org.jsoup.Connection.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.nodes.Document;

import configurations.Config;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FetchedPage {

    public enum DeviceType {
        DESKTOP,
        MOBILE
    }

    private static Config config = new Config();

    private static final Map<CacheKey, FetchedPage> fetchedPageCache = new ConcurrentHashMap<>();

    public static FetchedPage fetchPage(String url) {
        return fetchedPages(url, Method.GET, Collections.emptyMap(), false, DESKTOP);
    }

    public static FetchedPage fetchPageAsMobileDevice(String url) {
        return fetchedPages(url, Method.GET, Collections.emptyMap(), true, MOBILE);
    }

    public static FetchedPage performAjaxRequest(String url, Method method, Map<String, String> data) {
        return fetchedPages(url, method, data, false, DESKTOP);
    }

    @SneakyThrows
    private static FetchedPage fetchedPages(String urlToFetch, Method method, Map<String, String> data, boolean mobile, DeviceType device) {
        CacheKey cacheKey = new CacheKey(urlToFetch, device);
        if (fetchedPageCache.containsKey(cacheKey)) {
            log.warn("duplicate call for fetched page: {}\n\t{}", cacheKey, Thread.currentThread().getStackTrace()[3]);
            return fetchedPageCache.get(cacheKey);
        } else {
            Fetcher fetcher = Fetcher.builder().deviceType(device).method(method).data(data).build();
            FetchedPage fetchedPage = new FetchedPage(urlToFetch, fetcher.fetch(urlToFetch), mobile);
            fetchedPageCache.put(cacheKey, fetchedPage);
            return fetchedPage;
        }
    }

    @Value
    @AllArgsConstructor
    private static class CacheKey {
        String url;
        DeviceType device;
    }

    private final String url;
    private final boolean mobile;
    private final Response response;

    private Optional<Document> document = Optional.empty();

    private FetchedPage(String url, Response response, boolean mobile) {
        this.url = url;
        this.response = response;
        this.mobile = mobile;
    }

    public synchronized Document getDocument() {
        if (!document.isPresent()) {
            try {
                document = Optional.of(response.parse());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return document.get();
    }

    public String getUrl() {
        return url;
    }

    public int getStatusCode() {
        return response.statusCode();
    }

    public boolean isMobile() {
        return mobile;
    }

    public String getCookieValue(String cookieName) {
        return response.cookie(cookieName);
    }

    public String getContentType() {
        return response.contentType();
    }

    public String getPageBody() {
        return response.body();
    }

    public String getCustomHeader(String header) {
        return response.header(header);
    }

    public Map<String, String> getHeaders() {
        return response.headers();
    }

    public Map<String, String> getCookies() {
        return response.cookies();
    }

    public String getStatusMessage() {
        return response.statusMessage();
    }

    public boolean hasCookie(String cookieName) {
        return response.hasCookie(cookieName);
    }

    public boolean hasHeader(String header) {
        return response.hasHeader(header);
    }

    public String getReferrer() {
        return config.getReferrer();
    }
}