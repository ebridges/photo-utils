package tinfoil;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumEntry;
import org.apache.log4j.Logger;
import tinfoil.picasa.AlbumInfo;
import tinfoil.picasa.PhotoService;
import tinfoil.picasa.UploadConfiguration;

/**
 * <b>Album</b>
 *
 * @author ebridges@tinfoil.biz
 */


public interface Album {
    AlbumInfo getAlbumInfo();
    URL getAlbumURL();
    AlbumEntry getAlbumEntry();
}

class PicasaAlbum implements Album {
    private static final Logger log = Logger.getLogger(PicasaAlbum.class);
    private final PicasawebService service;
    private final URL albumUrl;
    private final AlbumEntry albumEntry;
    private final AlbumInfo albumInfo;

    public PicasaAlbum(UploadConfiguration configuration, AlbumEntry album, AlbumInfo albumInfo) throws AuthenticationException {

        this.albumEntry = album;
        this.albumInfo = albumInfo;
        this.service = (new PhotoService().init(configuration.getCredentials()));

        try {
            this.albumUrl = new URL(this.albumEntry.getFeedLink().getHref());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to form albumEntry URL.", e);
        }
    }

    public AlbumInfo getAlbumInfo() {
        return albumInfo;
    }

    public AlbumEntry getAlbumEntry() {
        return albumEntry;
    }

    public URL getAlbumURL() {
        return albumUrl;
    }

    /*
    public void populate(File[] pictures) throws IOException {
        Map<File,PhotoEntry> seen = new HashMap<File,PhotoEntry>();

        for(File p : pictures) {
            File picture = p.getCanonicalFile();

            if(!seen.containsKey(picture)) {
                PhotoEntry pe = new PhotoEntry();
                String mimeType = Util.getTypeFromPicture(picture);

                PlainTextConstruct title = new PlainTextConstruct(picture.getName().toLowerCase().replaceAll(" ","_"));
                pe.setTitle(title);

                PlainTextConstruct descr = new PlainTextConstruct(albumEntry.getTitle().getPlainText()+"/"+picture.getName());
                pe.setDescription(descr);

                pe.setClient(Constants.APPLICATION_NAME);
                pe.setCommentsEnabled(Boolean.TRUE);

                pe.setKeywords( albumEntry.getMediaKeywords() );

                Date creationDate = Util.lookupCreateDateOrModifiedDate(picture);
                if(null == creationDate) {
		    // try {
                        creationDate = albumEntry.getDate();
			//} catch (ServiceException e) {
                        //throw new IOException("Unable to get albumEntry date.", e);
			// }
                }

                if(null != creationDate) {
                    pe.setTimestamp(creationDate);
                }

                DateTime dt = new DateTime(new Date());
                dt.setTzShift(-4*60);
                pe.setPublished(dt);

                MediaFileSource media = new MediaFileSource(picture, mimeType);
                pe.setMediaSource(media);

                PhotoEntry photoEntry;
                try {
                    log.info("Inserting photoEntry ["+ pe.getTitle().getPlainText() +"] to albumUrl ["+albumUrl+"]");
                    photoEntry = service.insert( albumUrl, pe);
                    assert photoEntry != null;
                    seen.put(picture, photoEntry);
                    if(log.isTraceEnabled()) {
                        log.trace("    Photo Entry ID: " + photoEntry.getId());
                        for(Link l : photoEntry.getLinks()) {
                            log.trace("    Photo Entry Link - REL: [" + l.getRel() + "] HREF: [" + l.getHref() + "]");
                        }
                        log.trace("    Photo Entry Feed Link: " + photoEntry.getFeedLink().getHref());
                        log.trace("    Photo Entry Edit Link: " + photoEntry.getEditLink().getHref());
                        log.trace("    Photo Entry Self Link: " + photoEntry.getSelfLink().getHref());
                    }
                } catch (ServiceException e) {
                    throw new IOException("Unable to add photo to albumEntry: ["+e.getMessage()+"]", e);
                }

                log.debug("    Adding tags to this photo.");
                // add month & year plus albumEntry keywords as tags to this photo.

                URL photoIdUrl = new URL(photoEntry.getFeedLink().getHref());

                log.debug("    Create date: "+creationDate);
                if(null != creationDate) {
                    Pair<Integer,Integer> my = getMonthAndYear(creationDate);

                    TagEntry monthTag = new TagEntry();
                    monthTag.setTitle(new PlainTextConstruct(MONTHS.get(my.getFirst())));
                    monthTag.setWeight(my.getFirst()+5000);

                    TagEntry yearTag = new TagEntry();
                    yearTag.setTitle(new PlainTextConstruct(my.getSecond().toString()));
                    yearTag.setWeight(my.getSecond());

                    try {
                        log.debug("    Adding month tag [" + monthTag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
                        service.insert( photoIdUrl, monthTag );
                        log.debug("    Adding year tag [" + yearTag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
                        service.insert( photoIdUrl, yearTag );
                    } catch (ServiceException e) {
                        throw new IOException("Unable to add month/year tag to photo: ["+e.getMessage()+"]", e);
                    }

                    if(null != albumInfo.getKeywords() && !albumInfo.getKeywords().isEmpty()) {
                        for( String keyword : albumInfo.getKeywords()) {
                            if(null != keyword && keyword.length() > 0) {
                                TagEntry tag = new TagEntry();
                                tag.setTitle(new PlainTextConstruct(keyword));
                                try {
                                    log.debug("    Adding tag [" + tag.getTitle().getPlainText() + "] to photo URL [" + photoIdUrl + "]");
                                    TagEntry tagEntry = service.insert( photoIdUrl, tag );

                                    log.trace("    Tag Entry ID: " + tagEntry.getId());
                                    for(Link l : tagEntry.getLinks()) {
                                        log.trace("    Tag Entry Link - REL: [" + l.getRel() + "] HREF: [" + l.getHref() + "]");
                                    }
                                    log.trace("    Tag Entry Edit Link: " + tagEntry.getEditLink().getHref());
                                    log.trace("    Tag Entry Self Link: " + tagEntry.getSelfLink().getHref());
                                } catch (ServiceException e) {
                                    throw new IOException("Unable to add tag to photo: ["+e.getMessage()+"]", e);
                                }
                            }
                        }
                    }
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
    */
}
