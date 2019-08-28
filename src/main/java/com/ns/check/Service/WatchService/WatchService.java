package com.ns.check.Service.WatchService;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

@Service
public class WatchService {

    public static int count=0;

    public  int getCount() {
        return count;
    }

    public  void setCount(int count) {
        WatchService.count = count;
    }

    public void watchingMethod() throws IOException {
        java.nio.file.WatchService watchService = FileSystems.getDefault().newWatchService();

        Path directory = Paths.get("E:\\Reload Check\\src\\main\\resources\\Rules\\");

        WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);


        while (true) {
            for (WatchEvent<?> event : watchKey.pollEvents()) {

                System.out.println(event.kind());
                count++;

            }


        }


    }
}
