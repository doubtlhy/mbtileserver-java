package cn.doubtlhy.mbtileserver.controller;

import cn.doubtlhy.mbtileserver.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * @author doubtlhy
 * @date 2022/3/26 22:04
 */
@RestController
public class TileController {

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
                    RequestMappingInfo.paths("/services/" + id).methods(RequestMethod.GET)
                            .produces(MediaType.APPLICATION_JSON_VALUE).build(),
                    this,
                    TileController.class.getDeclaredMethod("getTileJSON", HttpServletRequest.class));

            handlerMapping.registerMapping(
                    RequestMappingInfo.paths("/services/" + id + "/tiles/**").methods(RequestMethod.GET)
                            .build(),
                    this,
                    TileController.class.getDeclaredMethod("getTile", HttpServletRequest.class));
        }
    }

    public Map<String, Object> getTileJSON(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.endsWith("/")) {
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }
        String tilesetURL = String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), requestURI);
        String id = IDFromURLPath(requestURI);
        Tileset ts = svcSet.getTilesets(id);
        Map<String, Object> tileJSON = ts.getTileJSON(tilesetURL, request.getQueryString());
        if (svcSet.enablePreview) {
            tileJSON.put("map", String.format("%s/map", tilesetURL));
        }
        return tileJSON;
    }

    public ResponseEntity getTile(HttpServletRequest request) {
        int z = 1;
        int x = 1;
        int y = 1;
        try {
            String requestURI = request.getRequestURI();
            // split path components to extract tile coordinates x, y and z
            String[] pcs = requestURI.substring(1).split("/");
            // we are expecting at least "services", <id> , "tiles", <z>, <x>, <y plus .ext>
            int len = pcs.length;
            if (len < 6 || pcs[5].equals("")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("requested path is too short");
            }
            z = Integer.parseInt(pcs[len - 3]);
            x = Integer.parseInt(pcs[len - 2]);
            String yExt = pcs[len - 1];
            y = Integer.parseInt(yExt.substring(0, yExt.lastIndexOf(".")));
            // flip y to match the spec
            y = (1 << z) - 1 - y;
            String ext = yExt.substring(yExt.lastIndexOf("."));

            String id = requestURI.substring(0, requestURI.lastIndexOf("/tiles/"));
            id = id.substring("/services/".length());

            Tileset ts = svcSet.getTilesets(id);
            TileFormat tileFormat = ts.getTileformat();
            Tile tile = ts.getDb().readTile(z, x, y);
            if (tile.getData() != null) {
                int size = tile.getData().available();
                byte[] bytes = new byte[size];
                tile.getData().read(bytes);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", tileFormat.getContentType());
                if (tileFormat.toString().contains("pbf")) {
                    headers.add("Content-Encoding", "gzip");
                }
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(bytes);
            }
            return tileNotFoundHandler(tileFormat);
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(String.format("The tile with the parameters %s, %s, %s parse error", "x", "y", "z"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("The tile with the parameters x=%d, y=%d & zoom=%d not found", x, y, z));
        }
    }

    public ResponseEntity tileNotFoundHandler(TileFormat format) {
        HttpHeaders headers = new HttpHeaders();
        switch (format) {
            case PNG:
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(blankPNG());
            case JPG:
            case WEBP:
                headers.add("Content-Type", "image/png");
                File watermark = new File("watermark.png");
                if (watermark.exists()) {
                    try {
                        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(Files.readAllBytes(watermark.toPath()));
                    } catch (IOException e) {
                        log.error("read watermark file failed: {}", e.getMessage());
                    }
                }
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(blankPNG());
            case PBF:
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
            default:
                headers.add("Content-Type", "application/json");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body("{\"message\": \"Tile does not exist\"}");
        }
    }

    public byte[] blankPNG() {
        int[] a = {
                0x89, 0x50, 0x4e, 0x47, 0xd, 0xa, 0x1a, 0xa, 0x0, 0x0, 0x0, 0xd, 0x49,
                0x48, 0x44, 0x52, 0x0, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1, 0x0, 0x1, 0x3,
                0x0, 0x0, 0x0, 0x66, 0xbc, 0x3a, 0x25, 0x0, 0x0, 0x0, 0x3, 0x50, 0x4c,
                0x54, 0x45, 0x0, 0x0, 0x0, 0xa7, 0x7a, 0x3d, 0xda, 0x0, 0x0, 0x0, 0x1,
                0x74, 0x52, 0x4e, 0x53, 0x0, 0x40, 0xe6, 0xd8, 0x66, 0x0, 0x0, 0x0,
                0x1f, 0x49, 0x44, 0x41, 0x54, 0x68, 0xde, 0xed, 0xc1, 0x1, 0xd, 0x0,
                0x0, 0x0, 0xc2, 0x20, 0xfb, 0xa7, 0x36, 0xc7, 0x37, 0x60, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x71, 0x7, 0x21, 0x0, 0x0, 0x1, 0xa7,
                0x57, 0x29, 0xd7, 0x0, 0x0, 0x0, 0x0, 0x49, 0x45, 0x4e, 0x44, 0xae,
                0x42, 0x60, 0x82,
        };
        byte[] bytes = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            bytes[i] = (byte) a[i];
        }
        return bytes;
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