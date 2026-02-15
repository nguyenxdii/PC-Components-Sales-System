import { Outlet, Link, useNavigate } from "react-router-dom";
import {
  ShoppingCartOutlined,
  UserOutlined,
  SearchOutlined,
  LogoutOutlined,
  SettingOutlined,
  ShoppingOutlined,
} from "@ant-design/icons";
import { useState, useEffect } from "react";
import { Dropdown } from "antd";

export default function MainLayout() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const userData = localStorage.getItem("user");
    if (userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (error) {
        console.error("Error parsing user data:", error);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Chỉ chạy 1 lần khi component mount

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
    navigate("/login");
  };

  const menuItems = [
    {
      key: "account",
      icon: <UserOutlined />,
      label: "Thông tin tài khoản",
      onClick: () => navigate("/profile"),
    },
    {
      key: "orders",
      icon: <ShoppingOutlined />,
      label: "Quản lý đơn hàng",
      onClick: () => navigate("/orders"),
    },
    {
      type: "divider",
    },
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: "Đăng xuất",
      onClick: handleLogout,
      danger: true,
    },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-50">
        <div className="container mx-auto px-4">
          {/* Top bar */}
          <div className="flex items-center justify-between py-4">
            {/* Logo */}
            <Link to="/" className="flex items-center">
              <img
                src="/logo-exeshop.png"
                alt="EXEShop Logo"
                className="h-12 w-auto object-contain hover:opacity-80 transition-opacity"
              />
            </Link>

            {/* Search bar */}
            <div className="flex-1 max-w-2xl mx-8">
              <div className="relative">
                <input
                  type="text"
                  placeholder="Tìm kiếm sản phẩm..."
                  className="w-full px-4 py-2 pr-10 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                />
                <button className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-500 hover:text-orange-500">
                  <SearchOutlined style={{ fontSize: "20px" }} />
                </button>
              </div>
            </div>

            {/* Right section */}
            <div className="flex items-center gap-6">
              {/* Cart */}
              <Link
                to="/cart"
                className="flex items-center gap-2 hover:text-orange-500"
              >
                <div className="relative">
                  <ShoppingCartOutlined style={{ fontSize: "24px" }} />
                  <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                    0
                  </span>
                </div>
                <span>Giỏ hàng</span>
              </Link>

              {/* User */}
              {user ? (
                <Dropdown
                  menu={{ items: menuItems }}
                  trigger={["click"]}
                  placement="bottomRight"
                >
                  <div className="flex items-center gap-3 cursor-pointer hover:opacity-80 transition-opacity">
                    <span className="text-gray-700">
                      Xin chào, {user.fullName}
                    </span>
                    <img
                      src={user.avatar || "/user-default.jpg"}
                      alt={user.fullName}
                      className="w-10 h-10 rounded-full object-cover shadow-md hover:shadow-lg transition-shadow border-2 border-gray-200"
                    />
                  </div>
                </Dropdown>
              ) : (
                <div className="flex items-center gap-3">
                  <Link
                    to="/register"
                    className="px-4 py-2 text-gray-700 hover:text-orange-500 font-medium transition-colors"
                  >
                    Đăng ký
                  </Link>
                  <Link
                    to="/login"
                    className="flex items-center gap-2 px-5 py-2 bg-gradient-to-r from-red-500 to-orange-500 text-white rounded-md hover:from-red-600 hover:to-orange-600 transition-all shadow-md hover:shadow-lg"
                  >
                    <UserOutlined />
                    <span>Đăng nhập</span>
                  </Link>
                </div>
              )}
            </div>
          </div>

          {/* Navigation menu */}
          <nav className="border-t border-gray-200">
            <ul className="flex items-center gap-4 py-3">
              <li>
                <Link
                  to="/"
                  className="text-gray-700 hover:text-orange-500 font-medium"
                >
                  Trang chủ
                </Link>
              </li>
              <li className="text-gray-300 select-none">|</li>
              <li>
                <Link
                  to="/products"
                  className="text-gray-700 hover:text-orange-500 font-medium"
                >
                  Sản phẩm
                </Link>
              </li>
              <li className="text-gray-300 select-none">|</li>
              <li>
                <Link
                  to="/build-pc"
                  className="text-gray-700 hover:text-orange-500 font-medium"
                >
                  Xây dựng PC
                </Link>
              </li>
              <li className="text-gray-300 select-none">|</li>
              <li>
                <Link
                  to="/deals"
                  className="text-gray-700 hover:text-orange-500 font-medium"
                >
                  Khuyến mãi
                </Link>
              </li>
              <li className="text-gray-300 select-none">|</li>
              <li>
                <Link
                  to="/contact"
                  className="text-gray-700 hover:text-orange-500 font-medium"
                >
                  Liên hệ
                </Link>
              </li>
            </ul>
          </nav>
        </div>
      </header>

      {/* Main content */}
      <main className="flex-1">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-gray-900 text-white mt-12">
        <div className="container mx-auto px-4 py-8">
          <div className="grid grid-cols-4 gap-8">
            <div>
              <h3 className="text-lg font-bold mb-4">Về chúng tôi</h3>
              <p className="text-gray-400 text-sm">
                Hệ thống bán linh kiện PC uy tín, chất lượng cao với giá cả cạnh
                tranh.
              </p>
            </div>
            <div>
              <h3 className="text-lg font-bold mb-4">Chính sách</h3>
              <ul className="space-y-2 text-sm text-gray-400">
                <li>Chính sách bảo hành</li>
                <li>Chính sách đổi trả</li>
                <li>Chính sách vận chuyển</li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-bold mb-4">Hỗ trợ</h3>
              <ul className="space-y-2 text-sm text-gray-400">
                <li>Hotline: 098.655.2233</li>
                <li>Email: support@pcsales.vn</li>
                <li>Thời gian: 8:00 - 22:00</li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-bold mb-4">Kết nối</h3>
              <div className="flex gap-4 text-2xl">
                <a href="#" className="hover:text-orange-500">
                  Facebook
                </a>
                <a href="#" className="hover:text-orange-500">
                  Youtube
                </a>
              </div>
            </div>
          </div>
          <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400 text-sm">
            © 2024 PC Sales System. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}
