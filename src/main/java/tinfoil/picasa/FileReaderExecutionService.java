package tinfoil.picasa;

import static java.lang.String.format;
import static java.util.Collections.synchronizedList;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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

    private static ConcurrentMap<File, List<PhotoUploadResult>> COMPLETED = new ConcurrentHashMap<File, List<PhotoUploadResult>>();

    private final ExecutorService executor;
    private final UploadConfiguration configuration;
    private final CompletionService<PhotoUploadResult> completionService;

    public FileReaderExecutionService(final UploadConfiguration configuration) {
        this.configuration = configuration;

        this.executor = new SerialExecutor(
            newFixedThreadPool(
                this.configuration.getFileThreadPoolSize()
            )
        );

        this.completionService = new ExecutorCompletionService<PhotoUploadResult>(
            this.executor
        );
    }

    public static Map<File, List<PhotoUploadResult>> getCompleted() {
        return (COMPLETED);
    }

    public void addFilesFromFolderToAlbum(final File folder, final Album album) throws AuthenticationException {
        List<Callable<PhotoUploadResult>> photos = getPhotosToUpload(album, folder);
        Set<Future<PhotoUploadResult>> futures = new HashSet<Future<PhotoUploadResult>>();

        logger.info(format("adding %d photos to album [%s]", photos.size(), album.getAlbumInfo().getAlbumName()));

         for(Callable<PhotoUploadResult> photo : photos ) {
            futures.add(
                this.completionService.submit(photo)
            );
        }

        Future<PhotoUploadResult> completedFuture;
        PhotoUploadResult result = null;

        while (futures.size() > 0) {
            try {
                completedFuture = this.completionService.take();
                if(null == completedFuture) {
                    logger.warn("got null completedFuture.");
                    continue;
                }

                futures.remove(completedFuture);

                try {
                    result = completedFuture.get();

                    if(!COMPLETED.containsKey(folder)) {
                        COMPLETED.putIfAbsent(folder, synchronizedList(new LinkedList<PhotoUploadResult>()));
                    }
                    COMPLETED.get(folder).add(result);

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    logger.warn("completion service failed. (result: " + result + ")", cause);

                    for (Future<PhotoUploadResult> f: futures) {
                        f.cancel(true);
                    }

                    this.executor.shutdown();

                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private List<Callable<PhotoUploadResult>> getPhotosToUpload(final Album album, File folder) throws AuthenticationException {
        final File[] photos = folder.listFiles((FileFilter)Util.PHOTO_FILE_FILTER);
        final List<Callable<PhotoUploadResult>> callables = new ArrayList<Callable<PhotoUploadResult>>(photos.length);

        for(final File photo : photos) {
            if(photo.exists() && photo.canRead()) {
                callables.add(
                    new FileReaderThread(album, photo, this.configuration)
                );
            } else {
                logger.warn(format("Unable to access photo [%s]", photo.getAbsolutePath()));
            }
        }

        return unmodifiableList(callables);
    }
}
