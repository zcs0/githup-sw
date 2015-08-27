package com.util;

import java.text.DecimalFormat;

/**
 * SizeUtils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-15
 */
public class SizeUtils {

    /** gb to byte **/
    public static final long GB_2_BYTE = 1073741824;
    /** mb to byte **/
    public static final long MB_2_BYTE = 1048576;
    /** kb to byte **/
    public static final long KB_2_BYTE = 1024;
    public static String fileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("0.00");
		String str = fileS < KB_2_BYTE ? df.format((double) fileS) + "B"
				: fileS < MB_2_BYTE ? df.format((double) fileS / KB_2_BYTE) + "K"
						: fileS < GB_2_BYTE ? df
								.format((double) fileS / MB_2_BYTE) + "M" : df
								.format((double) fileS / GB_2_BYTE) + "G";
		return str;
	}
}
