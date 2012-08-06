package tinfoil;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gdata.data.photos.AlbumEntry;
import tinfoil.picasa.AlbumInfo;

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
    private final URL albumUrl;
    private final AlbumEntry albumEntry;
    private final AlbumInfo albumInfo;

    public PicasaAlbum(AlbumEntry album, AlbumInfo albumInfo) {
        this.albumEntry = album;
        this.albumInfo = albumInfo;

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

    @Override
    public String toString() {
        return String.format("Album [%s]",this.getAlbumInfo().getAlbumName());
    }
}
