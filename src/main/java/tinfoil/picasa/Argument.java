/*
 * Copyright (c) 2011. Edward Q. Bridges <ebridges@gmail.com>
 * Licensed under the GNU Lesser General Public License v.3.0
 * http://www.gnu.org/licenses/lgpl.html
 */

package tinfoil.picasa;

/**
 * User: ebridges
 * Date: 7/27/11
 * Time: 6:16 AM
 */
public enum Argument {
    help("help", 'h', "Instructions on options."),
    rootDir("rootDir", 'd', "Root directory of photos.", true),
    folderThreadPoolSize("folderThreads", 'o', "Used for reading folders.", true),
    folderList("folderList", 'f', "List of album folders.", true),
    environment("environment", 'e', "Run against test or live.", true),
    overwriteAlbum("overwriteAlbum", 'X', "Remove album if it exists already."),
    fileThreadPoolSize("fileThreads", 'i', "Used for reading files.", true);

    private String description;
    private String argName;
    private Character arg;
    private boolean hasParameter;

    Argument(String argName, Character arg, String description) {
        this.description = description;
        this.arg = arg;
        this.argName = argName;
        this.hasParameter = false;
    }

    Argument(String argName, Character arg, String description, Boolean hasParameter) {
        this.description = description;
        this.arg = arg;
        this.argName = argName;
        this.hasParameter = hasParameter;
    }

    public boolean hasParameter() {
        return hasParameter;
    }

    public String description() {
        return description;
    }

    public String argName() {
        return argName;
    }

    public Character arg() {
        return arg;
    }
}
