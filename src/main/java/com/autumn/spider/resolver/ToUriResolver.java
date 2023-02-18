package com.autumn.spider.resolver;


import java.net.http.HttpResponse;
import java.util.ArrayList;

public interface ToUriResolver {
    ArrayList<String> resolver(HttpResponse<String> response);
}
