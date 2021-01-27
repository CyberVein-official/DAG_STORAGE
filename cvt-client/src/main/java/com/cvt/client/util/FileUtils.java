package com.cvt.client.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    /**
     * 文件分割
     *
     * @param src      源文件路径
     * @param dist     目标文件路径
     * @param fileSize 分割后每个文件的大小，单位是MB
     */
    public static void split(String src, String dist, int fileSize) throws Exception {

        if ("".equals(src) || src == null || fileSize == 0 || "".equals(dist) || dist == null) {
            throw new IllegalArgumentException("参数不合法");
        }

        File srcFile = new File(src);//源文件
        File distDir = new File(dist);
        if (!distDir.exists()) {
            distDir.mkdirs();
        }

        long srcSize = srcFile.length();//源文件的大小

        int number = (int) (srcSize / fileSize);
        number = srcSize % fileSize == 0 ? number : number + 1;//分割后文件的数目

        String fileName = src.substring(src.lastIndexOf(File.separator));//源文件名

        InputStream in = null;//输入字节流
        BufferedInputStream bis = null;//输入缓冲流
        byte[] bytes = new byte[1024];//每次读取文件的大小为1K
        int len = -1;//每次读取的长度值
        try {
            in = new FileInputStream(srcFile);
            bis = new BufferedInputStream(in);
            for (int i = 0; i < number; i++) {

                String destName = dist + File.separator + fileName + "-" + i + ".dat";
                OutputStream out = new FileOutputStream(destName);
                BufferedOutputStream bos = new BufferedOutputStream(out);
                int count = 0;
                while ((len = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);//把字节数据写入目标文件中
                    count += len;
                    if (count >= fileSize) {
                        break;
                    }
                }
                bos.flush();//刷新
                bos.close();
                out.close();
            }
        } finally {
            //关闭流
            try {
                if (bis != null) bis.close();
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            srcFile.deleteOnExit();
        }
    }

    public static List<FileHashPair> signFiles(String outDir) throws Exception {
        File dir = new File(outDir);
        if (!dir.isDirectory()) {
            return new ArrayList<>();
        }
        File[] fileList = dir.listFiles();
        if (null == fileList) {
            return new ArrayList<>();
        }
        List<FileHashPair> fileHashPairList = new ArrayList<>();
        for (File file : fileList) {
            try (FileInputStream is = new FileInputStream(file)) {
                fileHashPairList.add(new FileHashPair(file.getAbsolutePath(), DigestUtils.md5Hex(IOUtils.toString(is, StandardCharsets.UTF_8))));
            }
        }
        return fileHashPairList;
    }
    public static class FileHashPair {
        String filePath;
        String hash;

        public FileHashPair() {
        }

        public FileHashPair(String filePath, String hash) {
            this.filePath = filePath;
            this.hash = hash;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }
    }
}
