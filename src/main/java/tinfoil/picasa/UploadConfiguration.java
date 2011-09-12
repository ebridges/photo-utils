package tinfoil.picasa;

import static tinfoil.Util.asList;
import static tinfoil.Util.throwIfEmpty;
import static tinfoil.picasa.Argument.rootDir;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tinfoil.Credentials;

/**
 * User: ebridges
 * Date: 7/26/11
 * Time: 9:23 PM  260438
 */
public class UploadConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(UploadConfiguration.class);

    public static final int DEFAULT_FOLDER_THREAD_POOL_SIZE = 1;

    /**
     *   PoolSize  Secs
     *   --------------
     *      2      260
     *      4      199
     *      8      178
     *     16      146
     */
    public static final int DEFAULT_FILE_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors()*8;

    private final String rootDirectory;
    private final Integer folderThreadPoolSize;
    private final Integer fileThreadPoolSize;
    private final SortedSet<String> folderList;
    private final Credentials credentials;
    private final boolean overwriteAlbum;

    private URL serviceUrl;

    public UploadConfiguration(Credentials credentials, Map<Argument, String> parameters) {
        this.credentials = credentials;

/*
@todo #1 Add support for reading from a run-status file
If a run-status file is specified on the command line then read in the previous run's files from there to
be re-uploaded.  Support command line arguments:
   a. rerun all from file (deleting and overwriting previous run)
   b. rerun only the errors from file
Given the filename, this class should populate the rootDirectory and folderList members appropriately based on above.
The overwriteAlbum member should be deprecated.
*/

        this.rootDirectory = parameters.get(rootDir);
        throwIfEmpty(this.rootDirectory, rootDir.argName());

        this.folderList = parameters.containsKey(Argument.folderList) ?
                new TreeSet<String>( asList(parameters.get(Argument.folderList)) ) :
                new TreeSet<String>() ;
        this.overwriteAlbum = parameters.containsKey(Argument.overwriteAlbum);

        this.folderThreadPoolSize = parameters.containsKey(Argument.folderThreadPoolSize) ?
                Integer.valueOf(parameters.get(Argument.folderThreadPoolSize)) :
                DEFAULT_FOLDER_THREAD_POOL_SIZE;
        this.fileThreadPoolSize = parameters.containsKey(Argument.fileThreadPoolSize) ?
                Integer.valueOf(parameters.get(Argument.fileThreadPoolSize)) :
                DEFAULT_FILE_THREAD_POOL_SIZE;
        try {
            this.serviceUrl = new URL(this.credentials.serviceUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public UploadConfiguration(CommandLine cmd) {
        this.rootDirectory = cmd.getOptionValue(rootDir.argName());
        
        this.folderThreadPoolSize = cmd.hasOption(Argument.folderThreadPoolSize.argName()) ?
                Integer.valueOf(cmd.getOptionValue(Argument.folderThreadPoolSize.argName())) :
                DEFAULT_FOLDER_THREAD_POOL_SIZE;

        this.fileThreadPoolSize = cmd.hasOption(Argument.fileThreadPoolSize.argName()) ?
                Integer.valueOf(cmd.getOptionValue(Argument.fileThreadPoolSize.argName())) :
                DEFAULT_FILE_THREAD_POOL_SIZE;

        this.folderList = cmd.hasOption(Argument.folderList.argName()) ?
                new TreeSet<String>(asList(cmd.getOptionValue(Argument.folderList.argName()))) :
                new TreeSet<String>();

        String env = cmd.getOptionValue(Argument.environment.argName());
        this.credentials = Credentials.valueOf(env);

        try {
            this.serviceUrl = new URL(this.credentials.serviceUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

        this.overwriteAlbum = cmd.hasOption(Argument.overwriteAlbum.argName());

        if(logger.isDebugEnabled()) {
            logger.debug(toString());
        }
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public Integer getFolderThreadPoolSize() {
        return folderThreadPoolSize;
    }

    public Collection<String> getFolderList() {
        return Collections.unmodifiableSortedSet(this.folderList);
    }

    public URL getServiceUrl() {
        return serviceUrl;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public boolean overwriteAlbum() {
        return overwriteAlbum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("UploadConfiguration");
        sb.append("{rootDirectory='").append(rootDirectory).append('\'');
        sb.append(", folderThreadPoolSize=").append(folderThreadPoolSize);
        sb.append(", fileThreadPoolSize=").append(fileThreadPoolSize);
        sb.append(", folderList=").append(folderList);
        sb.append(", credentials=").append(credentials);
        sb.append(", overwriteAlbum=").append(overwriteAlbum);
        sb.append(", serviceUrl=").append(serviceUrl);
        sb.append('}');
        return sb.toString();
    }

    public Integer getFileThreadPoolSize() {
        return fileThreadPoolSize;
    }
}
