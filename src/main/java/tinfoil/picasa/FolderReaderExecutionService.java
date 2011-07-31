package tinfoil.picasa;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

import static tinfoil.Util.PICTURE_DIRECTORY_FILE_FILTER;
import static tinfoil.Util.isEmpty;

/**
 * User: ebridges
 * Date: 7/26/11
 * Time: 9:30 PM
 */
public class FolderReaderExecutionService {
    private static final Logger logger = Logger.getLogger(FolderReaderExecutionService.class);

    private final ExecutorService executor;
    private final UploadConfiguration configuration;
    private final CompletionService<String> completionService;

    public FolderReaderExecutionService(UploadConfiguration configuration) {
        this.configuration = configuration;

        this.executor = new SerialExecutor(
            newFixedThreadPool(
                this.configuration.getFolderThreadPoolSize()
            )
        );

        this.completionService = new ExecutorCompletionService<String>(
            this.executor
        );
    }

    public void run() {
        logger.debug("beginning uploader service.");

        Set<Future<String>> futures = new HashSet<Future<String>>();
 
         for(Callable<String> data : getUploadData()) {
            futures.add(
                this.completionService.submit(data)
            );
        }

        Future<String> completedFuture;
        String result = null;

        while (futures.size() > 0) {
            // block until a callable completes
            try {
                completedFuture = this.completionService.take();
                if(null == completedFuture) {
                    logger.warn("got null completedFuture.");
                    continue;
                }

                futures.remove(completedFuture);

                try {
                    result = completedFuture.get();
                    logger.info(format("task completed: %s", result));

                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    logger.warn("completion service failed. (result: "+ result + ")", cause);

                    this.executor.shutdown();
                    
                    for (Future<String> f: futures) {
                        // pass true if you wish to cancel in-progress Callables as well as pending Callables
                        f.cancel(true);
                    }

                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * This will first check to see if there's a 'root' directory and will use that, otherwise it will fall back to
     * a given list of folders.
     *
     * If given a root directory, and this process has read access to the folder, it assumes that that folder will contain a
     * list of subfolders.  These subfolders are considered "albums," each containing a set of photos.
     *
     * If both the rootDirectory and folderList options are provided, this will use only the given list of folders in the
     * given root directory (if they exist).
     *
     * @return Iterable<Callable<String>> Collection of threads, each dedicated to reading an album folder.
     */
    @SuppressWarnings({"JavaDoc"})
    private Iterable<FolderReaderThread> getUploadData() {
        String rootDirectory = getConfiguration().getRootDirectory();
        Collection<String> folders = getConfiguration().getFolderList();

        if(!isEmpty(rootDirectory)) {
            if(!folders.isEmpty()) {
                // a. upload a specified list of folders under a specified root folder
                int sz = folders.size();
                logger.info(format("uploading %d folders from root folder %s", sz, rootDirectory));
                Collection<FolderReaderThread> callables = new ArrayList<FolderReaderThread>(sz);
                for(final String folder : folders) {
                    final File subFolder = new File(rootDirectory, folder);
                    if(subFolder.exists() && subFolder.canRead()) {
                        callables.add(
                            new FolderReaderThread(subFolder, this.configuration)
                        );
                    } else {
                        logger.warn(format("Unable to access folder [%s]", subFolder.getAbsolutePath()));
                    }
                }
                return callables;
            } else {
                // b. upload all subfolders of a specified root folder
                logger.info(format("uploading all folders from root folder %s", "rootfolder"));
                File rootFolder = new File(rootDirectory);
                File[] subFolders = rootFolder.listFiles(PICTURE_DIRECTORY_FILE_FILTER);
                logger.info(format("uploading %d folders from root folder %s", subFolders.length, rootDirectory));
                Collection<FolderReaderThread> callables = new ArrayList<FolderReaderThread>(subFolders.length);
                for(File subFolder : subFolders) {
                    if(subFolder.exists() && subFolder.canRead()) {
                        callables.add(
                            new FolderReaderThread(subFolder, this.configuration)
                        );
                    } else {
                        logger.warn(format("Unable to access folder [%s]", subFolder.getAbsolutePath()));
                    }
                }
                return callables;
            }
        } else if(!folders.isEmpty()) {
            // c. upload from a specified list of folders
            int sz = folders.size();
            logger.info(format("uploading from %d folders", sz));
            Collection<FolderReaderThread> callables = new ArrayList<FolderReaderThread>(sz);
            for(final String folder : folders) {
                final File subFolder = new File(folder);
                if(subFolder.exists() && subFolder.canRead()) {
                    callables.add(
                        new FolderReaderThread(subFolder, this.configuration)
                    );
                } else {
                    logger.warn(format("Unable to access folder [%s]", subFolder.getAbsolutePath()));
                }
            }
            return callables;
        } else {
            throw new IllegalArgumentException("no folders for upload specified.");
        }
    }

    protected UploadConfiguration getConfiguration() {
        return this.configuration;
    }

    public void shutdown() {
        logger.debug("shutting down uploader service.");
        if(!this.executor.isShutdown()) {
            this.executor.shutdownNow();
        }
    }
}

class SerialExecutor<T> extends AbstractExecutorService {
     final Queue<Runnable> tasks = new ArrayDeque<Runnable>();
     final ExecutorService executor;
     private Runnable active;

     SerialExecutor(ExecutorService executor) {
         this.executor = executor;
     }

    @Override
    public void shutdown() {
        this.executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return this.executor.awaitTermination(l, timeUnit);
    }

    @Override
    public void execute(final Runnable runnable) {
        tasks.offer(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } finally {
                        scheduleNext();
                    }
                }
            }
        );

        if(active == null) {
            scheduleNext();
        }
    }

     protected synchronized void scheduleNext() {
         if ((active = tasks.poll()) != null) {
             executor.submit(active);
         }
     }
 }