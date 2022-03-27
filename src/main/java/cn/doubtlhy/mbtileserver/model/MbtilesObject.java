package cn.doubtlhy.mbtileserver.model;

import cn.doubtlhy.mbtileserver.handlers.MBTilesReader;

public class MbtilesObject {

    private MBTilesReader mbTilesReader;

    private String imageFormat;

    public MbtilesObject(MBTilesReader reader, String format) {
        this.mbTilesReader = reader;
        this.imageFormat = format;
    }

    public MBTilesReader getMBTileReader() {
        return mbTilesReader;
    }

    public void setMBTileReader(MBTilesReader mbTilesReader) {
        this.mbTilesReader = mbTilesReader;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

}
