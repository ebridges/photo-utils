package tinfoil.picasa;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import tinfoil.Album;
import tinfoil.AlbumFactory;
import tinfoil.AuthenticationException;

/**
 * User: ebridges
 * Date: 7/29/11
 * Time: 6:27 AM
 */

public class FolderReaderThread implements Callable<Album> {
    private final static Logger logger = Logger.getLogger(FolderReaderThread.class);

    private final File folder;
    private final UploadConfiguration configuration;
    private final FileReaderExecutionService service;

    public FolderReaderThread(final File folder, final UploadConfiguration configuration) {
        this.configuration = configuration;
        this.folder = folder;
        this.service = new FileReaderExecutionService(this.configuration);
    }

    @Override
    public Album call() throws Exception {
        logger.debug(format("call(%s)", this.folder.getName()));
        final AlbumFactory albumFactory = AlbumFactory.newAlbumFactory(this.configuration);
        final AlbumInfo albumInfo = new AlbumInfo(this.folder);

        Album album = createAlbum(albumFactory, albumInfo);
        this.service.addFilesFromFolderToAlbum(this.folder, album);

        return album;
    }

    private Album createAlbum(AlbumFactory albumFactory, AlbumInfo albumInfo) throws IOException, AuthenticationException {
        if(configuration.overwriteAlbum()) {
            albumFactory.deleteIfExists(albumInfo);
        }
        return albumFactory.newAlbum(albumInfo);
    }
}