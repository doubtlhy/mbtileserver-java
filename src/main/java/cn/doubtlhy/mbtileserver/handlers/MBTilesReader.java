package cn.doubtlhy.mbtileserver.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteException;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MBTilesReader {
    private static final Logger log = LoggerFactory.getLogger(MBTilesReader.class);
    private File f;
    private Connection connection;

    public MBTilesReader(File f) throws Exception {
        try {
            connection = SQLiteHelper.establishConnection(f);
        } catch (Exception e) {
            throw new Exception("Establish Connection to " + f.getAbsolutePath() + " failed", e);
        }
        this.f = f;
    }

    public File getFile() {
        return f;
    }

    public File close() {
        try {
            connection.close();
        } catch (SQLException e) {
        }
        return f;
    }

    public MetadataEntry getMetadata() throws Exception {
        String sql = "SELECT * from metadata;";
        try {
            ResultSet resultSet = SQLiteHelper.executeQuery(connection, sql);
            MetadataEntry ent = new MetadataEntry();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String value = resultSet.getString("value");
                ent.addKeyValue(name, value);
            }
            return ent;
        } catch (SQLiteException e) {
            log.error("Could not add tileset for {}: Missing required table: 'tiles' or 'metadata'", f.getName());
            return null;
        } catch (Exception e) {
            throw new Exception("Get Metadata failed", e);
        }
    }

    public Map<String, Object> readMetadata() throws Exception {
        String sql = "SELECT * from metadata;";
        try {
            ResultSet resultSet = SQLiteHelper.executeQuery(connection, sql);
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

    public Tile getTile(int zoom, int column, int row) throws Exception {
        String sql = String.format(
                "SELECT tile_data FROM tiles WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d", zoom,
                column, row);

        try {
            ResultSet resultSet = SQLiteHelper.executeQuery(connection, sql);
            if (resultSet.next()) {
                InputStream tileDataInputStream = null;
                tileDataInputStream = resultSet.getBinaryStream("tile_data");

                return new Tile(zoom, column, row, tileDataInputStream);
            } else {
                return new Tile(zoom, column, row, null);
            }
        } catch (Exception e) {
            throw new Exception(String.format("Could not get Tile for z:%d, column:%d, row:%d", zoom, column, row), e);
        }
    }

    public int getMaxZoom() throws Exception {
        String sql = "SELECT MAX(zoom_level) FROM tiles";

        try {
            ResultSet resultSet = SQLiteHelper.executeQuery(connection, sql);
            return resultSet.getInt(1);
        } catch (Exception e) {
            throw new Exception("Could not get max zoom", e);
        }
    }

    public int getMinZoom() throws Exception {
        String sql = "SELECT MIN(zoom_level) FROM tiles";

        try {
            ResultSet resultSet = SQLiteHelper.executeQuery(connection, sql);
            return resultSet.getInt(1);
        } catch (Exception e) {
            throw new Exception("Could not get min zoom", e);
        }
    }
}
