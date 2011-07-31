package tinfoil.picasa;

import com.google.gdata.client.photos.PicasawebService;
import org.apache.log4j.Logger;
import tinfoil.AuthenticationException;
import tinfoil.Constants;
import tinfoil.Credentials;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 1:15 PM
 */
public class PhotoService {
    private static final Logger log = Logger.getLogger(PhotoService.class);
    private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();

    public PicasawebService init(Credentials credentials) throws AuthenticationException {
        PicasawebService service = new PicasawebService(Constants.APPLICATION_AGENT);
        try {
            service.setUserCredentials(
                credentials.username(),
                credentials.password()
            );
        } catch (com.google.gdata.util.AuthenticationException e) {
            throw new AuthenticationException(e);
        }

        long id = INSTANCE_COUNTER.incrementAndGet();
        log.info(format("[%d]: Picasa Web Service version %s/%s", id, PicasawebService.getVersion().toString(),service.getServiceVersion()));
        return service;
    }
}
