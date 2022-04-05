package cn.doubtlhy.mbtileserver.component;

import java.net.URI;

/**
 * @author doubtlhy
 * @date 2022/4/5 14:44
 */
public class ServiceSetConfig {
    Boolean enableServiceList;
    Boolean enableTileJSON;
    Boolean enablePreview;
    URI rootURL;

    public ServiceSetConfig(Boolean enableServiceList, Boolean enableTileJSON, Boolean enablePreview, URI rootURL) {
        this.enableServiceList = enableServiceList;
        this.enableTileJSON = enableTileJSON;
        this.enablePreview = enablePreview;
        this.rootURL = rootURL;
    }

    public ServiceSetConfig() {
    }
}
