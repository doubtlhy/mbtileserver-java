package cn.doubtlhy.mbtileserver.component;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doubtlhy
 * @date 2022/4/5 14:40
 */
public class Tileset {
    private static final Logger log = LoggerFactory.getLogger(Tileset.class);

    ServiceSet svc;
    Mbtiles db;
    String id;
    String name;
    TileFormat tileformat;
    Boolean published;
    String path;

    public Tileset(ServiceSet svc, Mbtiles db, String id, String name, TileFormat tileformat, Boolean published, String path) {
        this.svc = svc;
        this.db = db;
        this.id = id;
        this.name = name;
        this.tileformat = tileformat;
        this.published = published;
        this.path = path;
    }

    public static Tileset newTileset(ServiceSet svc, String filename, String id, String path) throws Exception {
        Mbtiles db;
        try {
            db = Mbtiles.newDB(filename);
        } catch (Exception e) {
            throw new Exception(String.format("Invalid mbtiles file %s: %s", filename, e.getMessage()), e);
        }

        Map<String, Object> metadata = db.readMetadata();
        String name = metadata.get("name") == null ? "" : metadata.get("name").toString();
        if (StringUtils.isEmpty(name)) {
            File f = new File(filename);
            name = f.getName().substring(0, f.getName().lastIndexOf("."));
        }
        return new Tileset(svc, db, id, name, db.getTileformat(), true, path);
    }

    public String getTileformatString() {
        return tileformat.toString();
    }

    public TileFormat getTileformat() {
        return tileformat;
    }

    public Mbtiles getDb() {
        return db;
    }

    public Map<String, Object> getTileJSON(String svcURL, String query) {
        String format = tileformat.toString();
        Map<String, Object> tileJSON = new HashMap<String, Object>() {
            {
                put("tilejson", "2.1.0");
                put("scheme", "xyz");
                put("format", format);
                put("tiles", new String[]{String.format("%s/tiles/{z}/{x}/{y}.%s%s", svcURL, format, query == null ? "" : query)});
            }
        };

        try {
            Map<String, Object> metadata = db.readMetadata();
            tileJSON.put("name", name);

            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                switch (entry.getKey()) {
                    // strip out values above
                    case "tilejson":
                    case "map":
                    case "tiles":
                    case "format":
                    case "scheme":
                    case "id":
                        continue;
                        // strip out values that are not supported or are overridden below
                    case "grids":
                    case "interactivity":
                    case "modTime":
                        continue;
                        // strip out values that come from TileMill but aren't useful here
                    case "metatile":
                    case "scale":
                    case "autoscale":
                    case "_updated":
                    case "Layer":
                    case "Stylesheet":
                        continue;
                    default:
                        tileJSON.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return tileJSON;
    }

    public String getName() {
        return name;
    }
}
