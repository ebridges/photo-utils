package tinfoil.sorter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tinfoil.Util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static tinfoil.Util.changeFileExtension;
import static tinfoil.Util.formatDate;
import static tinfoil.Util.isVideo;

/**
 * Date: 6/22/11
 * Time: 5:57 AM
 */
public class PictureSorter {
    private static final Logger log = LoggerFactory.getLogger(PictureSorter.class);

    private static final String IMAGE_BASENAME = "img";

    private static final String VIDEO_BASENAME = "vid";

    private static final Map<String,String> BASENAME_MAP = new HashMap<String,String>();

    static {
        BASENAME_MAP.put("jpeg",IMAGE_BASENAME);
        BASENAME_MAP.put("jpg",IMAGE_BASENAME);
        BASENAME_MAP.put("thm", VIDEO_BASENAME);
        BASENAME_MAP.put("mov", VIDEO_BASENAME);
        BASENAME_MAP.put("avi",VIDEO_BASENAME);
        BASENAME_MAP.put("mp4", VIDEO_BASENAME);
    }

    private File source;
    private File destination;

    public PictureSorter(File sourceDirectory, File destinationDirectory) {
        if(null == sourceDirectory) {
            throw new IllegalArgumentException("sourceDirectory cannot be null.");
        }
        if(!sourceDirectory.exists()) {
            throw new IllegalArgumentException("sourceDirectory does not exist");
        }
        if(null == destinationDirectory) {
            throw new IllegalArgumentException("destinationLocation cannot be null.");
        }
        if(!destinationDirectory.exists()) {
            throw new IllegalArgumentException("destinationDirectory does not exist");
        }

        this.source = sourceDirectory;
        this.destination = destinationDirectory;
        log.info(format("Sorting photos from %s into %s.", this.source.getAbsolutePath(), this.destination.getAbsolutePath()));
    }

    public void run() {
        log.debug("run() called.");
        String pictureName = null;
        File destPath = null;

        Util.PictureDirectoryWalker srcWalker = new Util.PictureDirectoryWalker(Util.MULTIMEDIA_FILE_FILTER);
        Util.PictureDirectoryWalker destWalker = new Util.PictureDirectoryWalker(Util.MULTIMEDIA_FILE_FILTER);
        try {
            srcWalker.walk(this.source);
            List<File> pictures = srcWalker.listPictures();
            log.info(format("found %d pictures to sort.", pictures.size()));
            
            for(File picture : pictures) {
                log.debug(format("copying file %s", picture.getAbsolutePath()));
                pictureName = picture.getName();
                Date createDate = Util.lookupCreateDateOrModifiedDate(picture);

                String destDir = formatDate(createDate, "yyyyMMdd");
                destPath = new File(destination, destDir);
                if(!destPath.exists()) {
                    forceMkdir(destPath);
                }

                destPath = renameIfExists(destPath, picture);

                if(null != destPath) {
                    log.info(format("copying file %s to %s",picture.getName(), destPath.getAbsolutePath()));
                    copyFileToDirectory(picture, destPath);
                } else {
                    log.info((format("file %s is a duplicate, skipping.", picture.getAbsolutePath())));
                }
            }

            destWalker.walk(this.destination);
            List<File> destPictures = destWalker.listPictures();
            Map<File,List<File>> picturesByDir = sortIntoDirectories(destPictures);
            log.info(format("found %d pictures to rename. %d sorted into directories", destPictures.size(), picturesByDir.size()));

            for(File parent : picturesByDir.keySet()) {
                List<File> sortedByCreate = picturesByDir.get(parent);
                Collections.sort( sortedByCreate, Util.PICTURE_CREATE_DATE_COMPARATOR );
                log.info(format("renaming %d pictures in parentDir %s", sortedByCreate.size(), parent.getAbsolutePath()));
                int cnt = 1;
                for(File file : sortedByCreate) {
                    // skip THM files, because they're treated together with video files
                    if(FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase("THM")) {
                        continue;
                    }

                    String newName = generateNewName(file, cnt);
                    File newFile = new File(file.getParentFile(), newName);
                    log.debug(format("    renaming %s to %s", file.getAbsolutePath(), newFile.getAbsolutePath()));
                    boolean succeeded = file.renameTo(newFile);
                    if(!succeeded) {
                        throw new IllegalArgumentException(format("unable to rename %s to %s", file.getAbsolutePath(), newName));
                    }

                    if(isVideo(file)) {
                        //rename THM file if it exists
                        File thumbnailFile = changeFileExtension(file, "THM");
                        if(thumbnailFile.exists()) {
                            String newThumbnailName = generateNewName(thumbnailFile, cnt);
                            File newThumbnailFile = new File(file.getParentFile(), newThumbnailName);
                            succeeded = thumbnailFile.renameTo(newThumbnailFile);
                            if(!succeeded) {
                                throw new IllegalArgumentException(format("unable to rename %s to %s", file.getAbsolutePath(), newName));
                            }
                        }
                    }

                    cnt++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(format("caught IOException when copying file: %s to directory: %s", pictureName, (destPath != null ? destPath.getName() : "[DEST FILE ALREADY EXISTS]")), e);
        }
    }

    private Map<File, List<File>> sortIntoDirectories(List<File> pictures) {
        Map<File, List<File>> resultsByDirectory = new HashMap<File, List<File>>();
        for(File p : pictures) {
            if(!resultsByDirectory.containsKey(p.getParentFile())) {
                resultsByDirectory.put(p.getParentFile(), new LinkedList<File>());
            }
            resultsByDirectory.get(p.getParentFile()).add(p);
        }
        return resultsByDirectory;
    }

    private String generateNewName(File name, int cnt) {
        String ext =  FilenameUtils.getExtension(name.getAbsolutePath()).toLowerCase();
        return format("%s-%03d.%s", BASENAME_MAP.get(ext), cnt, ext);
    }

    //private Map<File, Integer> DIRS_SEEN = new HashMap<File,Integer>();
    //private Map<File, Integer> FILES_SEEN = new HashMap<File, Integer>();
    private File renameIfExists(File destPath, File picture) {
        final File fqpn = new File(destPath.getAbsolutePath(), picture.getName());
      //  increment(FILES_SEEN, fqpn);

     //   if(!DIRS_SEEN.containsKey(destPath)) {
     //       DIRS_SEEN.put(destPath, 1);
     //   }

        if(fqpn.exists()) {
            /*
            String newName = format(
                    "%s-%d.%s",
                    FilenameUtils.getBaseName(picture.getName()),
                    FILES_SEEN.get(fqpn),
                    FilenameUtils.getExtension(picture.getName())
            );
            */

            try {
                log.debug(format("comparing %s to %s", fqpn.getAbsolutePath(), picture.getAbsolutePath()));
                if(fqpn.isDirectory()) {
                    log.debug("fqpn is a directory");
                }
                if(picture.isDirectory()) {
                    log.debug("picture is a directory");
                }

                if(!FileUtils.contentEquals(fqpn, picture)) {
                    File dupePath = new File(destPath, "dupes");
  //                  log.info(format("file %s exists, moving to %s", picture.getName(), dupePath.getAbsolutePath()));
                    if(!dupePath.exists()) {
                        boolean succeeded = dupePath.mkdirs();
                        if(!succeeded) {
                            throw new IllegalArgumentException(format("unable to create %s", dupePath.getAbsolutePath()));
                        }
                    }
                    return dupePath;
                } else {
                    log.info("ignoring a duplicate file at "+picture.getAbsolutePath());
                    return null;
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return destPath;
    }

    private Integer increment(Map<File, Integer> map, File key) {
        Integer ret;
        if(map.containsKey(key)) {
            Integer i = map.get(key);
            i++;
            map.put(key, i);
            ret = i;
        } else {
            map.put(key, 1);
            ret = 1;
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        log.info("Begin sorting pictures.");
        log.info(format("    sourceDirectory [%s]", args[0]));
        log.info(format("    destinationDirectory [%s]", args[1]));

        PictureSorter sorter = new PictureSorter( new File(args[0]), new File(args[1]) );
        sorter.run();
        log.info("Sorting complete.");
    }
}
