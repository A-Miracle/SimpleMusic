package com.ctao.music.network.service;

import com.ctao.music.interact.model.Update;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by A Miracle on 2017/8/29.
 */
public interface UpdateApiService {
    @GET("A-Miracle/SimpleMusic/master/update.json")
    Call<Update> checkUpdate();

    @GET("A-Miracle/SimpleMusic/master/{fileName}")
    Call<ResponseBody> downloadAPk(@Path("fileName") String fileName);
}
