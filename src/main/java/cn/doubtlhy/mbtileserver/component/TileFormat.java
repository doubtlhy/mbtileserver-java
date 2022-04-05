package cn.doubtlhy.mbtileserver.component;

/**
 * @author doubtlhy
 * @date 2022/4/5 14:57
 */
public enum TileFormat {
    UNKNOWN, // UNKNOWN TileFormat cannot be determined from first few bytes of tile
    GZIP, // encoding = gzip
    ZLIB, // encoding = deflate
    PNG,
    JPG,
    PBF,
    WEBP;

    @Override
    public String toString() {
        switch (this) {
            case PNG:
                return "png";
            case JPG:
                return "jpg";
            case PBF:
                return "pbf";
            case WEBP:
                return "webp";
            default:
                return "";
        }
    }

    public String getContentType() {
        switch (this) {
            case PNG:
                return "image/png";
            case JPG:
                return "image/jpeg";
            case WEBP:
                return "image/webp";
            case PBF:
                return "application/x-protobuf";
            default:
                return "";
        }
    }
}
