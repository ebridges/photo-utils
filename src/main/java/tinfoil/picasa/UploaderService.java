/*
 * Copyright (c) 2011. Edward Q. Bridges <ebridges@gmail.com>
 * Licensed under the GNU Lesser General Public License v.3.0
 * http://www.gnu.org/licenses/lgpl.html
 */
package tinfoil.picasa;

import static java.lang.String.format;
import static org.apache.commons.cli.OptionBuilder.withArgName;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import tinfoil.Album;
import tinfoil.Constants;

/**
 * User: ebridges
 * Date: 7/26/11
 * Time: 9:20 PM
 */
public class UploaderService {
    private static final Logger logger = Logger.getLogger(UploaderService.class);

    private static final Options OPTIONS = new Options();
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();

    private FolderReaderExecutionService executor;
    private CommandLine commandLine;

    public UploaderService(String[] args) {
        UploadConfiguration configuration = parseArguments(args);
        if(null != this.commandLine){
            this.executor = new FolderReaderExecutionService(configuration);
        }
    }

    private UploadConfiguration parseArguments(String[] args) {
        logger.debug("parsing service arguments.");

        CommandLine cmd;
        CommandLineParser parser = new GnuParser();
        try {
            cmd = parser.parse( OPTIONS, args);
        } catch (MissingArgumentException e) {
            System.err.println(e.getMessage());
            return null;
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }

        assert null != cmd;
        this.commandLine = cmd;

        return new UploadConfiguration(cmd);
    }

    private void run() {
        try {
            this.executor.run();
        } catch (InterruptedException e) {
            logger.warn("caught interrupted exception.", e);
            Thread.currentThread().interrupt();
        }
    }

    private void shutdown() {
        this.executor.shutdown();
    }

    private boolean isReadyToRun() {
        boolean readyToRun = true;

        if(null == commandLine) {
            readyToRun = false;
        } else if(commandLine.hasOption("help")) {
            readyToRun = false;
        }

        return readyToRun;
    }

    public static void main(String[] args) {
        logger.info(format("started at: %s", new Date()));
        UploaderService uploaderService = new UploaderService(args);
        long start = System.nanoTime();

        if(uploaderService.isReadyToRun()) {
            try {
                uploaderService.run();
            } finally {
                uploaderService.shutdown();
            }

            uploaderService.reportResults(FileReaderExecutionService.getCompleted());
        } else {
            HELP_FORMATTER.printHelp(Constants.APPLICATION_NAME, OPTIONS);
        }

        long end = System.nanoTime();
        logger.info(format("finished in %.2f secs", ((end-start)/1000000000.0)));
    }

    private void reportResults(Map<Album, List<PhotoUploadResult>> completed) {
        for(Album f : completed.keySet()) {
            logger.info(format("Album [%s] (%d)",f.getAlbumInfo().getAlbumName(), completed.get(f).size()));
            for(PhotoUploadResult r : completed.get(f)) {
                if(r.isSuccessful()) {
                    logger.info(format("    [%s]: %s",r.getPhoto().getName(), r.getMessage()));
                } else {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    logger.info(format("    [%s]: %s (%s)",r.getPhoto().getName(), r.getMessage(), r.getError().getMessage()));
                }
            }
        }
    }

    static {
        addOption(Argument.help);
        addOption(Argument.rootDir);
        addOption(Argument.folderThreadPoolSize);
        addOption(Argument.folderList);
        addOption(Argument.environment);
        addOption(Argument.overwriteAlbum);
        addOption(Argument.fileThreadPoolSize);
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    private static void addOption(Argument arg) {
        Option o = withArgName(arg.argName())
                  .withLongOpt(arg.argName())
                  .withDescription(arg.description())
                  .hasArg(arg.hasParameter())
                  .create(arg.arg());
        OPTIONS.addOption(o);
    }
}
