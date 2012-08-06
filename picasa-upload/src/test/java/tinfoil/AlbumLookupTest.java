/*
 * Copyright (c) 2011. Edward Q. Bridges <ebridges@gmail.com>
 * Licensed under the GNU Lesser General Public License v.3.0
 * http://www.gnu.org/licenses/lgpl.html
 */

package tinfoil;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.ServiceException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tinfoil.picasa.Argument;
import tinfoil.picasa.PhotoService;
import tinfoil.picasa.UploadConfiguration;

/**
 * User: ebridges
 * Date: 8/6/11
 * Time: 2:39 PM
 */
public class AlbumLookupTest {
    private static final Logger log = LoggerFactory.getLogger(AlbumLookupTest.class);

    private PicasawebService service;
    private UploadConfiguration configuration;

    @Before
    public void setUp() throws AuthenticationException {
        Map<Argument, String> params = new HashMap<Argument, String>();
        params.put(Argument.rootDir, "/Users/ebridges/Pictures/picasa-upload");
        this.configuration = new UploadConfiguration(Credentials.TEST, params);
        this.service = (new PhotoService().init(this.configuration.getCredentials()));
    }

    @Test
    public void lookupEntry() throws ServiceException, IOException {
        UserFeed userFeed = service.getFeed(new URL(this.configuration.getCredentials().serviceUrl()), UserFeed.class);
        List<GphotoEntry> entries = userFeed.getEntries();
        for (GphotoEntry entry : entries) {
          GphotoEntry adapted = entry.getAdaptedEntry();
            log.info(adapted.getId());
          if (adapted instanceof AlbumEntry) {
              AlbumEntry e = (AlbumEntry)adapted;
              m("access: %s",  e.getAccess());
              m("authors: %s", e.getAuthors());
              m("bytesUsed: %s", e.getBytesUsed());
              m("canEdit: %s", e.getCanEdit());
              m("categories: %s", e.getCategories());
              m("commentsEnabled/Count: %s/%s", e.getCommentsEnabled(), e.getCommentCount());
              m("content: %s", e.getContent());
              m("contributors: %s", e.getContributors());
              m("date: %s", e.getDate());
              m("description: %s", e.getDescription());
              m("editLink: %s", e.getEditLink().getHref());
              m("edited: %s", e.getEdited());
              m("eTag: %s", e.getEtag());
              m("extensionLocalName: %s", e.getExtensionLocalName());
              m("extensions: %s", e.getExtensions());
              m("feedLink: %s", e.getFeedLink().getHref());
              m("gphotoId: %s", e.getGphotoId());
              m("htmlLink: %s", e.getHtmlLink().getHref());
              m("id: %s", e.getId());
              m("kind: %s", e.getKind());
              m("location: %s", e.getLocation());
              m("name: %s", e.getName());
              m("nickname: %s", e.getNickname());
              m("photosLeft: %s", e.getPhotosLeft());
              m("photosUsed: %s", e.getPhotosUsed());
              m("title: %s", e.getTitle());
              m("username: %s", e.getUsername());
          }
        }
    }

    public void m(String msg, Object ... vals) {
        log.info(format("\t"+msg, vals));
    }
}
