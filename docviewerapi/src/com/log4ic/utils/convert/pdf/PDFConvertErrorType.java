package com.log4ic.utils.convert.pdf;

/**
 * @author: 张立鑫
 * @date: 11-8-19 下午1:06
 */
public enum PDFConvertErrorType {
    //没有错误
    NONE {
        int getErrorCode() {
            return -1;
        }

        String getErrorMessage() {
            return "";
        }

    },
    //文档过于复杂
    COMPLEX {
        int getErrorCode() {
            return 1;
        }

        String getErrorMessage() {
            return "ERROR   This file is too complex";
        }

    },
    //不支持字符集
    INVALID_CHARID {
        int getErrorCode() {
            return 2;
        }

        String getErrorMessage() {
            return "ERROR   Invalid charid";
        }


    };

    abstract int getErrorCode();

    abstract String getErrorMessage();
}
