package com.autumn.spider.persistence;


public interface Persistence<T> {

    void persistence(T data) throws Exception;

    void closeResources() throws Exception;
}
