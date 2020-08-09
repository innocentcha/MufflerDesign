package com.weining.android.util;

/**
 *  freq means frequency
 *  tl means transmission loss
 *  tlNum means the num of tl data in one combination of perforated pipe statistics
 *  tlNum is calculated by (freqMax - freqMin) / freqStep + 1
 *  tlStickNum is the num of tl data in one combination of insertion tube statistics
 *  don't edit the file if not necessary!!
 */
public class CountUtil {
    public static final int freqMin = 200;
    public static final int freqMax = 8000;
    public static final int freqStep = 10;
    public static final int tlNum = 781;
    public static final int tlStickNum = 1181;
    public static final int StickIdStep = 264;
}
