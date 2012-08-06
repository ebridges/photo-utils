package tinfoil.dao;

import tinfoil.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <b>DigikamAlbumDAOFactory</b>
 *
 * @author ebridges@tinfoil.biz
 */


public class DigikamAlbumDAOFactory {
    static {
        try {
            Class.forName(Constants.DIGIKAM_DATABASE_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to locate class ["+Constants.DIGIKAM_DATABASE_DRIVER+"]", e);
        }
    }

    public static DigikamAlbumDAO instance() throws SQLException {
        Connection connection = DriverManager.getConnection(Constants.DIGIKAM_DATABASE_URL);
        return new DigikamAlbumDAOImpl(connection);
    }
}
