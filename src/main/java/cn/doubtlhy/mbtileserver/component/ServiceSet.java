package cn.doubtlhy.mbtileserver.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doubtlhy
 * @date 2022/4/5 14:39
 */
@Component
public class ServiceSet {
    private static final Logger log = LoggerFactory.getLogger(ServiceSet.class);
    public Boolean enableServiceList;
    public Boolean enableTileJSON;
    public Boolean enablePreview;
    private Map<String, Tileset> tilesets;
    private String domain;
    private URI rootURL;

    public ServiceSet() {

    }

    public ServiceSet(Map<String, Tileset> tilesets, Boolean enableServiceList, Boolean enableTileJSON, Boolean enablePreview, URI rootURL) {
        this.tilesets = tilesets;
        this.enableServiceList = enableServiceList;
        this.enableTileJSON = enableTileJSON;
        this.enablePreview = enablePreview;
        this.rootURL = rootURL;
    }

    public static ServiceSet newServiceSet(ServiceSetConfig cfg) {
        if (cfg == null) {
            cfg = new ServiceSetConfig();
        }

        return new ServiceSet(new HashMap<>(),
                cfg.enableServiceList,
                cfg.enableTileJSON,
                cfg.enablePreview,
                cfg.rootURL);
    }

    public Map<String, Tileset> getTilesets() {
        return tilesets;
    }

    public Tileset getTilesets(String id) {
        return tilesets.get(id);
    }

    public void addTileset(String filename, String id) throws Exception {
        if (tilesets.containsKey(id)) {
            log.error("Tileset already exists for ID: {}", id);
            return;
        }

        String path = this.rootURL.getPath() + "/" + id;
        Tileset ts = Tileset.newTileset(this, filename, id, path);
        this.tilesets.put(id, ts);
    }

    public URI getRootURL() {
        return rootURL;
    }
}
