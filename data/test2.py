if __name__ == '__main__':
    import sys
    import urllib.request
    import re
    from bs4 import BeautifulSoup
    import time
    import sys
    import json
    from kafka import KafkaProducer

    url = sys.argv[1]
    # 获取页面源代码
    html = urllib.request.urlopen(url).read()
    # 解码
    html = html.decode("gbk")
    # 解析网页
    bs = BeautifulSoup(html, "html.parser")
    # 计数
    num = 0
    # 创建一个生产者
    producer = KafkaProducer(bootstrap_servers=['node4:9092', 'node4:9093', 'node4:9094'])
    # 获取所有li标签并遍历获取其中的href标签
    for i in bs.find_all("li"):
        href = i.a.get("href")
        if href.find("/book/") != -1:
            # 获取链接对应的内容
            content = urllib.request.urlopen(href).read().decode("gbk")
            # 解析页面
            cs = BeautifulSoup(content, "html.parser")
            # 获取所有的div标签
            # 遍历找到id为content的div
            for d in cs.find_all("div"):
                if d.get("id") == "content":
                    # 获取小说文本
                    context = d.get_text()
                    # 将文本按行分开 并且遍历 去掉每个元素中的\xa0
                    for c in context.splitlines():
                        line = "".join(c.split())
                        length = len(line)
                        # 如果内容不为空则写入文件
                        if length > 0:
                            line.replace("style5();", "")
                            line.replace("style6();", "")
                            # 对发送的消息进行编码 否则会出现断言错误 参数类型不匹配
                            msg = json.dumps(line).encode()
                            # 发送到kafka集群 主题名,信息
                            producer.send('newTopic', msg)
                            num = num + 1
