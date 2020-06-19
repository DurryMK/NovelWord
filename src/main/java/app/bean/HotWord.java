package app.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotWord {
    private String word;
    private String count;
    private List<Map<String,String>> list = new ArrayList<>();
    private boolean exist;

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }


    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public List<Map<String,String>> getList() {
        return list;
    }

    public void setList(List<Map<String,String>> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "HotWord{" +
                "word='" + word + '\'' +
                ", count='" + count + '\'' +
                ", list=" + list +
                ", exist=" + exist +
                '}';
    }
}
