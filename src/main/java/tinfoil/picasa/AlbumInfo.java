/*
 * Copyright (c) 2011. Edward Q. Bridges <ebridges@gmail.com>
 * Licensed under the GNU Lesser General Public License v.3.0
 * http://www.gnu.org/licenses/lgpl.html
 */

package tinfoil.picasa;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static tinfoil.Constants.APPLICATION_AGENT;
import static tinfoil.Util.toDate;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * User: ebridges
 * Date: 7/30/11
 * Time: 10:01 AM
 */
public class AlbumInfo {
    private final static Logger logger = Logger.getLogger(AlbumInfo.class);

    private final File folder;
    private final String folderName;
    private final Date albumDate;
    private final String albumName;
    private final List<String> keywords;
    private final String description;

    public AlbumInfo(File folder) {
        if(null == folder)
            throw new IllegalArgumentException("folder cannot be null");

        this.folder = folder;
        this.folderName = this.folder.getName();
        
        this.albumDate = extractDate(folderName);
        if(null == albumDate) {
            throw new IllegalArgumentException(format("Folder name in wrong format [%s]", albumDate));
        }
        logger.debug(format("Album date: [%s]", albumDate));

        this.albumName = makeName(this.folderName);
        logger.debug(format("Made album name: [%s] from folder name: [%s]", this.albumName, this.folderName));

        this.keywords = makeKeywords(this.folderName);
        logger.debug(format("Extracted [%d] keywords for album. Keywords are: %s",keywords.size(), keywords));

        this.description = format("Album uploaded on %s by [%s]", new Date(), APPLICATION_AGENT);
    }

    public File getFolder() {
        return folder;
    }

    public String getFolderName() {
        return folderName;
    }

    public Date getAlbumDate() {
        return albumDate;
    }

    public String getAlbumName() {
        return albumName;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public String getDescription() {
        return description;
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
            return toDate(s.substring(0, 8), "yyyyMMdd");
        } else if(s.matches("^\\d{6}.+")) {
            return toDate(s.substring(0, 6), "yyyyMM");
        } else
            return null;
    }

    private List<String> makeKeywords(String name) {
        String[] words = name.split("_");
        List<String> keywords = new ArrayList<String>(words.length);
        for(String word : words) {
            if(!ignoreWord(word)) {
                keywords.add(word);
            }
        }
        return unmodifiableList(keywords);
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
}
