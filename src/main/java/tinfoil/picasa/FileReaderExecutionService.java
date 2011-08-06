/*
 * Copyright (c) 2011. Edward Q. Bridges <ebridges@gmail.com>
 * Licensed under the GNU Lesser General Public License v.3.0
 * http://www.gnu.org/licenses/lgpl.html
 */

package tinfoil.picasa;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import tinfoil.Album;
import tinfoil.AuthenticationException;
import tinfoil.Util;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 2:10 PM
 */
public class FileReaderExecutionService {
    private static final Logger logger = Logger.getLogger(FileReaderExecutionService.class);

    private static ConcurrentMap<Album, List<PhotoUploadResult>> COMPLETED = new ConcurrentHashMap<Album, List<PhotoUploadResult>>();

    private final ExecutorService executor;
    private final UploadConfiguration configuration;
    private final CompletionService<String> completionService;

    public FileReaderExecutionService(final UploadConfiguration configuration) {
        this.configuration = configuration;

        this.executor = newFixedThreadPool(
            this.configuration.getFileThreadPoolSize()
        );

        this.completionService = new ExecutorCompletionService<String>(
            this.executor
        );
    }

    public static Map<Album, List<PhotoUploadResult>> getCompleted() {
        return (COMPLETED);
    }

    public void addFilesFromFolderToAlbum(final File folder, final Album album) throws AuthenticationException, InterruptedException {
        List<FileReaderThread> photos = getPhotosToUpload(album, folder);
        Set<Future<String>> futures = new HashSet<Future<String>>();

        logger.info(format("adding %d photos to album [%s]", photos.size(), album.getAlbumInfo().getAlbumName()));

        for(FileReaderThread photo : photos ) {
            futures.add(
                this.completionService.submit(photo)
            );
        }

        Future<String> completedFuture;
        String result = null;

        try {
            while (futures.size() > 0) {
                completedFuture = this.completionService.take();
                if(null == completedFuture) {
                    logger.warn("got null completedFuture.");
                    continue;
                }
                futures.remove(completedFuture);
                result = completedFuture.get();
                
            }
        } catch (Throwable e) {
            Throwable t = e;
            if(null != e.getCause())
                t = e.getCause();
            logger.warn("upload failed when uploading [" + result + "], cause ["+t.getMessage()+"]",t);

            for (Future<String> f: futures) {
                f.cancel(true);
            }

            if(e instanceof InterruptedException)
                Thread.currentThread().interrupt();
        } finally {
            this.executor.shutdown();
            boolean success = this.executor.awaitTermination(5, TimeUnit.SECONDS);
            if (!success) {
                this.executor.shutdownNow();
            }
        }
    }

    private List<FileReaderThread> getPhotosToUpload(final Album album, final File folder) throws AuthenticationException {
        final File[] photos = folder.listFiles((FileFilter)Util.PHOTO_FILE_FILTER);
        final List<FileReaderThread> callables = new ArrayList<FileReaderThread>(photos.length);
        final List<PhotoUploadResult> results = new LinkedList<PhotoUploadResult>();

        for(final File photo : photos) {
            if(photo.exists() && photo.canRead()) {
                PhotoUploadResult result = new PhotoUploadResult(album, photo);
                results.add(result);
                callables.add(
                    new FileReaderThread(album, photo, this.configuration, result)
                );
            } else {
                logger.warn(format("Unable to access photo [%s]", photo.getAbsolutePath()));
            }
        }

        COMPLETED.put(album, unmodifiableList(results));

        return unmodifiableList(callables);
    }
}
