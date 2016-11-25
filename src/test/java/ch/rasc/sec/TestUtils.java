package ch.rasc.sec;

/**
 * User: NotePad.by
 * Date: 11/24/2016.
 */
public class TestUtils {

    private static final String HOST = "https://seorgy-15283.herokuapp.com/";
    private static final String LOCALHOST = "http://127.0.0.1:8080/";

    public static String getUrl(String action) {
        return HOST + action;
    }

}
