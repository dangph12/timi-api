package com.example.timi_api.infrastructure.message;

public final class Message {

    private Message() {}

    public static final String NOT_FOUND = "Không tìm thấy";
    public static final String SKU_NOT_FOUND = "Không tìm thấy SKU: ";
    public static final String DESIGN_NOT_FOUND = "Không tìm thấy thiết kế: ";
    public static final String ACCOUNT_NOT_FOUND = "Không tìm thấy tài khoản";
    public static final String PART_NOT_FOUND = "Không tìm thấy bộ phận: ";
    public static final String PART_OPTION_NOT_FOUND = "Không tìm thấy tùy chọn bộ phận: ";
    public static final String INVALID_IMAGE_URL = "URL ảnh không hợp lệ";
    public static final String PART_SINGLE_SELECT_ONLY = "Bộ phận này chỉ được chọn 1 tùy chọn: ";
    public static final String MUTEX_GROUP_CONFLICT = "Không thể chọn nhiều tùy chọn trong cùng nhóm: ";
    public static final String CHARACTER_DESIGN_CREATED = "Tạo thiết kế nhân vật thành công";
    public static final String ORDER_CREATED = "Tạo đơn hàng thành công";
    public static final String PAYMENT_SUCCESS = "Thanh toán thành công";
    public static final String INVALID_SIGNATURE = "Chữ ký không hợp lệ";
    public static final String EMAIL_ORDER_CONFIRM_SUBJECT = "Xác nhận đơn hàng #";
    public static final String EMAIL_COD_INTRO = "Đơn hàng của bạn đã được tiếp nhận. Bạn sẽ thanh toán khi nhận hàng.";
    public static final String EMAIL_QR_INTRO = "Cảm ơn bạn đã thanh toán. Đơn hàng của bạn đã được xác nhận.";
}
