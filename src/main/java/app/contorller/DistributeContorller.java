package app.contorller;

import app.bean.HotWord;
import app.biz.HandleBiz;
import app.biz.ServiceBiz;
import app.dao.InfoDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.rmi.runtime.Log;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DistributeContorller {

    @Autowired
    private ServiceBiz seb;

    @Autowired
    private HandleBiz hab;

    @Autowired
    private InfoDao id;

    @RequestMapping("/index")
    public String index() {
        return "/index.html";
    }


    private boolean isService = false;

    @RequestMapping("/index/start")
    public @ResponseBody
    Map<String, Integer> start() {
        Map<String, Integer> back = new HashMap<String, Integer>();
        if (!isService) {
            if (hab.Service()) {
                back.put("code", 1);
                isService = true;
            } else {
                back.put("code", -1);
            }
        } else {
            back.put("code", 1);
        }
        return back;
    }

    @RequestMapping("/index/load")
    public @ResponseBody
    Map<String, Long> load(String url, String key) {
        System.out.println(url + ";" + key);
        Map<String, Long> back = new HashMap<String, Long>();
        long time = seb.Service(url, key);
        back.put("code", time);
        return back;
    }

    @RequestMapping("/index/search")
    public @ResponseBody
    Map<String, Object> search(String key, String url) {
        Map<String, Object> back = new HashMap<String, Object>();
        String tableName = url.substring(url.lastIndexOf("/") + 1);
        HotWord hw = new HotWord();
        if (tryCheckTable(1, tableName)) {
            hw = id.getHotWord(key, tableName);
            back.put("code", 1);
        } else {
            back.put("code", -1);
        }
        back.put("hotWord", hw);
        return back;
    }

    private Logger log = Logger.getLogger(this.getClass());

    private boolean tryCheckTable(int num, String tableName) {
        if (num == 4) {
            log.info("[检查失败]");
            return false;
        }
        log.info("[第" + num + "次检查表]");
        if (id.isExist(tableName)) {
            log.info("[存在表]");
            return true;
        } else {
            log.info("[不存在表,等待10秒后检查]");
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            tryCheckTable(num + 1, tableName);
        }
        return false;
    }
}
