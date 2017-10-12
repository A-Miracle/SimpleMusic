package com.ctao.music.network.retrofit2;

/**
 * Created by A Miracle on 2017/5/12.
 * 基于 retrofit2 的网络配置
 */
public class RetrofitConfig {
    public static final String URL_KU_GOU;
    public static final String URL_GIT_HUB;

    static{
        URL_KU_GOU = "http://lyrics.kugou.com/";
        URL_GIT_HUB = "https://raw.githubusercontent.com/";
    }
}
