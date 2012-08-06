/*
 * Copyright (c) 2011. Edward Q. Bridges <ebridges@gmail.com>
 * Licensed under the GNU Lesser General Public License v.3.0
 * http://www.gnu.org/licenses/lgpl.html
 */

package tinfoil.picasa;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import tinfoil.Album;

/**
 * User: ebridges
 * Date: 8/2/11
 * Time: 6:11 AM
 */
public class FileUploadException extends IOException {
    private final Album album;
    private final File photo;

    public FileUploadException(final Album album, final File photo, final Throwable cause) {
        super(format("failed when uploading photo [%s] to album [%s]", photo.getName(), album.getAlbumInfo().getAlbumName()), (cause.getCause() == null ? cause : cause.getCause()));
        this.album = album;
        this.photo = photo;
    }

    public FileUploadException(final Album album, final File photo, String message, final Throwable cause) {
        super(format("failed when %s using [%s/%s]", message, album.getAlbumInfo().getAlbumName(), photo.getName()), (cause.getCause() == null ? cause : cause.getCause()));
        this.album = album;
        this.photo = photo;
    }

    public Album getAlbum() {
        return album;
    }

    public File getPhoto() {
        return photo;
    }
}
