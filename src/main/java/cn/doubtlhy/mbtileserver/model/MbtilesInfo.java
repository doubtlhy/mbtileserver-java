package cn.doubtlhy.mbtileserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

public class MbtilesInfo {
    public String name;
    @JsonIgnore
    public String fullPath;
    @JsonIgnore
    public File mbFile;
    public String imageType;
    public String url;
    @JsonIgnore
    public String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getMbFile() {
        return mbFile;
    }

    public void setMbFile(File mbFile) {
        this.mbFile = mbFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

}
