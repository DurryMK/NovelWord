package other;

import app.conf.Conf;
import app.service.PythonExecute;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Process {
    public boolean start(String url, String fileName) {
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
                System.out.println("[开始加载文本]:");
                PythonExecute.service(url, relFilePath);
                System.out.println("[加载文本耗时]:" + (System.currentTimeMillis() - start));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        String url = "http://www.quanshuwang.com/book/9/9055";
        String filename = url.substring(url.lastIndexOf("/") + 1);
        new Process().start(url, filename);
    }
}
