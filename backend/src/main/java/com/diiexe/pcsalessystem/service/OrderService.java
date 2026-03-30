package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.MoMoPaymentResponse;
import com.diiexe.pcsalessystem.dto.OrderRequest;
import com.diiexe.pcsalessystem.dto.OrderResponse;
import com.diiexe.pcsalessystem.entity.*;
import com.diiexe.pcsalessystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoMoService moMoService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) throws Exception {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng không có sản phẩm nào");
        }

        // 1. Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setShippingAddress(request.getShippingAddress());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setStatus("PENDING");

        double total = 0;
        List<OrderDetail> details = new ArrayList<>();
        for (CartItem item : cart.getCartItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setPriceAtPurchase(item.getProduct().getPrice());
            details.add(detail);
            total += detail.getPriceAtPurchase() * detail.getQuantity();
        }

        order.setTotalPrice(total);
        
        // --- Xử lý Voucher ---
        double finalPrice = total;
        boolean voucherFreeShip = false;
        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode().trim())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));
            
            // Validate Voucher
            LocalDateTime now = LocalDateTime.now();
            if (Boolean.FALSE.equals(voucher.getIsActive())) {
                throw new RuntimeException("Mã giảm giá đã bị vô hiệu hóa");
            }
            if (voucher.getQuantity() != null && voucher.getQuantity() <= 0) {
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
            }
            if (voucher.getExpirationDate() != null && voucher.getExpirationDate().isBefore(now)) {
                throw new RuntimeException("Mã giảm giá đã hết hạn");
            }
            if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
                throw new RuntimeException("Mã giảm giá chưa đến ngày hiệu lực");
            }
            if (voucher.getMinOrderValue() != null && total < voucher.getMinOrderValue()) {
                throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này (Tối thiểu: " + voucher.getMinOrderValue().longValue() + "đ)");
            }

            // Tính toán giá giảm
            if (Boolean.TRUE.equals(voucher.getIsFreeShip())) {
                voucherFreeShip = true;
            } else {
                double discount = 0;
                if ("PERCENT".equals(voucher.getType())) {
                    discount = (total * voucher.getValue()) / 100;
                    if (voucher.getMaxDiscountValue() != null && discount > voucher.getMaxDiscountValue()) {
                        discount = voucher.getMaxDiscountValue();
                    }
                } else if ("FIXED".equals(voucher.getType())) {
                    discount = voucher.getValue();
                }
                finalPrice = total - discount;
            }

            if (finalPrice < 0) finalPrice = 0;

            order.setVoucher(voucher);
            // Giảm số lượng voucher
            if (voucher.getQuantity() != null) {
                voucher.setQuantity(voucher.getQuantity() - 1);
                voucherRepository.save(voucher);
            }
        }

        order.setFinalPrice(finalPrice);
        
        // --- Xử lý Phí vận chuyển ---
        double shippingFee = (total >= 1000000 || voucherFreeShip) ? 0 : 30000;
        order.setShippingFee(shippingFee);
        order.setFinalPrice(order.getFinalPrice() + shippingFee);

        order.setOrderDetails(details);

        // 2. Tạo thông tin thanh toán
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(order.getFinalPrice());
        payment.setStatus("UNPAID");
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        // 3. Xóa giỏ hàng và gửi email nếu là COD
        if (!"MOMO".equals(request.getPaymentMethod())) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
            emailService.sendOrderConfirmation(savedOrder);
        }

        // 4. Xử lý MoMo nếu cần
        OrderResponse response = new OrderResponse();
        response.setId(savedOrder.getId());
        response.setOrderCode(savedOrder.getOrderCode());
        response.setFinalPrice(savedOrder.getFinalPrice());
        response.setStatus(savedOrder.getStatus());

        if ("MOMO".equals(request.getPaymentMethod())) {
            MoMoPaymentResponse momoResponse = moMoService.createPayment(savedOrder);
            if (momoResponse != null && momoResponse.getResultCode() == 0) {
                response.setPaymentUrl(momoResponse.getPayUrl());
            } else {
                throw new RuntimeException("Lỗi khi tạo yêu cầu thanh toán MoMo: " + 
                    (momoResponse != null ? momoResponse.getMessage() : "Không có phản hồi"));
            }
        }

        return response;
    }

    @Transactional
    public void updatePaymentStatus(String orderCode, String transactionStatus, String transactionId) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        Payment payment = order.getPayment();
        if (payment == null) return;

        if ("0".equals(transactionStatus) || "PAID".equals(transactionStatus)) {
            payment.setStatus("PAID");
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionId(transactionId);
            order.setStatus("PROCESSING");

            // TRỪ STOCK KHI THANH TOÁN THÀNH CÔNG
            for (OrderDetail detail : order.getOrderDetails()) {
                Product product = detail.getProduct();
                if (product != null) {
                    int newStock = product.getStock() - detail.getQuantity();
                    if (newStock < 0) newStock = 0; // Tránh âm kho
                    product.setStock(newStock);
                    productRepository.save(product);
                }
            }
            
            // 1. Xóa giỏ hàng sau khi thanh toán MOMO thành công
            Cart cart = cartRepository.findByUserId(order.getUser().getId()).orElse(null);
            if (cart != null) {
                cart.getCartItems().clear();
                cartRepository.save(cart);
            }

            // 2. Gửi mail xác nhận
            emailService.sendOrderConfirmation(order);
        } else {
            payment.setStatus("FAILED");
            order.setStatus("CANCELLED");
        }

        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void checkAndUpdatePaymentStatus(String orderCode) throws Exception {
        com.diiexe.pcsalessystem.dto.MoMoQueryStatusResponse response = moMoService.queryPaymentStatus(orderCode);
        
        // resultCode = 0 có nghĩa là thanh toán thành công
        if (response.getResultCode() != null && response.getResultCode() == 0) {
            updatePaymentStatus(orderCode, "0", String.valueOf(response.getTransId()));
        } else {
            // Nếu vẫn thất bại hoặc chưa thanh toán, có thể cập nhật log hoặc giữ nguyên
            System.out.println("Query status for " + orderCode + ": " + response.getMessage());
        }
    }
}
