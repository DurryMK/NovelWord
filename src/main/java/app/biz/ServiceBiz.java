package app.biz;

import app.dao.InfoDao;
import app.service.PythonExecute;
import app.tools.TableNameManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceBiz {

    @Autowired
    private InfoDao id;

    private Logger log = Logger.getLogger(ServiceBiz.class);

    public long Service(String url, String key) {
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            //更新表名
            TableNameManager.upDate(fileName);
            long start = System.currentTimeMillis();
            //1.该url是否已经被处理过
            if (!id.isExist(fileName)) {//全新的url
                //先爬取数据存入txt
                log.info("[开始解析数据]");
                //调用python脚本
                PythonExecute.service(url);
                log.info("[数据解析结束],耗时:" + (System.currentTimeMillis() - start));
            }
            return System.currentTimeMillis() - start;
        } catch (Exception e) {
            log.info("[解析数据失败]");
            e.printStackTrace();
            return -1;
        }

    }
}
