package com.example.timi_api.domain.constant;

public enum SkuTransactionType {
    ORDER_OUT(-1),
    RESTOCK_IN(1),
    MANUAL_EXPORT(-1),
    MANUAL_ADJUST_INCREASE(1),
    MANUAL_ADJUST_DECREASE(-1);

    private final int sign;

    SkuTransactionType(int sign) {
        this.sign = sign;
    }

    public int applySign(int quantity) {
        return quantity * this.sign;
    }
}
