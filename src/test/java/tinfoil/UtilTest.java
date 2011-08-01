package tinfoil;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static tinfoil.Util.isEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * User: ebridges
 * Date: 7/31/11
 * Time: 5:09 PM
 */
public class UtilTest {
    private final static Logger logger = Logger.getLogger(UtilTest.class);
    
    @Test
    public void testLookupCreateDate_Jpeg() throws Exception {
        ImageInfo image = loadImageInfo("TEST.jpeg");
        Date expected = image.getCreateDate();
        Date actual = Util.lookupCreateDateAlt2(image.getImage());

        logger.info(format("expected [%s] / actual [%s]", expected, actual));
        assertEquals(expected, actual);
    }

    @Test
    public void testLookupCreateDate_Png() throws Exception {
        ImageInfo image = loadImageInfo("TEST.png");
        Date expected = image.getCreateDate();
        Date actual = Util.lookupCreateDateOrModifiedDate(image.getImage());

        logger.info(format("expected [%s] / actual [%s]", expected, actual));
        assertEquals(expected, actual);
    }

    @Test
    public void testLookupCreateDate_Gif() throws Exception {
        ImageInfo image = loadImageInfo("TEST.gif");
        Date expected = image.getCreateDate();
        Date actual = Util.lookupCreateDateOrModifiedDate(image.getImage());

        logger.info(format("expected [%s] / actual [%s]", expected, actual));
        assertEquals(expected, actual);
    }

    @Test
    public void testLookupCreateDate_Bmp() throws Exception {
        ImageInfo image = loadImageInfo("TEST.bmp");
        Date expected = image.getCreateDate();
        Date actual = Util.lookupCreateDateOrModifiedDate(image.getImage());

        logger.info(format("expected [%s] / actual [%s]", expected, actual));
        assertEquals(expected, actual);
    }

    @Test
    public void testLookupCreateDate_Tif() throws Exception {
        ImageInfo image = loadImageInfo("TEST.tif");
        Date expected = image.getCreateDate();
        Date actual = Util.lookupCreateDateAlt2(image.getImage());

        logger.info(format("expected [%s] / actual [%s]", expected, actual));
        assertEquals(expected, actual);
    }

    private ImageInfo loadImageInfo(String filename) throws IOException {
        String propertiesPath = format("src/test/resources/tinfoil/UtilTest/%s.properties", filename);
        String imagePath = format("src/test/resources/tinfoil/UtilTest/%s", filename);

        File file = new File(imagePath);
        if(!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException("can't access test image at "+file.getAbsolutePath());
        }

        File propertiesFile = new File(propertiesPath);
        Properties metadata = new Properties();
        metadata.load(new BufferedReader(new FileReader(propertiesFile)));
        String dateString = metadata.getProperty("create_date");
        Date createDate = null;
        if(!isEmpty(dateString))
            createDate = Util.toDate(dateString, "yyyy-MM-dd'T'HH:mm:ss");

        return new ImageInfo(file, createDate);
    }
}

class ImageInfo {
    private final File image;
    private final Date createDate;

    ImageInfo(File image, Date createDate) {
        this.image = image;
        this.createDate = createDate;
    }

    public File getImage() {
        return image;
    }

    public Date getCreateDate() {
        return createDate;
    }
}