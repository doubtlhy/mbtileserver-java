package cn.doubtlhy.mbtileserver.controller;

import cn.doubtlhy.mbtileserver.compoent.MbtilesListBean;
import cn.doubtlhy.mbtileserver.handlers.MBTilesReader;
import cn.doubtlhy.mbtileserver.handlers.MetadataEntry;
import cn.doubtlhy.mbtileserver.model.MbtilesObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author doubtlhy
 * @date 2022/4/3 15:21
 */
@Controller
public class PreviewController {

    private static final Logger log = LoggerFactory.getLogger(TileController.class);
    @Autowired
    MbtilesListBean mbtilesListBean;
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    /***
     * Register controller methods to various URLs.
     */
    @PostConstruct
    public void init() throws NoSuchMethodException {
        HashMap<String, MbtilesObject> tilesets = mbtilesListBean.getTilesets();
        for (String id : tilesets.keySet()) {
            handlerMapping.registerMapping(
                    RequestMappingInfo.paths("/services/" + id + "/map").methods(RequestMethod.GET)
                            .build(),
                    this,
                    PreviewController.class.getDeclaredMethod("previewHandler", Model.class, HttpServletRequest.class));
        }
    }
    public String previewHandler(Model map, HttpServletRequest request) {
        String id = IDFromURLPath(request.getRequestURI());
        MbtilesObject mbtilesObject = mbtilesListBean.getMBReader(id);
        MBTilesReader mbTilesReader = mbtilesObject.getMBTileReader();
        String format = mbtilesObject.getImageFormat();
        String host = String.format("%s://%s:%s", request.getScheme(), request.getServerName(), request.getServerPort());
        String tilesetURL = String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getRequestURI().substring(0,"/map".length()));
        Map<String, Object> tileJSON = new HashMap<String, Object>() {
            {
                put("tilejson", "2.1.0");
                put("scheme", "xyz");
                put("format", format);
                put("tiles", new String[]{String.format("%s%s/tiles/{z}/{x}/{y}.%s", host, request.getRequestURI().substring(0, request.getRequestURI().length()-"/map".length()), format)});
                put("map", String.format("%s%s", host, request.getRequestURI()));
            }
        };
        String fileName = mbTilesReader.getFile().getName();
        String prefix = fileName.substring(0, fileName.lastIndexOf("."));

        try {
            MetadataEntry metadata = mbTilesReader.getMetadata();
            String name = metadata.getTilesetName();
            if (StringUtils.isEmpty(name)) {
                name = prefix;
            }
            tileJSON.put("name", name);

            for (Map.Entry<String, Object> entry : mbTilesReader.readMetadata().entrySet()) {
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
        map.addAttribute("URL", mbtilesListBean.getRootURL()+"/"+id);
        map.addAttribute("ID", id);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonStr = objectMapper.writeValueAsString(tileJSON);
            map.addAttribute("TileJSON", jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            map.addAttribute("TileJSON", "{}");
        }
        if ("pbf".equals(format)) {
            return "map_gl";
        }
        return "map";
    }


    public String IDFromURLPath(String id) {
        String root = mbtilesListBean.getRootURL() + "/";
        if (id.startsWith(root)) {
            id = id.replace(root, "");
            if (mbtilesListBean.getMBReaderHM().get(id) != null) {
                return id;
            }
            int i = id.lastIndexOf("/tiles/");
            if (i != -1) {
                id = id.substring(0, i);
            } else {
                i = id.lastIndexOf("/map");
                if (i != -1) {
                    id = id.substring(0, i);
                }
            }
        } else {
            return "";
        }
        if (mbtilesListBean.getMBReaderHM().get(id) != null) {
            return id;
        }
        return "";
    }
}
