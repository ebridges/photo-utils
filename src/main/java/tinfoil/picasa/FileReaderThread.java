/*
 * Copyright (c) 2011. Edward Q. Bridges <ebridges@gmail.com>
 * Licensed under the GNU Lesser General Public License v.3.0
 * http://www.gnu.org/licenses/lgpl.html
 */

package tinfoil.picasa;

import static java.lang.String.format;
import static tinfoil.Util.getTypeFromPicture;
import static tinfoil.Util.lookupCreateDateOrModifiedDate;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tinfoil.Album;
import tinfoil.AuthenticationException;
import tinfoil.Constants;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 3:47 PM
 */
public class FileReaderThread implements Callable<String> {
    private final Logger logger = LoggerFactory.getLogger(FileReaderThread.class);

    private final Album album;
    private final File photo;
    private final PhotoUploadResult result;
    private final PicasawebService service;

    public FileReaderThread(Album album, final File photo, final UploadConfiguration configuration, PhotoUploadResult result) throws AuthenticationException {
        this.album = album;
        this.photo = photo;
        this.result = result;
        this.service = (new PhotoService()).init(configuration.getCredentials());
    }

    @Override
    public String call() throws Exception {
        logger.info(format("adding photo [%s] to folder [%s]", photo.getName(), album.getAlbumInfo().getAlbumName()));
        try {
            PhotoEntry photoEntry = initalizePhotoEntry();
            if(null != photoEntry) {
                addTagsToPhoto(photoEntry);
                result.setSuccess();
            } else {
                result.setPartiallyProcessed();
            }
        } catch (Exception e) {
            result.setError(e);
            throw e;
        } catch (Throwable t) {
            result.setError(t);
            throw new RuntimeException(t);
        }

        return format("%s/%s",album.getAlbumInfo().getFolder().getName(), photo.getName());
    }

    private PhotoEntry initalizePhotoEntry() throws IOException, InterruptedException {
        logger.info(format("adding photo [%s] to album [%s]", photo.getName(), album.getAlbumInfo().getAlbumName()));
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

        Date creationDate = lookupCreateDateOrModifiedDate(photo);
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
            if(Thread.currentThread().isInterrupted()) {
                throw new InterruptedException(format("photo [%s] not added to albumUrl [%s]", photo.getName(), album.getAlbumURL()));
            }
            logger.debug("inserting photoEntry [" + pe.getTitle().getPlainText() + "] to albumUrl [" + album.getAlbumURL() + "]");
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
            throw new FileUploadException(this.album, this.photo, e);
        }
    }

    private void addTagsToPhoto(PhotoEntry photoEntry) throws ServiceException, IOException, InterruptedException {
        logger.debug("adding tags to "+photo.getName());

        // add month & year plus albumEntry keywords as tags to this photo.
        URL photoIdUrl = new URL(photoEntry.getFeedLink().getHref());

        Pair<Integer,Integer> my = getMonthAndYear(photoEntry.getTimestamp());

        TagEntry monthTag = new TagEntry();
        monthTag.setTitle(new PlainTextConstruct(MONTHS.get(my.getFirst())));
        monthTag.setWeight(my.getFirst() + 5000);
        logger.trace("    Adding month tag [" + monthTag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
        try {
            service.insert( photoIdUrl, monthTag );
        } catch (ServiceException e) {
            throw new FileUploadException(this.album, this.photo, "adding month tag", e);
        }

        TagEntry yearTag = new TagEntry();
        yearTag.setTitle(new PlainTextConstruct(my.getSecond().toString()));
        yearTag.setWeight(my.getSecond());
        logger.trace("    Adding year tag [" + yearTag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
        try {
            service.insert( photoIdUrl, yearTag );
        } catch (ServiceException e) {
            throw new FileUploadException(this.album, this.photo, "adding year tag", e);
        }

        if(null != album.getAlbumInfo().getKeywords() && !album.getAlbumInfo().getKeywords().isEmpty()) {
           // int cnt=0;
            for( final String keyword : album.getAlbumInfo().getKeywords()) {
                if(null != keyword && keyword.length() > 0) {
                    TagEntry tag = new TagEntry();
                    tag.setTitle(new PlainTextConstruct(keyword));
                    try {
                        logger.trace("    Adding tag [" + tag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
                        TagEntry tagEntry = service.insert( photoIdUrl, tag );

                        if(logger.isTraceEnabled()){
                            logger.trace("    Tag Entry ID: " + tagEntry.getId());
                            for(Link l : tagEntry.getLinks()) {
                                logger.trace("    Tag Entry Link - REL: [" + l.getRel() + "] HREF: [" + l.getHref() + "]");
                            }
                            logger.trace("    Tag Entry Edit Link: " + tagEntry.getEditLink().getHref());
                            logger.trace("    Tag Entry Self Link: " + tagEntry.getSelfLink().getHref());
                        }
                    } catch (ServiceException e) {
                        throw new FileUploadException(this.album, this.photo, "adding tag ["+ tag + "]", e);
                    }
                }
              //  cnt++;
               // if(Thread.currentThread().isInterrupted()) {
               //     throw new InterruptedException(format("only %d of %d total num of tags added to photo [%s] in album [%s]", cnt, album.getAlbumInfo().getKeywords().size(), photo.getName(), album.getAlbumURL()));
               // }
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
