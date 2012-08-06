package tinfoil.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * <b>DigikamAlbumDAOImpl</b>
 *
 * @author ebridges@tinfoil.biz
 */


class DigikamAlbumDAOImpl implements DigikamAlbumDAO {
    private Connection connection;
    public DigikamAlbumDAOImpl(Connection c) {
        this.connection = c;
    }

    public void destroy() {
        if(null != connection) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to close DB connection: ["+e.getMessage()+"]", e);
            }
        }
    }

    public List<DigikamAlbum> listAllDigikamAlbums() {
        List<DigikamAlbum> list = null;

        return list;
    }
}
