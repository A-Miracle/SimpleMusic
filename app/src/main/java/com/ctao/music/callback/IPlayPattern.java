package com.ctao.music.callback;

/**
 * Created by A Miracle on 2017/7/7.
 */
public interface IPlayPattern {
    /** 循环 */
    int PATTERN_CYCLE = 0XA;
    /** 单曲 */
    int PATTERN_SINGLE = 0XB;
    /** 列表 */
    int PATTERN_LIST = 0XC;
    /** 随机 */
    int PATTERN_RANDOM = 0XD;

    int getPattern();
}
