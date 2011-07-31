package tinfoil.picasa;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import tinfoil.Credentials;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import static tinfoil.Util.asList;

/**
 * User: ebridges
 * Date: 7/26/11
 * Time: 9:23 PM
 */
public class UploadConfiguration {
    private final static Logger logger = Logger.getLogger(UploadConfiguration.class);

    public static final int DEFAULT_FOLDER_THREAD_POOL_SIZE = 1;
    public static final int DEFAULT_FILE_THREAD_POOL_SIZE = 20;

    private final String rootDirectory;
    private final Integer folderThreadPoolSize;
    private final Integer fileThreadPoolSize;
    private final SortedSet<String> folderList;
    private final Credentials credentials;
    private final boolean overwriteAlbum;

    private URL serviceUrl;

    public UploadConfiguration(CommandLine cmd) {
        this.rootDirectory = cmd.getOptionValue(Argument.rootDir.argName());
        
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
        sb.append(", folderList=").append(folderList);
        sb.append(", credentials=").append(credentials);
        sb.append(", serviceUrl=").append(serviceUrl);
        sb.append(", overwriteAlbum=").append(overwriteAlbum);
        sb.append('}');
        return sb.toString();
    }

    public Integer getFileThreadPoolSize() {
        return fileThreadPoolSize;
    }
}