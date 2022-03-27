package cn.doubtlhy.mbtileserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MbtileServerApplication {
    public static void main(String[] args) {
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        if (applicationArguments.containsOption("help")) {
            printUsage();
            System.exit(0);
        }
        SpringApplication.run(MbtileServerApplication.class, args);
    }

    public static void printUsage() {
        String sb = "Serve tiles from mbtiles files.\n\n" +
                "Usage:\n\tjava [flags] -jar mbtileserver.jar\n" +
                "Flags:\n" +
                "\t-Ddir=string             Directory containing mbtiles files. Directory containing mbtiles files.  Can be a comma-delimited list of directories. (default \"./tilesets\")\n" +
                "\t-Dserver.port=int        Server port. (default 8000)\n";
        System.out.println(sb);
    }
}
