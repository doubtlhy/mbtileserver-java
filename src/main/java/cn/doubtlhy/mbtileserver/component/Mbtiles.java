package cn.doubtlhy.mbtileserver.component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mbtiles {
    String filename;
    Connection db;
    String id;
    TileFormat tileformat;

    public Mbtiles(Connection db, String filename, TileFormat tileformat) {
        this.db = db;
        this.filename = filename;
        this.tileformat = tileformat;
    }

    public static Mbtiles newDB(String filename) throws Exception {
        Connection db;
        try {
            File file = new File(filename);
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (ClassNotFoundException | SQLException e) {
            throw new Exception("Establish Connection failed.", e);
        }

        int tableCount;

        Statement stmt = db.createStatement();
        ResultSet resultSet = stmt.executeQuery("SELECT count(*) FROM sqlite_master WHERE name in ('tiles', 'metadata')");
        while (resultSet.next()) {
            tableCount = resultSet.getInt(1);
            if (tableCount < 2) {
                throw new Exception("Missing required table: 'tiles' or 'metadata'");
            }
        }

        byte[] data;
        TileFormat tileformat = TileFormat.UNKNOWN;
        resultSet = stmt.executeQuery("select tile_data from tiles limit 1");
        while (resultSet.next()) {
            data = resultSet.getBytes(1);
            tileformat = detectTileFormat(data);
            if (tileformat == TileFormat.GZIP) {
                // GZIP masks PBF, which is only expected type for tiles in GZIP format
                tileformat = TileFormat.PBF;
            }
        }

        return new Mbtiles(db, filename, tileformat);
    }

    public static List<String> findMBtiles(String baseDir) throws FileNotFoundException {
        List<String> filenames = new ArrayList<>();
        File f = new File(baseDir);
        File[] files = f.listFiles();
        if (files == null) {
            throw new FileNotFoundException(String.format("file not found: %s", baseDir));
        }
        for (File file : files) {
            if (file.isDirectory()) {
                filenames.addAll(findMBtiles(file.getAbsolutePath()));
            } else {
                String fileName = file.getName();
                int index = fileName.lastIndexOf(".");
                if (index < 0) {
                    continue;
                }
                String suffix = fileName.substring(index);
                if (".mbtiles".equals(suffix)) {
                    filenames.add(file.getAbsolutePath());
                }
            }
        }
        return filenames;
    }

    public static TileFormat detectTileFormat(byte[] data) {
        Map<TileFormat, byte[]> patterns = new HashMap<>();
        patterns.put(TileFormat.GZIP, new byte[]{(byte) 0x1f, (byte) 0x8b});
        patterns.put(TileFormat.ZLIB, new byte[]{(byte) 0x78, (byte) 0x9c});
        patterns.put(TileFormat.PNG, new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A});
        patterns.put(TileFormat.JPG, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        patterns.put(TileFormat.WEBP, new byte[]{(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46});

        for (Map.Entry<TileFormat, byte[]> entry : patterns.entrySet()) {
            if (startsWith(data, entry.getValue())) {
                return entry.getKey();
            }
        }
        return TileFormat.UNKNOWN;
    }

    public static boolean startsWith(byte[] array, byte[] prefix) {
        if (array == prefix) {
            return true;
        }
        if (array == null || prefix == null) {
            return false;
        }
        int prefixLength = prefix.length;

        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefixLength; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }

        return true;
    }

    public Map<String, Object> readMetadata() throws Exception {
        String sql = "SELECT * from metadata;";
        try {
            Statement stmt = db.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            Map<String, Object> ent = new HashMap<>();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String value = resultSet.getString("value");
                switch (name) {
                    case "maxzoom":
                    case "minzoom":
                        ent.put(name, Integer.parseInt(value));
                        break;
                    case "bounds":
                    case "center":
                        String[] split = value.split(",");
                        List<Double> out = new ArrayList<>();
                        for (String v : split) {
                            Double i = Double.parseDouble(v);
                            out.add(i);
                        }
                        ent.put(name, out);
                        break;
                    case "json":
                        ObjectMapper objectMapper = new ObjectMapper();
                        Map<String, Object> jsonMap = objectMapper.readValue(value, Map.class);
                        ent.putAll(jsonMap);
                        break;
                    default:
                        ent.put(name, value);
                }
            }
            return ent;
        } catch (Exception e) {
            throw new Exception("Get Metadata failed", e);
        }
    }

    public Tile readTile(int zoom, int column, int row) throws Exception {
        String sql = String.format(
                "SELECT tile_data FROM tiles WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d", zoom,
                column, row);

        try {
            Statement stmt = db.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next()) {
                InputStream tileDataInputStream;
                tileDataInputStream = resultSet.getBinaryStream("tile_data");
                return new Tile(zoom, column, row, tileDataInputStream);
            } else {
                return new Tile(zoom, column, row, null);
            }
        } catch (Exception e) {
            throw new Exception(String.format("Could not get Tile for z:%d, column:%d, row:%d", zoom, column, row), e);
        }
    }

    public TileFormat getTileformat() {
        return this.tileformat;
    }
}
