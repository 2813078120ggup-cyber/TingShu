package com.atguigu.tingshu.common.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 *
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(202, "服务异常"),
    DATA_ERROR(203, "数据异常"),
    ILLEGAL_REQUEST(204, "非法请求"),
    REPEAT_SUBMIT(205, "重复提交"),
    ARGUMENT_VALID_ERROR(206, "参数校验异常"),
    SIGN_ERROR(207, "签名错误"),
    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(209, "没有权限"),
    SIGN_OVERDUE(210, "签名已过期"),


    ACCOUNT_ERROR(211, "账号不正确"),
    PASSWORD_ERROR(212, "密码不正确"),
    PHONE_CODE_ERROR(213, "手机验证码不正确"),
    LOGIN_MOBLE_ERROR( 214, "账号不正确"),
    ACCOUNT_STOP( 215, "账号已停用"),
    NODE_ERROR( 216, "该节点下有子节点，不可以删除"),

    VOD_FILE_ID_ERROR( 217, "声音媒体id不正确"),

    XXL_JOB_ERROR(218, "调度操作失败"),

    ACCOUNT_LESS(219, "账户余额不足"),
    ACCOUNT_LOCK_ERROR(220, "账户余额锁定失败"),
    ACCOUNT_UNLOCK_ERROR(221, "账户余额解锁失败"),
    ACCOUNT_MINUSLOCK_ERROR(222, "账户余额扣减失败"),
    ACCOUNT_LOCK_REPEAT(223, "重复锁定"),
    ORDER_SUBMIT_REPEAT(224, "不能重复提交订单"),

    NO_BUY_NOT_SEE(225, "未购买不能观看"),

    REPEAT_BUY_ERROR(226, "重复购买，请确认后会下单"),

    EXIST_NO_EXPIRE_LIVE(227, "当前存在未过期直播"),

    ;

    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
