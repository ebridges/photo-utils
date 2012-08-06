package tinfoil;

import static java.lang.String.format;

/**
 * <b>Uploader</b>
 *
 * @author ebridges@tinfoil.biz
 */

public class Uploader {
    /*
    private static final Logger log = LoggerFactory.getLogger(Uploader.class);

    public static void main(String[] args) throws Exception {
        log.info("Begin uploading.");
        log.info(format("    sourceDirectory [%s]", Constants.PHOTO_DIRECTORY));
        log.info(format("    destinationLocation [%s]", Constants.SERVICE_URL));
        log.info(format("    username [%s]", Constants.SERVICE_USERNAME));
        Uploader uploader = new Uploader(
                new File(Constants.PHOTO_DIRECTORY),
                new URL(Constants.SERVICE_URL),
                Constants.SERVICE_USERNAME,
                Constants.SERVICE_PASSWORD
        );
        uploader.run();
        log.info("Uploading complete.");
    }

    private File sourceDirectory;
    private URL destinationLocation;
    private String username;
    private String password;

    public Uploader(File sourceDirectory, URL destinationLocation, String username, String password) {
        if(null == sourceDirectory) {
            throw new IllegalArgumentException("sourceDirectory cannot be null.");
        }
        if(!sourceDirectory.exists()) {
            throw new IllegalArgumentException("sourceDirectory does not exist");
        }
        if(null == destinationLocation) {
            throw new IllegalArgumentException("destinationLocation cannot be null.");
        }
        if(Util.isEmpty(username) || Util.isEmpty(password)) {
            throw new IllegalArgumentException("username & password cannot be empty.");
        }
        this.sourceDirectory = sourceDirectory;
        this.destinationLocation = destinationLocation;
        this.username = username;
        this.password = password;
    }

    public void run() throws IOException, AuthenticationException {
        log.info("run() called.");
        File[] directories = sourceDirectory.listFiles(PICTURE_DIRECTORY_FILE_FILTER);
        Arrays.sort(directories, PICTURE_DIRECTORY_COMPARATOR);
        if(log.isDebugEnabled()) {
            log.debug("located ["+directories.length+"] album directories in directory ["+sourceDirectory+"]");
        }
        
        if(null != directories && directories.length > 0) {
            log.debug("["+directories.length+"] Album directories to be uploaded.");
            AlbumFactory albumFactory = AlbumFactory.newAlbumFactory(destinationLocation, username, password);
            for( File file : directories ) {
                assert file != null;
                String albumName = file.getName();
                if(albumName == null || albumName.length() < 1) {
                    log.warn("Unable to get albumName from file ["+file+"]");
                    continue;
                }

                Date albumDate = extractDate(albumName);
                if(null == albumDate) {
                    log.warn("Directory name in wrong format ["+albumName+"]");
                    continue;
                }
                log.debug("Album date: ["+albumDate+"]");

                String name = makeName(albumName);
                log.debug("Made album name: ["+name+"] from folder name: ["+albumName+"]");

                List<String> keywords = makeKeywords(albumName);
                log.debug("["+keywords.size()+"] keywords for album. Keywords are: "+keywords+"");

                Album album = albumFactory.newAlbum(name, albumDate, keywords, albumName);
                log.info("Album ["+name+"] successfully created");

                File[] pictures = file.listFiles((FileFilter)MULTIMEDIA_FILE_FILTER);
                log.debug("["+pictures.length+"] Photos in album ["+albumName+"].");

                album.populate(pictures);
                log.info("["+pictures.length+"] pictures added to album ["+albumName+"].");

                waitSome(15);
            }
            log.debug("All albums uploaded.");
        }
    }

    private String makeName(String albumName) {
        String[] parts = albumName.split("_", 2);
        if(parts.length > 1) {
            return parts[1].replaceAll("_", " ");
        } else
            return "";
    }

    private Date extractDate(String s) {
        if(s.matches("^\\d{8}.+")) {
            return Util.toDate(s.substring(0,8), "yyyyMMdd");
        } else if(s.matches("^\\d{6}.+")) {
            return Util.toDate(s.substring(0,6), "yyyyMM");
        } else
            return null;
    }

    private List<String> makeKeywords(String name) {
        String[] words = name.split("_");
        ArrayList<String> keywords = new ArrayList<String>(words.length);
        for(String word : words) {
            if(!ignoreWord(word)) {
                keywords.add(word);
            }
        }
        return Collections.unmodifiableList(keywords);
    }

    private static final Map<String, Boolean> IGNORE = new HashMap<String, Boolean>();

    static {
        IGNORE.put("an", true);
        IGNORE.put("a", true);
        IGNORE.put("and", true);

        IGNORE.put("aboard", true);
        IGNORE.put("about", true);
        IGNORE.put("above", true);
        IGNORE.put("across", true);
        IGNORE.put("after", true);
        IGNORE.put("against", true);
        IGNORE.put("along", true);
        IGNORE.put("alongside", true);
        IGNORE.put("amid", true);
        IGNORE.put("amidst", true);
        IGNORE.put("among", true);
        IGNORE.put("amongst", true);
        IGNORE.put("around", true);
        IGNORE.put("as", true);
        IGNORE.put("aside", true);
        IGNORE.put("astride", true);
        IGNORE.put("at", true);
        IGNORE.put("athwart", true);
        IGNORE.put("atop", true);
        IGNORE.put("barring", true);
        IGNORE.put("before", true);
        IGNORE.put("behind", true);
        IGNORE.put("below", true);
        IGNORE.put("beneath", true);
        IGNORE.put("beside", true);
        IGNORE.put("besides", true);
        IGNORE.put("between", true);
        IGNORE.put("beyond", true);
        IGNORE.put("by", true);
        IGNORE.put("circa", true);
        IGNORE.put("concerning", true);
        IGNORE.put("despite", true);
        IGNORE.put("down", true);
        IGNORE.put("during", true);
        IGNORE.put("except", true);
        IGNORE.put("failing", true);
        IGNORE.put("following", true);
        IGNORE.put("for", true);
        IGNORE.put("from", true);
        IGNORE.put("given", true);
        IGNORE.put("in", true);
        IGNORE.put("inside", true);
        IGNORE.put("into", true);
        IGNORE.put("like", true);
        IGNORE.put("mid", true);
        IGNORE.put("minus", true);
        IGNORE.put("near", true);
        IGNORE.put("next", true);
        IGNORE.put("notwithstanding", true);
        IGNORE.put("of", true);
        IGNORE.put("off", true);
        IGNORE.put("on", true);
        IGNORE.put("onto", true);
        IGNORE.put("opposite", true);
        IGNORE.put("out", true);
        IGNORE.put("outside", true);
        IGNORE.put("over", true);
        IGNORE.put("pace", true);
        IGNORE.put("past", true);
        IGNORE.put("per", true);
        IGNORE.put("plus", true);
        IGNORE.put("regarding", true);
        IGNORE.put("round", true);
        IGNORE.put("save", true);
        IGNORE.put("since", true);
        IGNORE.put("than", true);
        IGNORE.put("through", true);
        IGNORE.put("throughout", true);
        IGNORE.put("till", true);
        IGNORE.put("times", true);
        IGNORE.put("to", true);
        IGNORE.put("toward", true);
        IGNORE.put("towards", true);
        IGNORE.put("under", true);
        IGNORE.put("underneath", true);
        IGNORE.put("unlike", true);
        IGNORE.put("until", true);
        IGNORE.put("up", true);
        IGNORE.put("upon", true);
        IGNORE.put("versus", true);
        IGNORE.put("via", true);
        IGNORE.put("with", true);
        IGNORE.put("within", true);
        IGNORE.put("without", true);
        IGNORE.put("worth", true);
    }

    private boolean ignoreWord(String word) {
        //log.debug("Testing ["+word+"] whether it qualifies as a keyword.");
        if(null != word && word.trim().length() > 0) {
            //if word starts with a lower-case letter, then skip it.
            if(Character.isLowerCase(word.charAt(0))){
                return true;
            }

            // otherwise if word is in ignore-list, then skip it.
            String w = word.trim().toLowerCase();
            return (IGNORE.containsKey(w) || w.matches("^\\d{6,8}$"));
        }
        return false;
    }
    */
}
