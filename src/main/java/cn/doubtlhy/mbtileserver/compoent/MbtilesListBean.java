package cn.doubtlhy.mbtileserver.compoent;

import cn.doubtlhy.mbtileserver.handlers.MBTilesReader;
import cn.doubtlhy.mbtileserver.handlers.MetadataEntry;
import cn.doubtlhy.mbtileserver.model.MbtilesInfo;
import cn.doubtlhy.mbtileserver.model.MbtilesObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class MbtilesListBean implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(MbtilesListBean.class);
    @Value("${dir}")
    private String mbPath;
    private String rootURL = "/services";
    private File watermark;
    private List<MbtilesInfo> mbtilesInfoList = new ArrayList<>();
    private HashMap<String, MbtilesObject> tilesets = new HashMap<>();

    public File getWatermark() {
        return watermark;
    }

    public void setWatermark(File watermark) {
        this.watermark = watermark;
    }

    public String getRootURL() {
        return rootURL;
    }

    public void setRootURL(String rootURL) {
        this.rootURL = rootURL;
    }

    public HashMap<String, MbtilesObject> getTilesets() {
        return tilesets;
    }

    public List<MbtilesInfo> getAllmbfiles() {
        for (MbtilesInfo mb : mbtilesInfoList) {
            mb.url = String.format("%s/%s", rootURL, mb.id);
        }
        return mbtilesInfoList;
    }

    public HashMap<String, MbtilesObject> getMBReaderHM() {
        return tilesets;
    }

    public MbtilesObject getMBReader(String _keyvalue) {
        if (tilesets.containsKey(_keyvalue)) {
            return tilesets.get(_keyvalue);
        }
        return null;
    }

    public MetadataEntry getMetadata(MBTilesReader mbReader) throws Exception {
        return mbReader.getMetadata();
    }

    public void setmbtilesValue() throws Exception {
        tilesets.clear();
        this.mbtilesInfoList.clear();
        this.readAllFile(mbPath, mbPath);
        log.info("read mbtiles file finish!");
    }

    private void readAllFile(String filePath, String basePath) throws Exception {
        if (filePath.contains(",")) {
            for (String path : filePath.split(",")) {
                readAllFile(path, path);
            }
        } else {
            File f = new File(filePath);
            File[] files = f.listFiles();
            if (files == null) {
                throw new FileNotFoundException(String.format("file not found: %s", filePath));
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    readAllFile(file.getAbsolutePath(), basePath);
                } else {
                    String fileName = file.getName();
                    int index = fileName.lastIndexOf(".");
                    if (index < 0) {
                        continue;
                    }
                    String suffix = fileName.substring(index);
                    String prefix = fileName.substring(0, fileName.lastIndexOf("."));
                    if (".mbtiles".equals(suffix)) {
                        MBTilesReader mbReader = new MBTilesReader(file);
                        MetadataEntry metadata = mbReader.getMetadata();
                        if (metadata == null) {
                            continue;
                        }
                        MbtilesInfo mbInfo = new MbtilesInfo();
                        String name = metadata.getTilesetName();
                        if (StringUtils.isEmpty(name)) {
                            name = prefix;
                        }
                        mbInfo.name = name;
                        mbInfo.id = relativePathID(file.getAbsolutePath(), basePath);
                        mbInfo.fullPath = file.getAbsolutePath();
                        mbInfo.mbFile = file;

                        if (!tilesets.containsKey(mbInfo.id)) {
                            String imageFormat = metadata.getTileMimeType().toString();
                            if ("".equals(imageFormat)) {
                                imageFormat = "png";
                            }
                            mbInfo.imageType = imageFormat;
                            tilesets.put(mbInfo.id, new MbtilesObject(mbReader, imageFormat));
                        }
                        mbtilesInfoList.add(mbInfo);
                    } else if ("watermark.png".equals(fileName)) {
                        watermark = file;
                    }
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Searching for tilesets in {}", mbPath);
        this.readAllFile(mbPath, mbPath);
        log.info("Published {} services", tilesets.size());
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

}
