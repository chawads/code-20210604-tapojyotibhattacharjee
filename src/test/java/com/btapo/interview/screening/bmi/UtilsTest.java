package com.btapo.interview.screening.bmi;

import com.btapo.interview.screening.bmi.service.ApplicationService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SpringBootTest
@ActiveProfiles("test")
public class UtilsTest {

    @Autowired
    private ApplicationService applicationService;

    @Test
    public void verifyZipSuccessful() throws IOException {
        Path dir = Files.createTempDirectory("test-dir");
        String zipDirName = dir + ".zip";
        Path file1 = Files.createTempFile("file1", ".txt");
        Path file2 = Files.createTempFile("file2", ".txt");
        Path file3 = Files.createTempFile("file3", ".txt");
        Files.write(file1, "file1".getBytes(StandardCharsets.UTF_8));
        Files.write(file2, "file2".getBytes(StandardCharsets.UTF_8));
        Files.write(file3, "file3".getBytes(StandardCharsets.UTF_8));
        Files.move(file1, Paths.get(dir + File.separator + file1.getFileName()),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        Files.move(file2, Paths.get(dir + File.separator + file2.getFileName()),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        Files.move(file3, Paths.get(dir + File.separator + file3.getFileName()),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        applicationService.compressZipFile(dir.toString(), zipDirName);

        FileUtils.deleteDirectory(dir.toFile());

        decompressZip(dir.toString(), zipDirName);

        Files.list(dir).forEach(path -> {
            String filename = path.toFile().getName();
            if (filename.startsWith("file1")) {
                try {
                    assert "file1".equals(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (filename.startsWith("file2")) {
                try {
                    assert "file2".equals(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (filename.startsWith("file3")) {
                try {
                    assert "file3".equals(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        FileUtils.deleteQuietly(new File(zipDirName));
        FileUtils.deleteDirectory(dir.toFile());
    }

    private static void decompressZip(String baseDir, String zipFilePath) throws IOException {
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
}
