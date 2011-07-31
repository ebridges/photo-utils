package tinfoil.dao;

import java.util.List;

/**
 * <b>DigikamAlbumDAO</b>
 *
 * @author ebridges@tinfoil.biz
 */


public interface DigikamAlbumDAO {
    void destroy();

    List<DigikamAlbum> listAllDigikamAlbums();
}
