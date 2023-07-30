package com.blazemaple.constant;

/**
 * @author BlazeMaple
 * @description
 * @date 2023/7/27 17:24
 */
public enum RespCode {
    SUCCESS((byte) 1,"成功"), FAIL((byte)2,"失败");

    private byte code;
    private String desc;

    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
