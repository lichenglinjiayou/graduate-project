package com.lichenglin.common.constant;

public class RepositoryConstant {
    public enum PurchaseStatusEnum{
        CREATE_STATUS(0,"新建"),ASSIGN_STATUS(1,"已分配"),
        RECEIVE_STATUS(2,"已领取"),FINISH(3,"已完成"),
        ERROR_STATUS(4,"异常");

        private int code;
        private String message;

        PurchaseStatusEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

    public enum PurchaseDetailStatusEnum{
        CREATE_STATUS(0,"新建"),ASSIGN_STATUS(1,"已分配"),
        RECEIVE_STATUS(2,"正在采购"),FINISH(3,"已完成"),
        ERROR_STATUS(4,"采购失败");

        private int code;
        private String message;

        PurchaseDetailStatusEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }
}
