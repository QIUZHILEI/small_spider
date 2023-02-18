package com.autumn.spider.engine;

import com.autumn.spider.resolver.ToUriResolver;

public interface Engine {
    //设置爬取网页的解析步骤，resolvers必须按照爬取的步骤顺序严格给出
    void initStep(ToUriResolver[] resolvers);

    //开启爬虫
    void start();
}
