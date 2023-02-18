package com.autumn.spider.resolver;

import java.net.http.HttpResponse;

public interface ToDataResolver<T> {
    T resolver(HttpResponse<String> response);
}
