package tinfoil;

import javax.activation.MimetypesFileTypeMap;

/**
 * <b>Constants</b>
 *
 * @author ebridges@tinfoil.biz
 */

public class Constants {
    public static final MimetypesFileTypeMap MIMETYPE_MAP = new MimetypesFileTypeMap();
    static {
        MIMETYPE_MAP.addMimeTypes("image/png png");
        MIMETYPE_MAP.addMimeTypes("image/tiff tiff tif");
        MIMETYPE_MAP.addMimeTypes("image/bmp bmp");
        MIMETYPE_MAP.addMimeTypes("image/gif gif");
    }

    public static final String APPLICATION_VENDOR = "Tinfoil";
    public static final String APPLICATION_NAME = "PicasaUploader";
    public static final String APPLICATION_VERSION = "0.2";
    public static final String APPLICATION_AGENT = String.format("%s-%s-%s",APPLICATION_VENDOR,APPLICATION_NAME,APPLICATION_VERSION);

    public static final String DEFAULT_ACCESS = "private";

    
    public static final String PHOTO_DIRECTORY = "/Users/ebridges/Pictures/picasa-upload";
    public static final String DIGIKAM_DATABASE_DRIVER = "org.sqlite.JDBC.class";
    public static final String DIGIKAM_DATABASE_URL = "jdbc:sqlite:/"+PHOTO_DIRECTORY+"/digikam.db";

    public static final String PICASA_PROFILE_URL = "https://profiles.google.com";
    public static final String PICASA_DATA_API_URL = "https://picasaweb.google.com/data/feed/api/user";
    // first param is userId, second is the album name
    public static final String PICASA_ALBUM_URL = "https://picasaweb.google.com/%s/%s";

    private Constants() {
    }
}
