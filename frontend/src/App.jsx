import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AdminLayout from "./layouts/AdminLayout";
import MainLayout from "./layouts/MainLayout";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import DashboardPage from "./pages/admin/DashboardPage";
import ComingSoon from "./pages/admin/ComingSoon";
import HomePage from "./pages/client/HomePage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Auth Routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="products" element={<ComingSoon title="Sản phẩm" />} />
          <Route path="categories" element={<ComingSoon title="Danh mục" />} />
          <Route path="brands" element={<ComingSoon title="Thương hiệu" />} />
          <Route path="orders" element={<ComingSoon title="Đơn hàng" />} />
          <Route path="payments" element={<ComingSoon title="Thanh toán" />} />
          <Route path="users" element={<ComingSoon title="Người dùng" />} />
          <Route path="comments" element={<ComingSoon title="Đánh giá" />} />
          <Route path="vouchers" element={<ComingSoon title="Voucher" />} />
          <Route path="warranties" element={<ComingSoon title="Bảo hành" />} />
          <Route
            path="settings/account"
            element={<ComingSoon title="Tài khoản" />}
          />
          <Route
            path="settings/appearance"
            element={<ComingSoon title="Giao diện" />}
          />
        </Route>

        {/* Customer Routes */}
        <Route path="/" element={<MainLayout />}>
          <Route index element={<HomePage />} />
          <Route
            path="products"
            element={
              <div className="container mx-auto p-8 text-xl">
                Products Listing (Coming soon)
              </div>
            }
          />
          <Route
            path="build-pc"
            element={
              <div className="container mx-auto p-8 text-xl">
                Build PC (Coming soon)
              </div>
            }
          />
          <Route
            path="deals"
            element={
              <div className="container mx-auto p-8 text-xl">
                Deals (Coming soon)
              </div>
            }
          />
          <Route
            path="contact"
            element={
              <div className="container mx-auto p-8 text-xl">
                Contact (Coming soon)
              </div>
            }
          />
          <Route
            path="cart"
            element={
              <div className="container mx-auto p-8 text-xl">
                Shopping Cart (Coming soon)
              </div>
            }
          />
        </Route>

        {/* 404 Route */}
        <Route
          path="*"
          element={
            <div className="min-h-screen flex items-center justify-center text-2xl">
              404 - Chưa làm má
            </div>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
