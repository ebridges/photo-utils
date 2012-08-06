package tinfoil;

import static tinfoil.Constants.*;
import static tinfoil.CredentialInfo.*;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 1:17 PM
 */
class CredentialInfo {
    static final String TEST_USERID = "115366439539918876494";
    static final String TEST_SERVICE_URL = String.format("%s/%s",PICASA_DATA_API_URL,TEST_USERID);
    static final String TEST_USERNAME = "picasatest@eqbridges.com";
    static final String TEST_PASSWORD = "p1c@s@t3st";
    static final String TEST_AUTHOR_NAME = "SimonFoo HaskellBar";
    static final String TEST_AUTHOR_HOME = String.format("%s/%s",PICASA_PROFILE_URL, TEST_USERID);
    static final String TEST_AUTHOR_MAIL = "picasatest@eqbridges.com";

    static final String USERID = "";
    static final String SERVICE_URL = String.format("%s/%s",PICASA_DATA_API_URL,USERID);
    static final String SERVICE_USERNAME = "";
    static final String SERVICE_PASSWORD = "";
    static final String AUTHOR_NAME = "";
    static final String AUTHOR_HOME = String.format("%s/%s", PICASA_PROFILE_URL, USERID);
    static final String AUTHOR_MAIL = "";
}

public enum Credentials {
    TEST(TEST_USERID, TEST_SERVICE_URL, TEST_USERNAME, TEST_PASSWORD, TEST_AUTHOR_NAME, TEST_AUTHOR_HOME, TEST_AUTHOR_MAIL),
    PROD(USERID, SERVICE_URL, SERVICE_USERNAME, SERVICE_PASSWORD, AUTHOR_NAME, AUTHOR_HOME, AUTHOR_MAIL);

    private final String userId;
    private final String serviceUrl;
    private final String username;
    private final String password;
    private final String authorName;
    private final String authorHome;
    private final String authorMail;

    Credentials(String userId, String serviceUrl, String username, String password, String authorName, String authorHome, String authorMail) {
        this.userId = userId;
        this.serviceUrl = serviceUrl;
        this.username = username;
        this.password = password;
        this.authorName = authorName;
        this.authorHome = authorHome;
        this.authorMail = authorMail;
    }

    public String userId() {
        return userId;
    }

    public String serviceUrl() {
        return serviceUrl;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String authorName() {
        return authorName;
    }

    public String authorHome() {
        return authorHome;
    }

    public String authorMail() {
        return authorMail;
    }
}
