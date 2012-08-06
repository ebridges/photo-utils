package tinfoil;

import static java.lang.String.format;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.IOCase.INSENSITIVE;
import static org.apache.commons.io.filefilter.FileFilterUtils.or;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.util.common.xml.XmlWriter;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
*/
/*
import org.metaphile.directory.IDirectory;
import org.metaphile.file.JpegFile;
import org.metaphile.segment.ExifSegment;
import org.metaphile.tag.ITag;
*/

/**
 * <b>Util</b>
 *
 * @author ebridges@tinfoil.biz
 */

public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static String formatDate(Date date, String format) {
        if(null == date) {
            throw new IllegalArgumentException("got null date.");
        }
        if(isEmpty(format)) {
            throw new IllegalArgumentException("got empty format string");
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);

    }

    public static String getTypeFromPicture(File file) {
        return Constants.MIMETYPE_MAP.getContentType(file);
    }

    public static boolean isVideo(File file) {
        String extension = getExtension(file.getAbsolutePath());
        return "AVI".equalsIgnoreCase(extension) || "MOV".equalsIgnoreCase(extension);
    }

    /* *
     * Returns null if it can't figure out the create date.
     * 
     * @param file
     * @return
     * @throws IOException
     *
    public static Date lookupCreateDateAlt(final File file) throws IOException {
        File picture = file;
        if(isVideo(picture)) {
            // try to use THM file to derive info.
            log.debug(format("using thm file to lookup create date for %s", picture.getAbsolutePath()));
            File f = changeFileExtension(picture, "THM");
            if(f.exists()) {
                picture = f;
            }
        }

        // default to last modified date.
        Date createDate = null;
        String mimeType = getTypeFromPicture(picture);
        if(mimeType.endsWith("jpeg") || getExtension(picture.getAbsolutePath()).equalsIgnoreCase("THM")) {
            try {
                IImageMetadata metadata = Sanselan.getMetadata(picture);

                if (metadata instanceof JpegImageMetadata)  {
                    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                    TiffField field = jpegMetadata.findEXIFValue(TiffConstants.EXIF_TAG_CREATE_DATE);

                    if (field == null) {
                        jpegMetadata.dump();
//                        throw new IllegalArgumentException(format("unable to locate create date in metadata for photo %s", picture.getAbsolutePath()));
                        log.warn("Unable to get create date from EXIF info.");
                        return createDate;
                    }
                   // log.debug(format("tag %s: %s", field.getTagName(), field.getStringValue()));
                    String date = field.getStringValue();
                    if(null != date && date.length() > 0 && !date.toLowerCase().equals("null")) {
                        createDate = toDate(date, "yyyy:MM:dd HH:mm:ss");
                    } else {
                        log.warn("got null create date for file: "+picture.getAbsolutePath());
                    }
                }

            } catch (ImageReadException e) {
                log.warn("Caught ImageReadException: "+e.getMessage());
            }
        } else {
            log.warn("unsupported file type: "+picture.getAbsolutePath());
        }

        return createDate;
    }
    */

    public static Date lookupCreateDateAlt2(File file) {
        Date createDate = null;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            /*
            Iterator i = metadata.getDirectoryIterator();
            while( i.hasNext()) {
                Directory d = (Directory) i.next();
                Iterator ii = d.getTagIterator();
                while(ii.hasNext()) {
                    Object o = ii.next();
                    System.out.println(o.toString());
                }
            }
            */

            Directory directory = metadata.getDirectory(ExifDirectory.class);
            createDate = directory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
        } catch (ImageProcessingException e) {
            log.warn(format("caught ImageProcessingException [%s] [%s]", e.getMessage(), file.getAbsolutePath()));
        } catch (MetadataException e) {
            log.warn(format("caught MetadataException [%s] [%s]", e.getMessage(), file.getAbsolutePath()));
        }
        return createDate;
    }

    public static File changeFileExtension(File file, String newExtension) {
        String newFile = FilenameUtils.getBaseName(file.getAbsolutePath()) + "." + newExtension;
        return new File(file.getParentFile(), newFile);
    }

    public static Date lookupCreateDateOrModifiedDate(File picture) throws IOException {
        //return lookupCreateDateOrModifiedDate(getTypeFromPicture(picture), picture);
        //return lookupCreateDateAlt( picture);
        Date date = lookupCreateDateAlt2(picture);
        if(null == date) {
            return new Date(picture.lastModified());
        } else {
            return date;
        }
    }

    /*
    public static Date lookupCreateDateOrModifiedDate(String mimeType, File picture) {
        Date createDate = null;

        try {
            if(mimeType.endsWith("jpeg")) {
                JpegFile file = new JpegFile(picture);
                ExifSegment exif = file.getExifSegment();

                if (exif != null) {
                    IDirectory exifDir = exif.getDirectory( ExifSegment.DIRECTORY_EXIF );
                    Iterator i = exifDir.getTagIterator();
                    while (i.hasNext()) {
                        ITag tag = (ITag) i.next();
                        log.debug("Examining tag: ["+tag.getName()+"="+tag.getValueAsString()+"]");
                        if(tag.getName().equalsIgnoreCase("Date Time Original")) {
                            String date = tag.getValueAsString();
                            if(null != date && date.length() > 0 && !date.toLowerCase().equals("null")) {
                                createDate = toDate(date, "yyyy:MM:dd HH:mm:ss");
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Unable to load picture metadata from ["+picture.getAbsolutePath()+"]", e);
            throw new IllegalArgumentException(e);
        }

        if(null == createDate) {
            log.info("Unable to get create date from EXIF info, using file modification time instead.");
            return new Date(picture.lastModified());
        }

        return createDate;
    }
    */

    public static Date toDate(String date, String format) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 0);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("unable to parse date from [" + date + "]", e);
        }
        c.setTimeInMillis(d.getTime());
        // log.debug("Parsed ["+c.getTime()+"] from ['"+date+"','"+format+"']");
        Date dateType = c.getTime();
        assert null != dateType;
        return dateType;
    }

    public static String toString(Object[] vals) {
        StringBuilder value = new StringBuilder(vals.length*32);
        for(int i=0; i<vals.length; i++) {
            if(i > 0) {
                value.append(',');
            }
            if(null != vals[i]) {
                value.append(vals.toString());
            }
        }
        return value.toString();
    }

    /**
     * Utility function to dump the entry as XML to the provided stream.
     *
     * @param entry The entry to dump.
     * @param out   The output stream to write to.
     * @throws java.io.IOException Thrown when error writing.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static void dump(BaseEntry entry, OutputStream out)
            throws IOException {
        Writer w = new OutputStreamWriter(out);
        XmlWriter xmlW = new XmlWriter(w);
        entry.generateAtom(xmlW, new ExtensionProfile());
        w.flush();
    }

    /**
     * Utility function to dump the feed as XML to the provided stream.
     *
     * @param feed The feed to dump.
     * @param out  The output stream to write to.
     * @throws java.io.IOException Thrown when error writing.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static void dump(BaseFeed feed, OutputStream out)
            throws IOException {
        Writer w = new OutputStreamWriter(out);
        XmlWriter xmlW = new XmlWriter(w);
        feed.generateAtom(xmlW, new ExtensionProfile());
        w.flush();
    }

    public static void throwIfEmpty(String value, String field) {
        if(isEmpty(value)) {
            throw new IllegalArgumentException(String.format("%s cannot be empty",field));
        }
    }

    public static boolean isEmpty(String v) {
        return (v == null || v.length() < 1);
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    public static void waitSome(long i) {
        log.info("Pausing for "+i+" seconds.");
        try {
            Thread.sleep(i*1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static IOFileFilter MULTIMEDIA_FILE_FILTER = or(
            suffixFileFilter("jpg", INSENSITIVE),
            suffixFileFilter("jpeg", INSENSITIVE),
            suffixFileFilter("png", INSENSITIVE),
            suffixFileFilter("gif", INSENSITIVE),
            suffixFileFilter("bmp", INSENSITIVE),
            suffixFileFilter("avi", INSENSITIVE),
            suffixFileFilter("mov", INSENSITIVE),
            suffixFileFilter("thm", INSENSITIVE),
            suffixFileFilter("mp4", INSENSITIVE)
    );

    public static IOFileFilter PHOTO_FILE_FILTER = or(
            suffixFileFilter("jpg", INSENSITIVE),
            suffixFileFilter("jpeg", INSENSITIVE),
            suffixFileFilter("png", INSENSITIVE),
            suffixFileFilter("gif", INSENSITIVE),
            suffixFileFilter("bmp", INSENSITIVE)
    );

    public static Collection<String> asList(String val) {
        return asList(val, "\\s*,\\s*");
    }

    public static Collection<String> asList(String val, String delim) {
        if(!isEmpty(val)) {
            return Arrays.asList(
                val.split(delim)
            );
        } else {
            return Collections.emptyList();
        }
    }

    public static class PictureDirectoryWalker extends DirectoryWalker<File> {
        private List<File> results;

        public PictureDirectoryWalker(IOFileFilter filter) {
            super(HiddenFileFilter.VISIBLE, filter, -1);
            results = new LinkedList<File>();
        }

        public void walk(File startDirectory)  throws IOException {
            super.walk(startDirectory, results);
        }

        public List<File> listPictures() {
          return results;
        }

        protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
          results.add(file);
        }
    }

    public static PictureDirectoryFileFilter PICTURE_DIRECTORY_FILE_FILTER = new PictureDirectoryFileFilter();

    private static class PictureDirectoryFileFilter implements FileFilter {
        /**
         * Tests whether or not the specified abstract pathname should be
         * included in a pathname list.
         *
         * @param pathname The abstract pathname to be tested
         * @return <code>true</code> if and only if <code>pathname</code>
         *         should be included
         */
        public boolean accept(File pathname) {
            //log.debug("Testing ["+pathname.getName()+"] for acceptance.");
            if(null != pathname.getName() && pathname.getName().length() > 0) {
                if(pathname.getName().matches("^\\d{6,8}.+")) {
                    return true;
                }
            }
            return false;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static PictureDirectoryComparator PICTURE_DIRECTORY_COMPARATOR = new PictureDirectoryComparator();
    
    private static class PictureDirectoryComparator implements Comparator<File> {
        public int compare(File L, File R) {
            return L.getName().compareTo( R.getName() );
        }
    }

    public static PictureCreateDateComparator PICTURE_CREATE_DATE_COMPARATOR = new PictureCreateDateComparator();

    private static class PictureCreateDateComparator implements Comparator<File> {
        public int compare(File L, File R) {
            try {
                Date left = lookupCreateDateOrModifiedDate(L);
                Date right = lookupCreateDateOrModifiedDate(R);

                return left.compareTo(right);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private Util() {
    }
}
