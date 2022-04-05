package cn.doubtlhy.mbtileserver.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author doubtlhy
 * @date 2022/4/5 20:22
 */

@Component
public class Server implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    @Autowired
    public static ServiceSet svcSet;

    @Value("${dir}")
    private String tilePath;

    public void serve() throws URISyntaxException, FileNotFoundException {
        String rootURLStr = "/services";
        URI rootURL;

        rootURL = new URI(rootURLStr);
        ServiceSetConfig cfg = new ServiceSetConfig(true, true, true, rootURL);
        svcSet = ServiceSet.newServiceSet(cfg);

        for (String path : tilePath.split(",")) {
            // Discover all tilesets
            log.info("Searching for tilesets in {}", path);
            List<String> filenames = Mbtiles.findMBtiles(path);
            if (filenames.isEmpty()) {
                log.error("No tilesets found in {}", path);
            }

            // Register all tilesets
            for (String filename : filenames) {
                String id = relativePathID(filename, path);
                try {
                    svcSet.addTileset(filename, id);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public String relativePathID(String filename, String baseDir) {
        String basePath = new File(baseDir).getAbsolutePath();
        String fullPath = new File(filename).getAbsolutePath();
        String subpath = fullPath.replace(basePath, "");
        if (!"/".equals(File.separator)) {
            subpath = subpath.replaceAll(File.separator + File.separator, "/");
        }
        return subpath.substring(1, subpath.lastIndexOf("."));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.serve();
        log.info("Published {} services", svcSet.getTilesets().size());
    }

    public void refreshTilesets() throws Exception {
        svcSet.getTilesets().clear();
        this.serve();
        log.info("read mbtiles file finish!");
    }
}
