package com.example.timi_api.infrastructure.message;

public final class Message {

    private Message() {}

    // Not found errors
    public static final String NOT_FOUND = "Không tìm thấy";
    public static final String ORDER_NOT_FOUND = "Không tìm thấy đơn hàng";
    public static final String SKU_NOT_FOUND = "Không tìm thấy SKU: ";
    public static final String DESIGN_NOT_FOUND = "Không tìm thấy thiết kế: ";
    public static final String ACCOUNT_NOT_FOUND = "Không tìm thấy tài khoản";
    public static final String PART_NOT_FOUND = "Không tìm thấy bộ phận: ";
    public static final String PART_OPTION_NOT_FOUND = "Không tìm thấy tùy chọn bộ phận: ";

    // Business errors
    public static final String INSUFFICIENT_STOCK = "Số lượng hàng trong kho không đủ";
    public static final String CANNOT_PAY_ORDER = "Không thể thanh toán đơn hàng này";
    public static final String ORDER_ALREADY_PAID = "Đơn hàng đã được thanh toán";
    public static final String CANNOT_CANCEL_ORDER = "Không thể hủy đơn hàng này";

    // Success messages
    public static final String CHARACTER_DESIGN_CREATED = "Tạo thiết kế nhân vật thành công";
    public static final String ORDER_CREATED = "Tạo đơn hàng thành công";
    public static final String ORDER_CANCELLED = "Đã hủy đơn hàng";
    public static final String GET_ORDER_SUCCESS = "Lấy đơn hàng thành công";
    public static final String LIST_ORDERS_SUCCESS = "Lấy danh sách đơn hàng thành công";
    public static final String LIST_SKUS_SUCCESS = "Lấy danh sách SKU thành công";
    public static final String SKU_CREATED = "Tạo SKU thành công";
    public static final String SKU_UPDATED = "Cập nhật SKU thành công";
    public static final String SKU_DELETED = "Xóa SKU thành công";
    public static final String SKU_QUANTITY_ADJUSTED = "Điều chỉnh số lượng SKU thành công";
    public static final String LIST_PARTS_SUCCESS = "Lấy danh sách bộ phận thành công";
    public static final String LIST_PART_OPTIONS_SUCCESS = "Lấy danh sách tùy chọn thành công";
    public static final String PAYMENT_SUCCESS = "Thanh toán thành công";

    // Payment
    public static final String INVALID_SIGNATURE = "Chữ ký không hợp lệ";

    // Status history notes
    public static final String COD_PAYMENT_NOTE = "COD - chờ thanh toán khi nhận hàng";
    public static final String CUSTOMER_CANCELLED_NOTE = "Khách hàng hủy";
    public static final String QR_PAID_NOTE = "QR - đã thanh toán";
    public static final String QR_EXPIRED_NOTE = "QR thanh toán hết hạn (10 phút)";

    // Email
    public static final String EMAIL_ORDER_CONFIRM_SUBJECT = "Xác nhận đơn hàng #";
    public static final String EMAIL_CONFIRM_INTRO = "Cảm ơn bạn đã đặt hàng! Đơn hàng của bạn đã được tiếp nhận.";

    // Validation — CreateOrder
    public static final String EMAIL_NOT_BLANK = "Email không được để trống";
    public static final String NAME_NOT_BLANK = "Tên không được để trống";
    public static final String PHONE_NOT_BLANK = "Số điện thoại không được để trống";
    public static final String ADDRESS_NOT_BLANK = "Địa chỉ không được để trống";
    public static final String ITEMS_NOT_EMPTY = "Danh sách sản phẩm không được để trống";

    // Validation — CreateOrderItem
    public static final String SKU_ID_NOT_NULL = "Mã SKU không được để trống";
    public static final String DESIGN_ID_NOT_NULL = "Mã thiết kế không được để trống";
    public static final String QUANTITY_MIN_ONE = "Số lượng phải lớn hơn hoặc bằng 1";

    // Validation — CreateCharacterDesign
    public static final String DESIGN_NAME_NOT_BLANK = "Tên thiết kế không được để trống";
    public static final String IMAGE_URL_NOT_BLANK = "URL ảnh không được để trống";
    public static final String PART_SELECTIONS_NOT_EMPTY = "Danh sách lựa chọn bộ phận không được để trống";

    // Validation — CreateCharacterPartSelection
    public static final String PART_OPTION_ID_NOT_NULL = "Mã tùy chọn bộ phận không được để trống";

    // Cart
    public static final String CART_ITEM_ADDED = "Thêm vào giỏ hàng thành công";
    public static final String CART_ITEM_UPDATED = "Cập nhật giỏ hàng thành công";
    public static final String CART_ITEM_REMOVED = "Xóa sản phẩm khỏi giỏ hàng thành công";
    public static final String CART_GET_SUCCESS = "Lấy giỏ hàng thành công";
    public static final String CHECKOUT_SUCCESS = "Đặt hàng thành công";
    public static final String CART_COUNT_SUCCESS = "Lấy số lượng giỏ hàng thành công";
    public static final String CART_ITEM_NOT_FOUND = "Không tìm thấy sản phẩm trong giỏ hàng";
    public static final String CART_ITEMS_NOT_EMPTY = "Vui lòng chọn sản phẩm để thanh toán";

    // Auth
    public static final String EMAIL_EXISTS = "Email đã được đăng ký";
    public static final String REGISTER_SUCCESS = "Đăng ký thành công";
    public static final String LOGIN_SUCCESS = "Đăng nhập thành công";
    public static final String INVALID_CREDENTIALS = "Email hoặc mật khẩu không đúng";
    public static final String REFRESH_TOKEN_INVALID = "Refresh token không hợp lệ hoặc đã hết hạn";
    public static final String TOKEN_REFRESHED = "Token đã được làm mới";
    public static final String AUTH_SUCCESS = "Xác thực thành công";
    public static final String PROFILE_UPDATED = "Cập nhật thông tin thành công";
    public static final String LOGOUT_SUCCESS = "Đăng xuất thành công";
    public static final String UNAUTHORIZED = "Không có quyền truy cập";
    public static final String FORBIDDEN = "Không có quyền thực hiện hành động này";
}
