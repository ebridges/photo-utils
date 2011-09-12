package tinfoil.picasa;

import java.io.File;

import tinfoil.Album;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 4:23 PM
 */
//@todo #1 Rename "PhotoUploadResult" to "PhotoRunStatus" to be consistent with AlbumRunStatus.
//@todo #1 change PhotoRunStatus#setSuccess() and PhotoRunStatus#setPartiallyProcessed to accept an ID
public class PhotoUploadResult {
    private final Album album;
    private final File photo;
    private String message;
    private Throwable error;

    public PhotoUploadResult(Album album, File photo) {
        this.album = album;
        this.photo = photo;
        this.message = "UNPROCESSED";
    }

    public Album getAlbum() {
        return album;
    }

    public File getPhoto() {
        return photo;
    }

    public Boolean isSuccessful() {
        return null == error;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }

    public void setSuccess() {
        this.message = "SUCCESS";
    }

    public void setPartiallyProcessed() {
        this.message = "PARTIALLY_PROCESSED";
    }

    public void setError(Throwable error) {
        if(null != error) {
            this.message = "ERROR";
            if(null == error.getCause()) {
                this.error = error;
            } else {
                this.error = error.getCause();
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PhotoUploadResult");
        sb.append("{successful=").append(isSuccessful());
        sb.append(", album=").append(album);
        sb.append(", photo=").append(photo);
        sb.append(", message='").append(message).append('\'');
        sb.append(", error=").append((null != error ? error.getMessage() : ""));
        sb.append('}');
        return sb.toString();
    }
}
