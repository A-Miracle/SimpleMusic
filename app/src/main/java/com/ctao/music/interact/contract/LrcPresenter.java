package com.ctao.music.interact.contract;

import com.ctao.music.interact.model.KuGouRawLyric;
import com.ctao.music.interact.model.KuGouSearchLyricResult;
import com.ctao.music.network.retrofit2.RetrofitConfig;
import com.ctao.music.network.retrofit2.RetrofitFactory;
import com.ctao.music.network.service.KuGouApiService;
import com.ctao.music.utils.LyricUtils;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by A Miracle on 2017/7/19.
 */
public class LrcPresenter implements LrcContract.Presenter{

    private final LrcContract.View mLrcView;

    public LrcPresenter(LrcContract.View lrcView) {
        this.mLrcView = lrcView;
    }

    @Override
    public void downloadLrcFile(final String title, final String artist, long duration) {
        Retrofit retrofit = RetrofitFactory.getInstance().getRetrofit(RetrofitConfig.URL_KU_GOU);
        final KuGouApiService apiService = retrofit.create(KuGouApiService.class);
        Call<KuGouSearchLyricResult> result = apiService.searchLyric(title, String.valueOf(duration));
        result.enqueue(new Callback<KuGouSearchLyricResult>() {
            @Override
            public void onResponse(Call<KuGouSearchLyricResult> call, Response<KuGouSearchLyricResult> response) {
                KuGouSearchLyricResult body = response.body();
                if (body != null
                        && body.status == 200
                        && body.candidates != null
                        && body.candidates.size() != 0) {
                    KuGouSearchLyricResult.Candidates candidates = body.candidates.get(0);
                    getRawLyric(apiService, title, artist, candidates.id, candidates.accesskey);
                    return;
                }
                mLrcView.showLyric(null);
            }

            @Override
            public void onFailure(Call<KuGouSearchLyricResult> call, Throwable t) {
                mLrcView.showLyric(null);
            }
        });
    }

    private void getRawLyric(KuGouApiService apiService, final String title, final String artist, String id, String accesskey) {
        Call<KuGouRawLyric> lyric = apiService.getRawLyric(id, accesskey);
        lyric.enqueue(new Callback<KuGouRawLyric>() {
            @Override
            public void onResponse(Call<KuGouRawLyric> call, Response<KuGouRawLyric> response) {
                KuGouRawLyric body = response.body();
                if(null == body){
                    mLrcView.showLyric(null);
                    return;
                }
                String rawLyric = LyricUtils.decryptBASE64(body.content);
                File file = LyricUtils.writeLrcToLoc(title, artist, rawLyric);
                mLrcView.showLyric(file);
            }

            @Override
            public void onFailure(Call<KuGouRawLyric> call, Throwable t) {
                mLrcView.showLyric(null);
            }
        });
    }
}
