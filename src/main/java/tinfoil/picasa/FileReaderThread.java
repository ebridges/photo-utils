package tinfoil.picasa;

import static java.lang.String.format;
import static tinfoil.Util.getTypeFromPicture;
import static tinfoil.Util.lookupCreateDate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.TagEntry;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.common.base.Pair;
import org.apache.log4j.Logger;
import tinfoil.Album;
import tinfoil.AuthenticationException;
import tinfoil.Constants;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 3:47 PM
 */
public class FileReaderThread implements Callable<PhotoUploadResult> {
    private final Logger logger = Logger.getLogger(FileReaderThread.class);

    private final Album album;
    private final File photo;
    private final PicasawebService service;

    public FileReaderThread(Album album, final File photo, final UploadConfiguration configuration) throws AuthenticationException {
        this.album = album;
        this.photo = photo;
        this.service = (new PhotoService()).init(configuration.getCredentials());
    }

    @Override
    public PhotoUploadResult call() throws Exception {
        logger.info(format("adding photo [%s] to folder [%s]", photo.getName(), album.getAlbumInfo().getAlbumName()));
        PhotoUploadResult result;

        try {
            PhotoEntry photoEntry = initalizePhotoEntry();
            if(null != photoEntry) {
                addTagsToPhoto(photoEntry);
            }
            result = new PhotoUploadResult(album, photo, "SUCCESS");
        } catch (Exception e) {
            result = new PhotoUploadResult(album, photo, e.getMessage(), e);
            throw e;
        } catch (Throwable t) {
            result = new PhotoUploadResult(album, photo, t.getMessage(), t);
        }

        return result;
    }

    private PhotoEntry initalizePhotoEntry() throws IOException, InterruptedException {
        PhotoEntry pe = new PhotoEntry();
        String mimeType = getTypeFromPicture(photo);

        PlainTextConstruct title = new PlainTextConstruct(photo.getName().toLowerCase().replaceAll(" ","_"));
        pe.setTitle(title);

        PlainTextConstruct descr = new PlainTextConstruct(album.getAlbumInfo().getAlbumName()+"/"+photo.getName());
        pe.setDescription(descr);

        pe.setClient(Constants.APPLICATION_NAME);
        pe.setCommentsEnabled(Boolean.TRUE);

        // @todo implementation leakage
        pe.setKeywords(album.getAlbumEntry().getMediaKeywords());

        Date creationDate = lookupCreateDate(photo);
        if(null == creationDate) {
            creationDate = album.getAlbumInfo().getAlbumDate();
        }

        if(null != creationDate) {
            pe.setTimestamp(creationDate);
        }

        DateTime dt = new DateTime(new Date());
        // @todo DST??
        dt.setTzShift(-4*60);
        pe.setPublished(dt);

        MediaFileSource media = new MediaFileSource(photo, mimeType);
        pe.setMediaSource(media);

        PhotoEntry photoEntry;
        try {
            logger.debug("Inserting photoEntry [" + pe.getTitle().getPlainText() + "] to albumUrl [" + album.getAlbumURL() + "]");
            if(Thread.currentThread().isInterrupted()) {
                throw new InterruptedException(format("photo [%s] not added to albumUrl [%s]", photo.getName(), album.getAlbumURL()));
            }
            photoEntry = service.insert( album.getAlbumURL(), pe);
            assert photoEntry != null;

            if(logger.isTraceEnabled()) {
                logger.trace("    Photo Entry ID: " + photoEntry.getId());
                for(Link l : photoEntry.getLinks()) {
                    logger.trace("    Photo Entry Link - REL: [" + l.getRel() + "] HREF: [" + l.getHref() + "]");
                }
                logger.trace("    Photo Entry Feed Link: " + photoEntry.getFeedLink().getHref());
                logger.trace("    Photo Entry Edit Link: " + photoEntry.getEditLink().getHref());
                logger.trace("    Photo Entry Self Link: " + photoEntry.getSelfLink().getHref());
            }
            return photoEntry;
        } catch (ServiceException e) {
            throw new IOException("Unable to add photo to album: ["+e.getMessage()+"]", e);
        }
    }

    private void addTagsToPhoto(PhotoEntry photoEntry) throws ServiceException, IOException, InterruptedException {
        logger.debug("adding tags to this photo.");

        // add month & year plus albumEntry keywords as tags to this photo.
        URL photoIdUrl = new URL(photoEntry.getFeedLink().getHref());

        Pair<Integer,Integer> my = getMonthAndYear(photoEntry.getTimestamp());

        TagEntry monthTag = new TagEntry();
        monthTag.setTitle(new PlainTextConstruct(MONTHS.get(my.getFirst())));
        monthTag.setWeight(my.getFirst() + 5000);

        TagEntry yearTag = new TagEntry();
        yearTag.setTitle(new PlainTextConstruct(my.getSecond().toString()));
        yearTag.setWeight(my.getSecond());

        try {
            logger.trace("    Adding month tag [" + monthTag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
            service.insert( photoIdUrl, monthTag );
            logger.trace("    Adding year tag [" + yearTag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
            service.insert( photoIdUrl, yearTag );
        } catch (ServiceException e) {
            throw new IOException("Unable to add month/year tag to photo: ["+e.getMessage()+"]", e);
        }

        if(null != album.getAlbumInfo().getKeywords() && !album.getAlbumInfo().getKeywords().isEmpty()) {
            int cnt=0;
            for( final String keyword : album.getAlbumInfo().getKeywords()) {
                if(null != keyword && keyword.length() > 0) {
                    TagEntry tag = new TagEntry();
                    tag.setTitle(new PlainTextConstruct(keyword));
                    try {
                        logger.trace("    Adding tag [" + tag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
                        TagEntry tagEntry = service.insert( photoIdUrl, tag );

                        logger.trace("    Tag Entry ID: " + tagEntry.getId());
                        for(Link l : tagEntry.getLinks()) {
                            logger.trace("    Tag Entry Link - REL: [" + l.getRel() + "] HREF: [" + l.getHref() + "]");
                        }
                        logger.trace("    Tag Entry Edit Link: " + tagEntry.getEditLink().getHref());
                        logger.trace("    Tag Entry Self Link: " + tagEntry.getSelfLink().getHref());
                    } catch (ServiceException e) {
                        throw new IOException("Unable to add tag to photo: ["+e.getMessage()+"]", e);
                    }
                }
                cnt++;
                if(Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException(format("only %d of %d total num of tags added to photo [%s] in album [%s]", cnt, album.getAlbumInfo().getKeywords().size(), photo.getName(), album.getAlbumURL()));
                }
            }
        }
    }

    private Pair<Integer,Integer> getMonthAndYear(Date albumDate) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(albumDate.getTime());
        return new Pair<Integer,Integer>(
                c.get(Calendar.MONTH),
                c.get(Calendar.YEAR)
        );
    }

    private static final Map<Integer,String> MONTHS = new HashMap<Integer, String>();
    static {
        MONTHS.put(0, "January");
        MONTHS.put(1, "February");
        MONTHS.put(2, "March");
        MONTHS.put(3, "April");
        MONTHS.put(4, "May");
        MONTHS.put(5, "June");
        MONTHS.put(6, "July");
        MONTHS.put(7, "August");
        MONTHS.put(8, "September");
        MONTHS.put(9, "October");
        MONTHS.put(10, "November");
        MONTHS.put(11, "December");
    }
}