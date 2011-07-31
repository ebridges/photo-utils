package tinfoil.dao;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * <b>DigikamAlbumDAOTest</b>
 *
 * @author ebridges@tinfoil.biz
 */


public class DigikamAlbumDAOTest extends TestCase {
    private static final Logger log = Logger.getLogger(DigikamAlbumDAOTest.class);
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
