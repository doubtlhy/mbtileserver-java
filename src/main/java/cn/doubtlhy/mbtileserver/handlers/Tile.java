package cn.doubtlhy.mbtileserver.handlers;

import java.io.InputStream;

public class Tile {
    private int zoom;
    private int column;
    private int row;
    private InputStream data;

    public Tile(int zoom, int column, int row, InputStream data) {
        this.zoom = zoom;
        this.column = column;
        this.row = row;
        this.data = data;
    }

    public InputStream getData() {
        return data;
    }

    public int getZoom() {
        return zoom;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
}