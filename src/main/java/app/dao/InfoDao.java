package app.dao;


import app.bean.HotWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InfoDao {
    private JdbcTemplate jp;

    @Autowired
    public void setjp(DataSource ds) {
        this.jp = new JdbcTemplate(ds);
    }

    public HotWord getHotWord(String key, String tableName) {
        System.out.println("key:" + key + ";name:" + tableName);
        HotWord hw = null;
        try {
            //返回值
            hw = new HotWord();
            //查询key的sql语句
            String sql = "SELECT * from `" + tableName + "` where word Like '%" + key + "%' ORDER BY count desc";
            //关于key的返回结果
            List<Map<String, Object>> keyWords = jp.queryForList(sql);
            //存储key的相关热词
            List<Map<String, String>> otherWords = new ArrayList<>();
            //如果没有查到与key相关的热词 则选取词频最高的十条词汇作为key的相关热词
            if (keyWords.size() <= 0) {
                try {
                    //查询全表中词频最高的十条
                    List<Map<String, Object>> lm = jp.queryForList("SELECT * from `" + tableName + "` ORDER BY count desc LIMIT 10");
                    //存入li
                    for (int i = 0; i < 10; i++) {
                        Map<String, String> mm = new HashMap<>();
                        mm.put("key", lm.get(i).get("word") + "");
                        mm.put("value", lm.get(i).get("count") + "");
                        otherWords.add(mm);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hw.setList(otherWords);
                //设置该热词不存在
                hw.setExist(false);
                //返回
                return hw;
            }
            //如果key存在 但是相关热词不足十条 则取词频最高的几条补足十条
            int end = 11 > keyWords.size() ? keyWords.size() : 11;

            for (int index = 0; index < end; index++) {
                //第一条是key
                if (index == 0) {
                    hw.setWord(keyWords.get(index).get("word") + "");
                    hw.setCount(keyWords.get(index).get("count") + "");
                } else {
                    //其余的是相关热词
                    Map<String, String> mm = new HashMap<>();
                    mm.put("key", keyWords.get(index).get("word") + "");
                    mm.put("value", keyWords.get(index).get("count") + "");
                    otherWords.add(mm);
                }
            }
            hw.setList(otherWords);
            if (keyWords.size() < 11) {
                try {
                    List<Map<String, Object>> lm = jp.queryForList("SELECT * from `" + tableName + "` ORDER BY count desc LIMIT 10");
                    for (int i = 0; i < (11 - keyWords.size()); i++) {
                        Map<String, String> mm = new HashMap<>();
                        mm.put("key", lm.get(i).get("word") + "");
                        mm.put("value", lm.get(i).get("count") + "");
                        otherWords.add(mm);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            hw.setList(otherWords);
            hw.setExist(true);
            System.out.println("最终结果:" + hw);
            return hw;
        } catch (
                Exception e) {
            e.printStackTrace();
            return hw;
        }
    }

    public boolean isExist(String tableName) {
        try {
            String sql = "SELECT * from `" + tableName + "` ";
            jp.queryForList(sql);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
