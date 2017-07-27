package com.ctao.music.network.service;

import com.ctao.music.interact.model.KuGouRawLyric;
import com.ctao.music.interact.model.KuGouSearchLyricResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by A Miracle on 2017/7/19.
 */
public interface KuGouApiService {

    @GET("search?ver=1&man=yes&client=pc")
    Call<KuGouSearchLyricResult> searchLyric(@Query("keyword") String songName, @Query("duration") String duration);

    @GET("download?ver=1&client=pc&fmt=lrc&charset=utf8")
    Call<KuGouRawLyric> getRawLyric(@Query("id") String id, @Query("accesskey") String accesskey);

}
