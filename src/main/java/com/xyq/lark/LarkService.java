package com.xyq.lark;


public interface LarkService {


    void start(String userDescription);

    void success();

    void failed();
    
    void abort();
}
