package tinfoil;

import com.google.gdata.client.Service;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.Kind;
import com.google.gdata.data.Link;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.GphotoFeed;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.ServiceException;
import com.sun.xml.internal.bind.v2.model.core.ID;
import org.apache.log4j.Logger;
import org.omg.CORBA.UNSUPPORTED_POLICY;
import sun.jvm.hotspot.utilities.UnsupportedPlatformException;
import tinfoil.picasa.AlbumInfo;
import tinfoil.picasa.PhotoService;
import tinfoil.picasa.UploadConfiguration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

/**
 * <b>AlbumFactory</b>
 *
 * @author ebridges@tinfoil.biz
 */


public abstract class AlbumFactory {

    protected AlbumFactory() {
    }

    public synchronized static AlbumFactory newAlbumFactory(UploadConfiguration configuration) throws AuthenticationException {
        return new PicasaAlbumFactory(configuration);
    }

    public abstract Album newAlbum(AlbumInfo albumInfo) throws IOException, AuthenticationException;
    public abstract void deleteIfExists(AlbumInfo albumInfo) throws IOException;
}

class PicasaAlbumFactory extends AlbumFactory {
    private static final Logger log = Logger.getLogger(PicasaAlbumFactory.class);
    private static final AtomicLong FOLDER_COUNTER = new AtomicLong();

    private final UploadConfiguration configuration;
    private final PicasawebService service;

    public PicasaAlbumFactory(final UploadConfiguration configuration) throws AuthenticationException {
        this.configuration = configuration;
        this.service = (new PhotoService().init(this.configuration.getCredentials()));
    }

    public void deleteIfExists(AlbumInfo albumInfo) throws IOException {
        try {
            AlbumEntry albumEntry = lookupEntry(albumInfo);
            if(null == albumEntry) {
                log.warn(format("album [%s] does not currently exist, no need to delete.",albumInfo.getAlbumName()));
                return;
            }
            log.debug(format("located albumEntry for deleting [%s]", albumEntry.getId()));

            Link editLink = albumEntry.getEditLink();

            this.service.delete(new URL(editLink.getHref()), albumEntry.getEtag());

            log.info(format("deleted album [%s] using URL (%s)",albumInfo.getAlbumName(), editLink.getHref()));
        } catch (ServiceException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private AlbumEntry lookupEntry(AlbumInfo albumInfo) throws ServiceException, IOException {
        UserFeed userFeed = service.getFeed(new URL(this.configuration.getCredentials().serviceUrl()), UserFeed.class);
        List<GphotoEntry> entries = userFeed.getEntries();
        AlbumEntry albumEntry = null;
        for (GphotoEntry entry : entries) {
          GphotoEntry adapted = entry.getAdaptedEntry();
          if (adapted instanceof AlbumEntry) {
              AlbumEntry e = (AlbumEntry)adapted;
              //log.debug("found album named: "+e.getName());
              if(e.getName().equals(albumInfo.getFolderName())) {
                  albumEntry = e;
                  break;
              }
          }
        }
        return albumEntry;
    }

    public Album newAlbum(AlbumInfo albumInfo) throws IOException, AuthenticationException {
        AlbumEntry entry = new AlbumEntry();

        entry.setAccess(Constants.DEFAULT_ACCESS);
        entry.setCommentsEnabled(Boolean.TRUE);
        entry.setDate(albumInfo.getAlbumDate());
        TextConstruct descr = new PlainTextConstruct( albumInfo.getDescription() );
        entry.setDescription(descr);

        Person us = new Person(
                this.configuration.getCredentials().authorName(),
                this.configuration.getCredentials().authorHome(),
                this.configuration.getCredentials().authorMail()
        );
        entry.getAuthors().add(us);

        TextConstruct title = new PlainTextConstruct( albumInfo.getAlbumName() );
        entry.setTitle( title );
        entry.setName( albumInfo.getAlbumName() );

        AlbumEntry albumEntry;
        try {
            synchronized (this) {
                log.debug(format("opening connection to service url (%s)",this.configuration.getServiceUrl()));
                albumEntry = service.insert(this.configuration.getServiceUrl(), entry);

                MediaKeywords mediaKeywords = new MediaKeywords();
                mediaKeywords.addKeywords( albumInfo.getKeywords() );
                albumEntry.setKeywords(mediaKeywords);

                albumEntry = service.update( new URL(albumEntry.getEditLink().getHref()), albumEntry);
            }
            
        } catch (Throwable e) {
            throw new IOException("Unable to create album: ["+e.getMessage()+"]", e);
        }

        long id = FOLDER_COUNTER.incrementAndGet();
        log.info(format("[%d]: folder name (%s), entry id (%s), editUri (%s), etag (%s)", id, albumEntry.getName(), albumEntry.getId(), albumEntry.getEditLink().getHref(), albumEntry.getEtag()));
        return new PicasaAlbum(configuration, albumEntry, albumInfo);
    }
}
