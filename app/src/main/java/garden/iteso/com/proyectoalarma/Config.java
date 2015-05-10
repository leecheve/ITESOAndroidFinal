package garden.iteso.com.proyectoalarma;

/**
 * Created by luisneto on 5/6/2015.
 */
public interface Config {
    // CONSTANTS
    static final String YOUR_SERVER_URL = "localhost/register";

    // Google project id
    static final String GOOGLE_SENDER_ID = "535309478114";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Message";

    static final String DISPLAY_MESSAGE_ACTION = "com.proyectoalarma.gcm.DISPLAY_MESSAGE";

    static final String EXTRA_MESSAGE = "message";
}
