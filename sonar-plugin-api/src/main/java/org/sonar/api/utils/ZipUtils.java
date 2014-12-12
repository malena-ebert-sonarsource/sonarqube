/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.api.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @since 1.10
 */
public final class ZipUtils {

  private static final String ERROR_CREATING_DIRECTORY = "Error creating directory: ";

  private ZipUtils() {
    // only static methods
  }

  /**
   * Unzip a file into a directory. The directory is created if it does not exist.
   *
   * @return the target directory
   */
  public static File unzip(File zip, File toDir) throws IOException {
    unzip(zip, toDir, new ZipEntryFilter() {
      @Override
      public boolean accept(ZipEntry entry) {
        return true;
      }
    });
    return toDir;
  }

  public static File unzip(InputStream zip, File toDir) throws IOException {
    unzip(zip, toDir, new ZipEntryFilter() {
      @Override
      public boolean accept(ZipEntry entry) {
        return true;
      }
    });
    return toDir;
  }

  public static File unzip(InputStream stream, File toDir, ZipEntryFilter filter) throws IOException {
    if (!toDir.exists()) {
      FileUtils.forceMkdir(toDir);
    }

    ZipInputStream zipStream = new ZipInputStream(stream);
    try {
      ZipEntry entry;
      while ((entry = zipStream.getNextEntry()) != null) {
        if (filter.accept(entry)) {
          File to = new File(toDir, entry.getName());
          if (entry.isDirectory()) {
            throwExceptionIfDirectoryIsNotCreatable(to);
          } else {
            File parent = to.getParentFile();
            throwExceptionIfDirectoryIsNotCreatable(parent);
            copy(zipStream, to);
          }
        }
      }
      return toDir;

    } finally {
      zipStream.close();
    }
  }

  private static void throwExceptionIfDirectoryIsNotCreatable(File to) throws IOException {
    if (to != null && !to.exists() && !to.mkdirs()) {
      throw new IOException(ERROR_CREATING_DIRECTORY + to);
    }
  }

  public static File unzip(File zip, File toDir, ZipEntryFilter filter) throws IOException {
    if (!toDir.exists()) {
      FileUtils.forceMkdir(toDir);
    }

    ZipFile zipFile = new ZipFile(zip);
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (filter.accept(entry)) {
          File to = new File(toDir, entry.getName());
          if (entry.isDirectory()) {
            throwExceptionIfDirectoryIsNotCreatable(to);
          } else {
            File parent = to.getParentFile();
            throwExceptionIfDirectoryIsNotCreatable(parent);
            copy(zipFile, entry, to);
          }
        }
      }
      return toDir;

    } finally {
      zipFile.close();
    }
  }

  private static void copy(ZipInputStream zipStream, File to) throws IOException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(to);
      IOUtils.copy(zipStream, fos);
    } finally {
      IOUtils.closeQuietly(fos);
    }
  }

  private static void copy(ZipFile zipFile, ZipEntry entry, File to) throws IOException {
    FileOutputStream fos = new FileOutputStream(to);
    InputStream input = null;
    try {
      input = zipFile.getInputStream(entry);
      IOUtils.copy(input, fos);
    } finally {
      IOUtils.closeQuietly(input);
      IOUtils.closeQuietly(fos);
    }
  }

  public static void zipDir(File dir, File zip) throws IOException {
    OutputStream out = null;
    ZipOutputStream zout = null;
    try {
      out = FileUtils.openOutputStream(zip);
      zout = new ZipOutputStream(out);
      zip(dir, zout);

    } finally {
      IOUtils.closeQuietly(zout);
      IOUtils.closeQuietly(out);
    }
  }

  private static void doZip(String entryName, InputStream in, ZipOutputStream out) throws IOException {
    ZipEntry entry = new ZipEntry(entryName);
    out.putNextEntry(entry);
    IOUtils.copy(in, out);
    out.closeEntry();
  }

  private static void doZip(String entryName, File file, ZipOutputStream out) throws IOException {
    if (file.isDirectory()) {
      entryName += '/';
      ZipEntry entry = new ZipEntry(entryName);
      out.putNextEntry(entry);
      out.closeEntry();
      File[] files = file.listFiles();
      for (int i = 0, len = files.length; i < len; i++) {
        doZip(entryName + files[i].getName(), files[i], out);
      }

    } else {
      InputStream in = null;
      try {
        in = new BufferedInputStream(new FileInputStream(file));
        doZip(entryName, in, out);
      } finally {
        IOUtils.closeQuietly(in);
      }
    }
  }

  private static void zip(File file, ZipOutputStream out) throws IOException {
    for (File child : file.listFiles()) {
      String name = child.getName();
      doZip(name, child, out);
    }
  }

  public interface ZipEntryFilter {
    boolean accept(ZipEntry entry);
  }

}
