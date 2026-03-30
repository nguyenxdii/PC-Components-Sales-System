import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Form, Input, Button, message, Modal, Spin } from "antd";
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

      // Hiển thị thông báo và chuyển hướng đến trang OTP
      message.success("Đăng ký thành công! Vui lòng kiểm tra email để nhận mã OTP.");
      setTimeout(() => {
        navigate(`/verify-otp?email=${values.email}`);
      }, 1500);
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
    <div className="relative min-h-screen">
      {/* Loading Overlay */}
      {loading && (
        <div className="fixed inset-0 z-[1000] flex flex-col items-center justify-center bg-white/70 backdrop-blur-sm transition-all duration-500">
          <div className="bg-white p-8 rounded-[40px] shadow-2xl flex flex-col items-center gap-6 border border-orange-50">
            <Spin size="large" className="scale-150 custom-orange-spin" />
            <div className="text-center">
              <h3 className="text-xl font-bold text-gray-800 mb-1">Đang gửi mã xác thực...</h3>
              <p className="text-gray-500 text-sm">Vui lòng không đóng trình duyệt trong giây lát.</p>
            </div>
          </div>
        </div>
      )}

      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-50 via-white to-orange-50 p-4">
        <div className="bg-white rounded-[32px] shadow-2xl w-full max-w-md p-10 m-4">
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
              className="rounded-xl h-12"
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
              className="rounded-xl h-12"
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
              className="rounded-xl h-12"
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
              className="rounded-xl h-12"
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
              className="w-full h-12 bg-gradient-to-r from-red-500 to-orange-500 border-none hover:from-red-600 hover:to-orange-600 font-bold rounded-xl shadow-lg shadow-orange-100"
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
    </div>
  );
}
