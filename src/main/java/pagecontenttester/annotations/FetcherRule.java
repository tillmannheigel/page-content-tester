package pagecontenttester.annotations;

import static pagecontenttester.fetcher.FetchedPage.annotationCall;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import pagecontenttester.configurations.Config;
import pagecontenttester.fetcher.FetchedPage;
import pagecontenttester.fetcher.FetchedPage.DeviceType;

public class FetcherRule implements MethodRule {

    private FetchedPage fetchedPage;
    private List<FetchedPage> fetchedPages = new ArrayList<>();
    private Config config = new Config();
    private Map<String, String> cookie = new HashMap<>();

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {

                List<Annotation> annotations = new LinkedList<>();
                annotations.addAll(Arrays.asList(method.getMethod().getDeclaringClass().getAnnotations()));
                annotations.addAll(Arrays.asList(method.getAnnotations()));
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Fetch) {
                        Fetch fetchPage = (Fetch) annotation;

                        String url = fetchPage.url();

                        Method method = fetchPage.method();
                        DeviceType device = fetchPage.device();
                        String referrer = getReferrer(fetchPage);
                        int timeout = getTimeout(fetchPage);
                        int retriesOnTimeout = getRetryCount(fetchPage);
                        Cookie[] cookieAnnotation = fetchPage.setCookies();
                        cookie = getCookies(cookieAnnotation);
                        Fetch.Protocol protocol = fetchPage.protocol();
                        String urlPrefix = fetchPage.urlPrefix();
                        String port = fetchPage.port();

                        fetchedPage = annotationCall(   url,
                                                        device,
                                                        method,
                                                        referrer,
                                                        timeout,
                                                        retriesOnTimeout,
                                                        cookie,
                                                        protocol,
                                                        urlPrefix,
                                                        port
                        );
                    }
                    if (annotation instanceof FetchPages) {
                        FetchPages fetchPages = (FetchPages) annotation;
                        fetchFromAnnotation(fetchPages.value());
                    }
                }

                base.evaluate();
            }
        };
    }

    private void fetchFromAnnotation(Fetch... fetches) {
        for (Fetch fetchPage : fetches) {
            fetchedPages.add(fetch( fetchPage.url(),
                                    fetchPage.device(),
                                    fetchPage.method(),
                                    fetchPage.referrer(),
                                    fetchPage.timeout(),
                                    fetchPage.retriesOnTimeout(),
                                    cookie,
                                    fetchPage.protocol(),
                                    fetchPage.urlPrefix(),
                                    fetchPage.port()
            ));
        }
    }

    private FetchedPage fetch(String url, DeviceType device, Method method, String referrer, int timeout, int retriesOnTimeout, Map<String, String> cookie, Fetch.Protocol protocol, String urlPrefix, String port) {
        return annotationCall(  url,
                                device,
                                method,
                                referrer,
                                timeout,
                                retriesOnTimeout,
                                cookie,
                                protocol,
                                urlPrefix,
                                port
        );
    }

    public FetchedPage get() {
        return fetchedPage;
    }

    public FetchedPage get(int index) {
        try {
            return fetchedPages.get(index);
        } catch (IndexOutOfBoundsException e) { // NOSONAR
            throw new GetFetchedPageException("could not find fetched page with index \"" + index + "\"");
        }
    }

    public FetchedPage get(String urlSnippet) {
        for (FetchedPage recentlyFetchedPage : fetchedPages){
            if (recentlyFetchedPage.getUrl().contains(urlSnippet)){
                return recentlyFetchedPage;
            }
        }
        throw new GetFetchedPageException("could not find fetched page with url-snippet \"" + urlSnippet + "\"");
    }

    private int getRetryCount(Fetch fetchPage) {
        return fetchPage.retriesOnTimeout() == 0 ? config.getTimeoutMaxRetryCount() : fetchPage.retriesOnTimeout();
    }

    private int getTimeout(Fetch fetchPage) {
        return fetchPage.timeout() == 0 ? config.getTimeoutValue() : fetchPage.timeout();
    }

    private String getReferrer(Fetch fetchPage) {
        return "referrer".equals(fetchPage.referrer()) ? config.getReferrer() : fetchPage.referrer();
    }

    private Map<String, String> getCookies(Cookie[] annotationCookies) {

        HashMap<String, String> cookies = new HashMap<>();

        for (Cookie annotationCookie : annotationCookies) {
            if ("1e97fa4a-34d3-11e7-a919-92ebcb67fe33".equals(annotationCookie.name())) {
                return Collections.emptyMap();
            }
            cookies.put(annotationCookie.name(), annotationCookie.value());
        }
        return cookies;
    }
}