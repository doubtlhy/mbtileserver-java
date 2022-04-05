package cn.doubtlhy.mbtileserver.controller;

import cn.doubtlhy.mbtileserver.component.Server;
import cn.doubtlhy.mbtileserver.component.ServiceSet;
import cn.doubtlhy.mbtileserver.component.Tileset;
import cn.doubtlhy.mbtileserver.model.UpdateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/services")
public class MbtilesController {

    private static final Logger log = LoggerFactory.getLogger(MbtilesController.class);
    @Autowired
    Server server;
    ServiceSet svcSet = Server.svcSet;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, path = {"", "/"}, produces = {"application/json;charset=UTF-8"})
    public List<Map<String, String>> serviceList(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.endsWith("/")) {
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }
        String rootURL = String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), requestURI);
        List<Map<String, String>> services = new ArrayList<>();
        for (String id : svcSet.getTilesets().keySet()) {
            Tileset ts = svcSet.getTilesets(id);
            Map<String, String> serviceInfo = new HashMap<>();
            serviceInfo.put("imageType", ts.getTileformatString());
            serviceInfo.put("url", String.format("%s/%s", rootURL, id));
            serviceInfo.put("name", ts.getName());
            services.add(serviceInfo);
        }
        return services;
    }

    @ResponseBody
    @RequestMapping(path = "refresh", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    public UpdateStatus refreshFolder() {
        UpdateStatus status = new UpdateStatus();
        try {
            server.refreshTilesets();
            status.code = 0;
            status.msg = "refresh succeeded";
        } catch (Exception e) {
            status.code = -1;
            status.msg = e.getMessage();
        }
        return status;
    }
}
