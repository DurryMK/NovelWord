package app.tools;

import app.conf.Conf;
import app.service.PythonExecute;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
public class test {
    public static void main(String[] args) {
        String url = "http://www.quanshuwang.com/book/9/9055";
        String filename = url.substring(url.lastIndexOf("/") + 1);
        System.out.println("[生成的文件路径]:" + filename);
        writeDataHubData(url.substring(url.lastIndexOf("/") + 1), url);
    }

    public static boolean writeDataHubData(String fileName, String url) {
        long start = System.currentTimeMillis();
        StringBuffer content = new StringBuffer();
        BufferedWriter out = null;
        try {
            if (StringUtils.isNotEmpty(fileName)) {
                //保存的文件名
                fileName = fileName + ".txt";
                //保存的路径
                String relFilePath = Conf.txtPath() + fileName;
                System.out.println("[生成的文件路径]:" + relFilePath);
                File file = new File(relFilePath);
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    file.delete();
                    file.createNewFile();
                }
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                out.write(" ");
                out.close();
                PythonExecute.service(url, relFilePath);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            //关闭流
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
