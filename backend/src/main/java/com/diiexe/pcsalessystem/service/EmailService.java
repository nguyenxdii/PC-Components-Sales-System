package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.entity.Order;
import com.diiexe.pcsalessystem.entity.OrderDetail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderConfirmation(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Xác nhận đơn hàng #" + order.getOrderCode() + " - EXEShop");

            String content = buildOrderEmailContent(order);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String buildOrderEmailContent(Order order) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        StringBuilder itemsHtml = new StringBuilder();
        for (OrderDetail detail : order.getOrderDetails()) {
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #eee;'>")
                    .append(detail.getProduct().getName())
                    .append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: center;'>")
                    .append(detail.getQuantity())
                    .append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: right;'>")
                    .append(currencyFormatter.format(detail.getPriceAtPurchase()))
                    .append("</td>")
                    .append("</tr>");
        }

        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden;'>" +
                "  <div style='background: linear-gradient(to right, #f97316, #ea580c); padding: 20px; text-align: center;'>" +
                "    <h1 style='color: white; margin: 0; font-size: 24px;'>EXEShop - Cảm ơn bạn!</h1>" +
                "  </div>" +
                "  <div style='padding: 30px;'>" +
                "    <h2 style='color: #333;'>Xác nhận đơn hàng thành công</h2>" +
                "    <p>Chào <strong>" + order.getUser().getFullName() + "</strong>,</p>" +
                "    <p>Đơn hàng của bạn đã được tiếp nhận thành công và đang được hệ thống xử lý để giao đi nhanh nhất có thể.</p>" +
                "    <div style='background: #fdf2f2; border-left: 4px solid #f97316; padding: 15px; margin: 20px 0;'>" +
                "      <p style='margin: 0;'><strong>Mã đơn hàng:</strong> #" + order.getOrderCode() + "</p>" +
                "      <p style='margin: 5px 0 0 0;'><strong>Ngày đặt:</strong> " + order.getCreatedAt() + "</p>" +
                "    </div>" +
                "    <table style='width: 100%; border-collapse: collapse;'>" +
                "      <thead>" +
                "        <tr style='background: #f8f8f8;'>" +
                "          <th style='padding: 10px; text-align: left;'>Sản phẩm</th>" +
                "          <th style='padding: 10px; text-align: center;'>SL</th>" +
                "          <th style='padding: 10px; text-align: right;'>Giá</th>" +
                "        </tr>" +
                "      </thead>" +
                "      <tbody>" + itemsHtml.toString() + "</tbody>" +
                "    </table>" +
                "    <div style='text-align: right; margin-top: 20px;'>" +
                "      <p style='font-size: 18px; color: #f97316;'><strong>Tổng cộng: " + currencyFormatter.format(order.getFinalPrice()) + "</strong></p>" +
                "    </div>" +
                "    <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>" +
                "      <h4 style='margin-bottom: 10px;'>Thông tin giao hàng:</h4>" +
                "      <p style='margin: 5px 0;'>" + order.getShippingAddress() + "</p>" +
                "      <p style='margin: 5px 0;'>Số điện thoại: " + order.getReceiverPhone() + "</p>" +
                "    </div>" +
                "  </div>" +
                "  <div style='background: #f8f8f8; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
                "    <p>Đây là email tự động, vui lòng không trả lời email này.</p>" +
                "    <p>&copy; 2026 EXEShop. All rights reserved.</p>" +
                "  </div>" +
                "</div>";
    }

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Mã xác thực đăng ký tài khoản - EXEShop");

            String content = buildOtpEmailContent(otp);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String buildOtpEmailContent(String otp) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden;'>" +
                "  <div style='background: linear-gradient(to right, #f97316, #ea580c); padding: 30px; text-align: center;'>" +
                "    <h1 style='color: white; margin: 0; font-size: 28px;'>EXEShop</h1>" +
                "  </div>" +
                "  <div style='padding: 40px; text-align: center;'>" +
                "    <h2 style='color: #333; margin-bottom: 20px;'>Mã xác thực của bạn</h2>" +
                "    <p style='color: #666; font-size: 16px; line-height: 1.6;'>Vui lòng sử dụng mã OTP dưới đây để hoàn tất quá trình đăng ký tài khoản. Mã này có hiệu lực trong 5 phút.</p>" +
                "    <div style='background: #fff7ed; border: 2px dashed #f97316; border-radius: 8px; padding: 20px; margin: 30px 0; display: inline-block;'>" +
                "      <span style='font-size: 36px; font-weight: 900; color: #f97316; letter-spacing: 8px;'>" + otp + "</span>" +
                "    </div>" +
                "    <p style='color: #999; font-size: 12px;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>" +
                "  </div>" +
                "  <div style='background: #f8f8f8; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
                "    <p>&copy; 2026 EXEShop. All rights reserved.</p>" +
                "  </div>" +
                "</div>";
    }
    public void sendPasswordChangeOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Cảnh báo bảo mật: Yêu cầu đổi mật khẩu - EXEShop");

            String content = buildPasswordOtpEmailContent(otp);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String buildPasswordOtpEmailContent(String otp) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 12px; overflow: hidden;'>" +
                "  <div style='background: linear-gradient(to right, #dc2626, #991b1b); padding: 30px; text-align: center;'>" +
                "    <h1 style='color: white; margin: 0; font-size: 28px;'>Cảnh báo Bảo mật</h1>" +
                "  </div>" +
                "  <div style='padding: 40px; text-align: center;'>" +
                "    <h2 style='color: #333; margin-bottom: 20px;'>Yêu cầu đổi mật khẩu</h2>" +
                "    <p style='color: #666; font-size: 16px; line-height: 1.6;'>Hệ thống EXEShop ghi nhận một yêu cầu thay đổi mật khẩu cho tài khoản của bạn. Vui lòng sử dụng mã xác nhận (OTP) dưới đây để hoàn tất quá trình này. Mã có hiệu lực trong 5 phút.</p>" +
                "    <div style='background: #fef2f2; border: 2px dashed #ef4444; border-radius: 8px; padding: 20px; margin: 30px 0; display: inline-block;'>" +
                "      <span style='font-size: 36px; font-weight: 900; color: #ef4444; letter-spacing: 8px;'>" + otp + "</span>" +
                "    </div>" +
                "    <p style='color: #b91c1c; font-size: 14px; font-weight: bold;'>Nếu bạn không phải là người yêu cầu mã này, vui lòng bỏ qua email và thay đổi mật khẩu của mình để đảm bảo an toàn.</p>" +
                "  </div>" +
                "  <div style='background: #f8f8f8; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
                "    <p>&copy; 2026 EXEShop. All rights reserved.</p>" +
                "  </div>" +
                "</div>";
    }
}
