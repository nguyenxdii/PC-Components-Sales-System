import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Form, Input, Button, message, Modal } from "antd";
import {
  UserOutlined,
  MailOutlined,
  LockOutlined,
  PhoneOutlined,
  HomeOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
  ArrowLeftOutlined,
} from "@ant-design/icons";
import { authAPI } from "../../services/api";

export default function RegisterPage() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(3);
  const navigate = useNavigate();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      await authAPI.register({
        fullName: values.fullName,
        email: values.email,
        password: values.password,
        phoneNumber: values.phoneNumber,
        address: values.address,
      });

      // Show countdown modal
      let count = 3;
      const modal = Modal.success({
        title: "🎉 Đăng ký thành công!",
        content: `Chào mừng bạn đến với PC Sales! Đang chuyển đến trang đăng nhập sau ${count} giây...`,
        okButtonProps: { style: { display: "none" } },
      });

      const interval = setInterval(() => {
        count--;
        setCountdown(count);
        if (count > 0) {
          modal.update({
            content: `Chào mừng bạn đến với PC Sales! Đang chuyển đến trang đăng nhập sau ${count} giây...`,
          });
        } else {
          clearInterval(interval);
          modal.destroy();
          navigate(`/login?email=${values.email}`);
        }
      }, 1000);
    } catch (error) {
      const errorMsg =
        error.response?.data?.message ||
        error.response?.data?.email ||
        "Đăng ký thất bại";
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-50 via-white to-orange-50">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8 m-4">
        {/* Logo */}
        <div className="text-center mb-6">
          <div className="flex justify-center mb-4">
            <img
              src="/logo-exeshop.png"
              alt="PC Sales Logo"
              className="h-16 w-auto"
            />
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">
            Đăng ký tài khoản
          </h1>
          <p className="text-gray-600">Tạo tài khoản mới để mua sắm</p>
        </div>

        {/* Form */}
        <Form
          form={form}
          name="register"
          onFinish={onFinish}
          layout="vertical"
          requiredMark={false}
          validateTrigger="onSubmit"
        >
          <Form.Item
            name="fullName"
            rules={[{ required: true, message: "Vui lòng nhập họ tên!" }]}
          >
            <Input
              prefix={<UserOutlined className="text-gray-400" />}
              placeholder="Họ và tên"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="email"
            rules={[
              { required: true, message: "Vui lòng nhập email!" },
              { type: "email", message: "Email không hợp lệ!" },
            ]}
          >
            <Input
              prefix={<MailOutlined className="text-gray-400" />}
              placeholder="Email"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: "Vui lòng nhập mật khẩu!" },
              { min: 8, message: "Mật khẩu phải có ít nhất 8 ký tự!" },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="Mật khẩu"
              size="large"
              iconRender={(visible) =>
                visible ? <EyeOutlined /> : <EyeInvisibleOutlined />
              }
            />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            dependencies={["password"]}
            rules={[
              { required: true, message: "Vui lòng xác nhận mật khẩu!" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue("password") === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(
                    new Error("Mật khẩu xác nhận không khớp!"),
                  );
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined className="text-gray-400" />}
              placeholder="Xác nhận mật khẩu"
              size="large"
              iconRender={(visible) =>
                visible ? <EyeOutlined /> : <EyeInvisibleOutlined />
              }
            />
          </Form.Item>

          <Form.Item name="phoneNumber">
            <Input
              prefix={<PhoneOutlined className="text-gray-400" />}
              placeholder="Số điện thoại (không bắt buộc)"
              size="large"
            />
          </Form.Item>

          <Form.Item name="address">
            <Input.TextArea
              placeholder="Địa chỉ (không bắt buộc)"
              rows={2}
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              loading={loading}
              className="w-full bg-gradient-to-r from-red-500 to-orange-500 border-none hover:from-red-600 hover:to-orange-600 font-semibold"
              style={{
                background: "linear-gradient(to right, #ef4444, #f97316)",
              }}
            >
              Đăng ký
            </Button>
          </Form.Item>
        </Form>

        {/* Links */}
        <div className="text-center mt-4">
          {/* Divider */}
          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-4 bg-white text-gray-500">Hoặc</span>
            </div>
          </div>

          {/* Back to Home Button */}
          <Link
            to="/"
            className="w-full flex items-center justify-center gap-2 px-4 py-3 border-2 border-gray-200 rounded-lg text-gray-700 font-medium hover:bg-gray-50 hover:border-gray-300 transition-all duration-200 mb-8"
          >
            <ArrowLeftOutlined className="text-lg" />
            Quay lại trang chủ
          </Link>

          {/* Login Link */}
          <p className="text-center text-sm text-gray-600">
            Đã có tài khoản?{" "}
            <Link
              to="/login"
              className="font-semibold text-red-500 hover:text-red-600 transition-colors"
            >
              Đăng nhập
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
