package com.btapo.interview.screening.bmi.utils;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class CompressionUtility {

    public static void compressZipFile(String sourceDir, String outputFile) throws IOException {
        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputFile));
        compressDirectoryToZipFile(sourceDir, sourceDir, zipFile);
        IOUtils.closeQuietly(zipFile);
    }

    public static void decompressZip(String baseDir, String zipFilePath) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();
                String name = zipEntry.getName();
                File file = new File(name);
                if (name.endsWith("/")) {
                    file.mkdirs();
                    continue;
                }
                File parent = file.getParentFile() == null
                        ? new File(baseDir) :
                        new File(baseDir + File.separator + file.getParent());
                parent.mkdirs();
                is = zipFile.getInputStream(zipEntry);
                fos = new FileOutputStream(parent + File.separator + file.getName());
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (zipFile != null) {
                zipFile.close();
            }
        }
    }

    private static void compressDirectoryToZipFile(String rootDir, String sourceDir, ZipOutputStream out) throws IOException {
        for (File file : Objects.requireNonNull(new File(sourceDir).listFiles())) {
            if (file.isDirectory()) {
                compressDirectoryToZipFile(rootDir, sourceDir + File.separator + file.getName(), out);
            } else {
                ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "") + file.getName());
                out.putNextEntry(entry);

                FileInputStream in = new FileInputStream(sourceDir + File.separator + file.getName());
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
            }
        }
    }
}
