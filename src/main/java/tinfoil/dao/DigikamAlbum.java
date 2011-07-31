package tinfoil.dao;

import tinfoil.Util;

import java.util.Date;

/**
 * <b>DigikamAlbum</b>
 *
 * @author ebridges@tinfoil.biz
 */


public class DigikamAlbum {
    private Long id;
    private String url;
    private Date date;
    private String caption;
    private String collection;
    private String icon;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if(Util.isEmpty(url)) {
            throw new IllegalArgumentException("Album URL cannot be null.");
        }
        this.url = url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DigikamAlbum that = (DigikamAlbum) o;

        if (!url.equals(that.url))
            return false;

        return true;
    }

    public int hashCode() {
        return url.hashCode();
    }
}
