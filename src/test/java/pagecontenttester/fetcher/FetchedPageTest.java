package pagecontenttester.fetcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static pagecontenttester.annotations.Fetch.Protocol.HTTPS;
import static pagecontenttester.fetcher.FetchedPage.DeviceType.DESKTOP;
import static pagecontenttester.fetcher.FetchedPage.DeviceType.MOBILE;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import pagecontenttester.annotations.Cookie;
import pagecontenttester.annotations.Fetch;
import pagecontenttester.annotations.GetFetchedPageException;
import pagecontenttester.runner.PageContentTester;

@Fetch(url = "github.com/christian-draeger")
public class FetchedPageTest extends PageContentTester {

    private static final String GITHUB_URL = "github.com/christian-draeger";
    private static final String GOOGLE_URL = "www.google.de";
    private static final String VALID_SELECTOR = "h1";
    
    @Test
    public void can_fetch_from_class_annotation() {
        assertThat(page.get().getUrl(), containsString("github"));
    }

    @Test
    public void fetcher_should_return_fetched_desktop_page_for_valid_url() {
        assertThat(page.get().isMobile(), is(false));
    }

    @Test
    @Fetch(url = GOOGLE_URL, device = MOBILE)
    public void fetcher_should_return_fetched_mobile_page_for_valid_url() {
        assertThat(page.get().isMobile(), is(true));
    }

    @Test
    @Fetch(url = GOOGLE_URL, device = MOBILE)
    public void fetcher_should_return_device_type_for_fetched_mobile_page() {
        assertThat(page.get().getDeviceType(), equalTo(MOBILE));
    }

    @Test
    @Fetch(url = GOOGLE_URL, device = DESKTOP)
    public void fetcher_should_return_device_type_for_fetched_desktop_page() {
        assertThat(page.get().getDeviceType(), equalTo(DESKTOP));
    }

    @Test
    public void fetcher_should_return_referrer() {
        assertThat(page.get().getConfig().getReferrer(), equalTo("http://www.google.com"));
    }

    @Test
    @Fetch(url = "www.idealo.de")
    public void fetcher_should_return_cookie_value() {
        assertThat(page.get().getCookieValue("ipcuid"), not(emptyString()));
    }

    @Test
    public void fetcher_should_return_cookies() {
        assertThat(page.get().getCookies(), either(hasEntry("logged_in", "no")).or(is(Collections.emptyMap())));
    }

    @Test
    @Fetch(url = "www.idealo.de")
    public void fetcher_should_return_cookie() {
        assertThat(page.get().hasCookie("ipcuid"), is(true));
    }

    @Test
    public void fetcher_should_return_content_type() {
        assertThat(page.get().getContentType(), containsString("text/html"));
    }

    @Test
    public void fetcher_should_return_element() {
        assertThat(page.get().getElement(VALID_SELECTOR).hasText(), is(true));
    }

    @Test
    public void fetcher_should_return_page_body() {
        assertThat(page.get().getPageBody(), containsString("<!DOCTYPE html>"));
    }

    @Test
    @Fetch(url = GOOGLE_URL)
    public void fetcher_should_return_status_message() {
        assertThat(page.get().getStatusMessage(), equalTo("OK"));
    }

    @Test
    @Fetch(url = GOOGLE_URL)
    public void fetcher_should_return_status_code() {
        assertThat(page.get().getStatusCode(), is(200));
    }

    @Test
    public void should_return_true_if_certain_element_is_present() {
        assertThat(page.get().isElementPresent(VALID_SELECTOR), is(true));
    }

    @Test
    public void should_return_false_if_certain_element_is_not_present() {
        assertThat(page.get().isElementPresent("dgfhkdgs"), is(false));
    }

    @Test
    public void fetcher_should_return_elements_by_selector() {
        assertThat(page.get().getElements(VALID_SELECTOR).size(), greaterThanOrEqualTo(1));
    }

    @Test
    public void fetcher_should_return_last_matching_element_by_selector() {
        assertThat(page.get().getElementLastOf("title").text(), not(emptyString()));
    }

    @Test
    public void fetcher_should_return_nth_matching_element_by_selector() {
        assertThat(page.get().getElement("title", 0).text(), not(emptyString()));
    }

    @Test
    public void fetcher_should_return_count_of_certain_element() {
        assertThat(page.get().getElementCount(VALID_SELECTOR), is(1));
    }

    @Test
    public void fetcher_should_return_headers() {
        assertThat(page.get().getHeaders(), hasEntry("Server", "GitHub.com"));
    }

    @Test(expected = NullPointerException.class)
    public void fetcher_should_return_null_for_non_existing_location_header() {
        String location = page.get().getLocation();
        assertThat(location, is(null));
    }

    @Test
    public void fetcher_should_return_certain_header() {
        assertThat(page.get().getHeader("Server"), equalTo("GitHub.com"));
    }

    @Test
    public void fetcher_should_check_is_certain_header_is_present() {
        assertThat(page.get().hasHeader("Server"), is(true));
    }

    @Test
    @Fetch(url = GITHUB_URL)
    @Fetch(url = GOOGLE_URL)
    public void fetch_multiple_pages_via_annotation_and_get_pages_by_index() {
        assertThat(page.get(0).isElementPresent("h1"), is(true));
        assertThat(page.get(0).getUrl(), equalTo("http://" + GITHUB_URL));

        assertThat(page.get(1).isElementPresent("#footer"), is(true));
        assertThat(page.get(1).getUrl(), equalTo("http://" + GOOGLE_URL));
    }

    @Test
    @Fetch(url = GITHUB_URL)
    @Fetch(url = GOOGLE_URL)
    public void fetch_multiple_pages_via_annotation_and_get_pages_by_url_snippet() {
        assertThat(page.get("github").isElementPresent("h1"), is(true));
        assertThat(page.get("google").isElementPresent("#footer"), is(true));
    }

    @Test(expected = GetFetchedPageException.class)
    public void fetch_page_via_annotation_and_try_to_get_fetched_page_by_unknown_url_snippet() {
        page.get("unknown");
    }

    @Test
    @Fetch(protocol = HTTPS, urlPrefix = "en", url = "wikipedia.org")
    public void fetch_page_via_annotation_and_build_url() {
        assertThat(page.get().getUrl(), equalTo("https://en.wikipedia.org"));
        assertThat(page.get().getUrlPrefix(), equalTo("en"));
    }

    @Test(expected = GetFetchedPageException.class)
    public void fetch_page_via_annotation_and_try_to_get_fetched_page_by_invalid_index() {
        page.get(1);
    }

    @Test
    @Fetch(url = GOOGLE_URL)
    @Fetch(url = GOOGLE_URL, device = MOBILE)
    public void fetch_as_desktop_and_mobile_device_by_annotation_and_get_page_by_url_and_device() {
        assertThat(page.get(GOOGLE_URL, DESKTOP).getDeviceType(), equalTo(DESKTOP));
        assertThat(page.get(GOOGLE_URL, MOBILE).getDeviceType(), equalTo(MOBILE));
    }

    @Test
    @Fetch(url = GOOGLE_URL)
    @Fetch(url = GOOGLE_URL, device = MOBILE)
    public void fetch_as_desktop_and_mobile_device_by_annotation_and_get_by_device() {
        assertThat(page.get(DESKTOP).getDeviceType(), equalTo(DESKTOP));
        assertThat(page.get(MOBILE).getDeviceType(), equalTo(MOBILE));
    }

    @Test
    @Fetch(url = "github.com:8080/christian-draeger")
    @Fetch(url = "github.com:8080/christian-draeger", device = MOBILE)
    public void should_retrun_fetched_page_even_with_wrong_port() {
        assertThat(page.get(GITHUB_URL, DESKTOP).getDeviceType(), equalTo(DESKTOP));
        assertThat(page.get(GITHUB_URL, MOBILE).getDeviceType(), equalTo(MOBILE));
    }

    @Test
    @Fetch(url = GOOGLE_URL)
    @Fetch(url = GOOGLE_URL, device = MOBILE)
    public void should_return_fetched_page_for_url_snippet_and_device() {
        assertThat(page.get("google", DESKTOP).getDeviceType(), equalTo(DESKTOP));
        assertThat(page.get("google", MOBILE).getDeviceType(), equalTo(MOBILE));
    }

    @Test
    @Fetch(url = GOOGLE_URL)
    @Fetch(url = GOOGLE_URL, device = MOBILE)
    public void should_return_fetched_page_for_url_snippet() {
        assertThat(page.get("google").getDeviceType(), equalTo(DESKTOP));
    }

    @Test(expected = GetFetchedPageException.class)
    @Fetch(url = GOOGLE_URL)
    @Fetch(url = GOOGLE_URL, device = MOBILE)
    public void should_throw_exception_if_page_can_not_get_by_url() {
        page.get("wrong-url", DESKTOP);

    }

    @Test
    public void get_name_of_test() {
        assertThat(page.get().getTestName(), equalTo("pagecontenttester.fetcher.FetchedPageTest.get_name_of_test"));
    }

    @Test
    public void get_name_of_other_test() {
        assertThat(page.get().getTestName(), equalTo("pagecontenttester.fetcher.FetchedPageTest.get_name_of_other_test"));
    }

    @Test
    public void can_store_page_body() throws IOException, InterruptedException {
        page.get().storePageBody();
        File file = new File("target/page-content-tester/stored/pagecontenttester.fetcher.FetchedPageTest.can_store_page_body.html");
        String pageBody = FileUtils.readFileToString(file);
        assertThat(pageBody, containsString("html"));
    }

    @Test
    public void store_page_body_if_element_not_present() throws IOException {
        page.get().getElements("dfghfjhg");
        File file = new File("target/page-content-tester/not-found/pagecontenttester.fetcher.FetchedPageTest.store_page_body_if_element_not_present.html");
        String pageBody = FileUtils.readFileToString(file);
        assertThat(pageBody, containsString("GitHub"));
    }


    @Test
    @Fetch(url = "whatsmyuseragent.org/", device = MOBILE)
    public void fetch_as_mobile_device_by_annotation() {
        String ua = page.get().getElement("p.intro-text").text();
        assertThat(ua, containsString(page.get().getConfig().getUserAgent(MOBILE)));
    }

    @Test
    @Fetch(url = "www.whatismyreferer.com/", referrer = "my.custom.referrer")
    public void fetch_page_by_setting_custom_referrer() {
        String referrer = page.get().getElement("strong").text();
        assertThat(referrer, equalTo("my.custom.referrer"));
    }

    @Test
    @Fetch(url = "www.whatismyreferer.com/")
    public void fetch_page_should_use_referrer_from_properties_by_default() {
        String referrer = page.get().getElement("strong").text();
        assertThat(referrer, equalTo(config.getReferrer()));
    }

    @Test
    @Fetch( url = "www.html-kit.com/tools/cookietester/",
            setCookies = @Cookie(name = "page-content-tester", value = "wtf-666"))
    public void can_set_cookie_via_annotation() throws Exception {
        assertThat(page.get().getDocument().body().text(),
                both(containsString("page-content-tester"))
                        .and(containsString("wtf-666")));
    }

    @Test
    @Fetch( url = "www.html-kit.com/tools/cookietester/",
            setCookies = {  @Cookie(name = "page-content-tester", value = "wtf-666"),
                            @Cookie(name = "some-other-cookie", value = "666-wtf") })
    public void can_set__multiple_cookies_via_annotation() throws Exception {
        String body = page.get().getDocument().body().text();
        assertThat(body, both(containsString("page-content-tester"))
                        .and(containsString("wtf-666")));
        assertThat(body, both(containsString("some-other-cookie"))
                        .and(containsString("666-wtf")));
    }

    @Ignore("problems in call method")
    @Test
    public void do_post_request_and_check_response() throws Exception {
//        JSONObject responseBody = call("http://bin.org/post", POST, Collections.emptyMap()).getJsonResponse();
//        assertThat(responseBody.get("data"), equalTo(""));
    }

    @Test
    public void should_return_true_for_certain_count_of_certain_element() {
        assertThat(page.get().isElementPresentNthTimes(VALID_SELECTOR, 1), is(true));
    }

    @Test
    public void should_return_false_for_invalid_count_of_certain_element() {
        assertThat(page.get().isElementPresentNthTimes(VALID_SELECTOR, 100), is(false));
    }

}
