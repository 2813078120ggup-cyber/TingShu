package com.atguigu.tingshu.common.util;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.util.StringUtils;

@Slf4j
public class PinYinUtils {


    public static void main(String[] args) {
        String str = "唐代诗人李白-libai";
        System.out.println(PinYinUtils.toHanyuPinyin(str));
        System.out.println(PinYinUtils.getFirstLetter(str));
    }

    /**
     * 汉字转拼音
     */
    public static String toHanyuPinyin(String hanzi) {
        if(StringUtils.isEmpty(hanzi)) return "";
        char[] chars = hanzi.trim().toCharArray();
        String hanyupinyin = "";

        //输出格式设置
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        /**
         * 输出大小写设置
         *
         * LOWERCASE:输出小写
         * UPPERCASE:输出大写
         */
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);

        /**
         * 输出音标设置
         *
         * WITH_TONE_MARK:直接用音标符（必须设置WITH_U_UNICODE，否则会抛出异常）
         * WITH_TONE_NUMBER：1-4数字表示音标
         * WITHOUT_TONE：没有音标
         */
//        defaultFormat.setToneType(HanyuPinyinToneType.WITH_TONE_MARK); //  必须设置WITH_U_UNICODE，否则会抛出异常
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        /**
         * 特殊音标ü设置
         *
         * WITH_V：用v表示ü
         * WITH_U_AND_COLON：用"u:"表示ü
         * WITH_U_UNICODE：直接用ü
         */
//        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

        // 中文的正则表达式
        String hanziRegex = "[\\u4E00-\\u9FA5]+";

        try {
            for (int i = 0; i < chars.length; i++) {
                // 判断为中文,则转换为汉语拼音
                if (String.valueOf(chars[i]).matches(hanziRegex)) {
                    hanyupinyin += PinyinHelper
                            .toHanyuPinyinStringArray(chars[i], defaultFormat)[0];
                } else {
                    // 不为中文,则不转换
                    hanyupinyin += chars[i];
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.error("字符不能转成汉语拼音");
        }

        return hanyupinyin;
    }
    /**
     * 取汉字的第一个字符
     */
    public static String getFirstLetter(String hanzi) {
        if(StringUtils.isEmpty(hanzi)) return "";
        char[] chars = hanzi.trim().toCharArray();
        StringBuilder firstPinyin = new StringBuilder();

        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 拼音大写
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 不带声调
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        try {
            String str = String.valueOf(chars[0]);
            // 中文的正则表达式
            String hanziRegex = "[\\u4E00-\\u9FA5]+";
            // 数字表达式
            String numberRegex = "[0-9]+";
            // 字母表达式
            String charRegex = "[a-zA-Z]+";

            for (int i = 0, len = chars.length; i < len; i++) {
                // 判断为汉字,则转为拼音,并取第一个字母
                if (Character.toString(chars[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    // 如果是多音字,只取第一个
                    String[] pys = PinyinHelper.toHanyuPinyinStringArray(chars[i], defaultFormat);
                    firstPinyin.append(pys[0].charAt(0));
                } else {
                    firstPinyin.append(chars[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.error("字符不能转为汉语拼音");
        }

        return firstPinyin.toString();
    }
}

