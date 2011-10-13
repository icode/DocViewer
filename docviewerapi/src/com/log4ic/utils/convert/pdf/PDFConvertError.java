package com.log4ic.utils.convert.pdf;

/**
 * @author: 张立鑫
 * @date: 11-8-19 下午1:05
 */
public class PDFConvertError {

    public PDFConvertError() {
    }

    public PDFConvertError(PDFConvertErrorType type) {
        this.type = type;
    }

    public PDFConvertError(PDFConvertErrorType type, int position) {
        this.type = type;
        this.position = position;
    }

    private PDFConvertErrorType type = PDFConvertErrorType.NONE;
    private Integer position;

    public PDFConvertErrorType getType() {
        return type;
    }

    public Integer getPosition() {
        return position;
    }

    public void setType(PDFConvertErrorType type) {

        this.type = type;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
