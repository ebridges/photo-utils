package tinfoil.dao;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * <b>DigikamAlbumDAOTest</b>
 *
 * @author ebridges@tinfoil.biz
 */


public class DigikamAlbumDAOTest extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(DigikamAlbumDAOTest.class);
    private DigikamAlbumDAO dao;

    public void testListAllDigikamAlbums() {
        List<DigikamAlbum> list = dao.listAllDigikamAlbums();
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = DigikamAlbumDAOFactory.instance();
    }

    protected void tearDown() throws Exception {
        dao.destroy();
        super.tearDown();
    }
}
