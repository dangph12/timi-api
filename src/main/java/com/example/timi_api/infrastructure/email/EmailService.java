package com.example.timi_api.infrastructure.email;

import com.example.timi_api.domain.entity.Order;
import com.example.timi_api.domain.entity.OrderItem;
import com.example.timi_api.infrastructure.message.Message;
import com.example.timi_api.infrastructure.repository.OrderRepository;
import com.samskivert.mustache.Mustache;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;
    private final Mustache.Compiler mustache;

    @Async
    @Transactional(readOnly = true)
    public void sendOrderConfirmation(Order order) {
        Order reloaded = orderRepository.findById(order.getId()).orElseThrow();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(reloaded.getEmail());
            helper.setSubject(Message.EMAIL_ORDER_CONFIRM_SUBJECT + reloaded.getPublicId());
            helper.setText(render(reloaded), true);
            mailSender.send(message);
            log.info("Email sent to {} for order {}", reloaded.getEmail(), reloaded.getPublicId());
        } catch (Exception e) {
            log.error("Failed to send email for order {}", reloaded.getPublicId(), e);
        }
    }

    private String render(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            BigDecimal subtotal = item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("skuCode", item.getSku().getSkuCode());
            itemMap.put("sizeName", item.getSku().getSize().getName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("imageUrl", item.getCharacterDesign().getImageUrl());
            itemMap.put("price", formatPrice(item.getPriceAtPurchase()));
            itemMap.put("subtotal", formatPrice(subtotal));
            items.add(itemMap);
        }

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("intro", Message.EMAIL_CONFIRM_INTRO);
        ctx.put("publicId", order.getPublicId());
        ctx.put("name", order.getName());
        ctx.put("phone", order.getPhone());
        ctx.put("address", order.getAddress());
        ctx.put("items", items);
        ctx.put("total", formatPrice(total));

        return mustache.loadTemplate("email/order-confirmation").execute(ctx);
    }

    private String formatPrice(BigDecimal price) {
        return new DecimalFormat("#,###").format(price);
    }
}
