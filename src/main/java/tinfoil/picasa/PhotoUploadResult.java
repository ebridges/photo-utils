package tinfoil.picasa;

import java.io.File;

import tinfoil.Album;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 4:23 PM
 */
public class PhotoUploadResult {
    private final Album album;
    private final File photo;
    private final String message;
    private final Throwable error;
    private final boolean successful;

    public PhotoUploadResult(Album album, File photo, String message) {
        this.album = album;
        this.photo = photo;
        this.message = message;
        this.error = null;
        this.successful = true;
    }

    public PhotoUploadResult(Album album, File photo, String message, Throwable error) {
        this.album = album;
        this.photo = photo;
        this.message = message;
        this.error = error;
        this.successful = (error == null);
    }

    public Album getAlbum() {
        return album;
    }

    public Boolean isSuccessful() {
        return successful;
    }

    public File getPhoto() {
        return photo;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }
}
