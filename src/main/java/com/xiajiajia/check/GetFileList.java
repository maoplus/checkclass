package com.xiajiajia.check;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class GetFileList {

  public static List<Path> getAllFile(String sourcePath) throws IOException {

    final List<Path> fileLists = Lists.newArrayList();

    Files.walkFileTree(Paths.get(sourcePath),
        new SimpleFileVisitor<Path>() {
          public FileVisitResult visitFile(Path file,
              BasicFileAttributes attrs) throws IOException {
                  fileLists.add(file);
            return FileVisitResult.CONTINUE;
          }

          public FileVisitResult postVisitDirectory(Path dir,
              IOException e) throws IOException {
            return FileVisitResult.CONTINUE;
          }
        });
    return fileLists;
  }
}
