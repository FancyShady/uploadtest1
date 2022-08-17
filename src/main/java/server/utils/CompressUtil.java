package server.utils;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Slf4j
public class CompressUtil {

    private static final Map<CompressType, CompressHandler> handlers = new HashMap<>();

    static {
        handlers.put(CompressType.ZIP, new ZipCompressHandler());
    }

    public static void decompress(File srcFile, String destDirPath) {
        String fileName = srcFile.getName();
        CompressHandler compressHandler = getCompressHandler(fileName)
                .orElseThrow(() -> new IllegalStateException(String.format("不支持文件[%s]的解压", fileName)));
        compressHandler.decompress(srcFile, destDirPath);
    }

    public static void compress(CompressType type, List<File> files, Path destDirPath) {
        CompressHandler compressHandler = Optional.ofNullable(handlers.get(type))
                .orElseThrow(() -> new IllegalStateException(String.format("不支持[%s]类型的压缩", type)));
        compressHandler.compress(files, destDirPath);
    }

    private static Optional<CompressHandler> getCompressHandler(String filename) {
        if (StringUtils.hasText(filename)) {
            String fileExtension = Files.getFileExtension(filename);
            CompressType compressType = CompressType.of(fileExtension);
            return Optional.ofNullable(handlers.get(compressType));
        }
        return Optional.empty();
    }

    public enum CompressType {
        ZIP,
        RAR;

        public static CompressType of(String type) {
            CompressType[] values = CompressType.values();
            for (CompressType value : values) {
                if (value.name().equalsIgnoreCase(type)) {
                    return value;
                }
            }
            throw new IllegalStateException(String.format("不支持的类型[%s]", type));
        }
    }

    interface CompressHandler {
        CompressType compressType();

        void decompress(File srcFile, String destDirPath);

        void compress(List<File> files, Path destDirPath);
    }

    static class ZipCompressHandler implements CompressHandler {

        @Override
        public CompressType compressType() {
            return CompressType.ZIP;
        }

        @Override
        public void decompress(File srcFile, String destDirPath) {
            long start = System.currentTimeMillis();
            //判断文件是否存在
            if (!srcFile.exists()) {
                throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
            }
            //开始解压
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(srcFile);
                Enumeration<?> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    log.debug("解压" + entry.getName());
                    //如果是文件夹，就创建个文件夹
                    if (entry.isDirectory()) {
                        String dirPath = destDirPath + "/" + entry.getName();
                        File dir = new File(dirPath);
                        dir.mkdirs();
                    } else {
                        //如果是文件，就先创建一个文件，然后用io流把内容copy过去
                        File targetFile = new File(destDirPath + "/" + entry.getName());
                        //保证这个文件的父文件夹必须要存在
                        if (!targetFile.getParentFile().exists()) {
                            targetFile.getParentFile().mkdirs();
                        }
                        targetFile.createNewFile();
                        //将压缩文件内容写入到这个文件中
                        InputStream is = zipFile.getInputStream(entry);
                        FileOutputStream fos = new FileOutputStream(targetFile);
                        int len;
                        byte[] buf = new byte[2 * 1024];
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                        //关流顺序，先打开的后关闭
                        fos.close();
                        is.close();
                    }
                }
                long end = System.currentTimeMillis();
                log.debug("解压完成，耗时：" + (end - start) + " ms");
            } catch (Exception e) {
                throw new RuntimeException("unzip error from ZipUtils", e);
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void compress(List<File> files, Path destDirPath) {
            long start = System.currentTimeMillis();
            ZipOutputStream zos = null;
            try {
                FileOutputStream out = new FileOutputStream(destDirPath.toFile());
                zos = new ZipOutputStream(out);
                for (File srcFile : files) {
                    byte[] buf = new byte[2 * 1024];
                    zos.putNextEntry(new ZipEntry(srcFile.getName()));
                    int len;
                    FileInputStream in = new FileInputStream(srcFile);
                    while ((len = in.read(buf)) != -1) {
                        zos.write(buf, 0, len);
                    }
                    zos.closeEntry();
                    in.close();
                }
                long end = System.currentTimeMillis();
                log.debug("压缩完成，耗时：" + (end - start) + " ms");
            } catch (Exception e) {
                throw new RuntimeException("zip error from ZipUtils", e);
            } finally {
                if (zos != null) {
                    try {
                        zos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
