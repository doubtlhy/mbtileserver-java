> Note: This project started as a partial implementation of the API and features provided by [mbtileserver](https://github.com/consbio/mbtileserver) written in Go by [Brendan Ward](https://github.com/brendan-ward). It might diverge from that project in the future.

# mbtileserver-java

A simple Java-based server for map tiles stored in [mbtiles](https://github.com/mapbox/mbtiles-spec) format.

## Summary

It currently provides support for `png`, `jpg`, and `pbf` (vector tile)
tilesets according to version 1.0 of the mbtiles specification. Tiles
are served following the XYZ tile scheme, based on the Web Mercator
coordinate reference system.

## Features
- Implementation by Spring Boot
- Serve tiles from mbtiles files
- Support image tile: png, jpg
- Support vector tile: pbf

## Usage

Run `java -jar mbtileserver.jar --help` for a list and description of the available flags:
```
Serve tiles from mbtiles files.

Usage:
  java -jar  [flags] mbtileserver.jar

Flags:
  -Ddir=string             Directory containing mbtiles files. Directory containing mbtiles files.  Can be a comma-delimited list of directories. (default "./tilesets")
  -Dserver.port=int        Server port. (default 8000)
```

## Endpoints

| Endpoint                                                     | Description                                                                    |
|--------------------------------------------------------------|--------------------------------------------------------------------------------|
| /services                                                    | lists all discovered and valid mbtiles in the tiles directory                  |
| /services/\<path-to-tileset>                                 | shows tileset metadata                                                         |                                                            |
| /services/\<path-to-tileset>/tiles/{z}/{x}/{y}.\<tile-format> | returns tileset tile at the given x, y, and z                                  |
| /refresh                                                     | refresh mbtiles files                                  |

## References

- [mbtiles-image-server](https://github.com/wclwksn/mbtiles-image-server)

- [mbtileserver](https://github.com/consbio/mbtileserver)

- [mbtiles4j](https://github.com/jtreml/mbtiles4j)

- [mbtiles-server](https://github.com/agorshkov23/mbtiles-server)
