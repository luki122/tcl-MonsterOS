package cn.tcl.music.model;

public class PlaylistInfo {
    public int id;
    public String name;
    public String description;
    public int type;
    public String artwork;
    public String path;

    public PlaylistInfo() {
    }

    public PlaylistInfo(int id, String name, String description, int type, String artwork, String path) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.artwork = artwork;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getArtwork() {
        return artwork;
    }

    public void setArtwork(String artwork) {
        this.artwork = artwork;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
