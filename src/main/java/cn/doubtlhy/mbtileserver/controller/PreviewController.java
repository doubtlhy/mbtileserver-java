package cn.doubtlhy.mbtileserver.controller;

import cn.doubtlhy.mbtileserver.component.Server;
import cn.doubtlhy.mbtileserver.component.ServiceSet;
import cn.doubtlhy.mbtileserver.component.TileFormat;
import cn.doubtlhy.mbtileserver.component.Tileset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author doubtlhy
 * @date 2022/4/3 15:21
 */
@Controller
public class PreviewController {

    private static final Logger log = LoggerFactory.getLogger(TileController.class);
    @Autowired
    Server server;
    ServiceSet svcSet = Server.svcSet;
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    /***
     * Register controller methods to various URLs.
     */
    @PostConstruct
    public void init() throws NoSuchMethodException {
        Map<String, Tileset> tilesets = svcSet.getTilesets();
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
        Tileset ts = svcSet.getTilesets(id);
        TileFormat format = ts.getTileformat();
        String requestURI = request.getRequestURI();
        if (requestURI.endsWith("/")) {
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }
        String tilesetURL = String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), requestURI);
        tilesetURL = tilesetURL.substring(0, tilesetURL.length() - "/map".length());
        Map<String, Object> tileJSON = ts.getTileJSON(tilesetURL, request.getQueryString());
        map.addAttribute("URL", svcSet.getRootURL() + "/" + id);
        map.addAttribute("ID", id);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonStr = objectMapper.writeValueAsString(tileJSON);
            map.addAttribute("TileJSON", jsonStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            map.addAttribute("TileJSON", "{}");
        }
        if ("pbf".equals(format.toString())) {
            return "map_gl";
        }
        return "map";
    }


    public String IDFromURLPath(String id) {
        String root = svcSet.getRootURL() + "/";
        if (id.startsWith(root)) {
            id = id.replace(root, "");
            if (svcSet.getTilesets(id) != null) {
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
        if (svcSet.getTilesets(id) != null) {
            return id;
        }
        return "";
    }
}
