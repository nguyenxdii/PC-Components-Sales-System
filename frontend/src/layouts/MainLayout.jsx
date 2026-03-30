import { Outlet, Link, useNavigate } from "react-router-dom";
import { 
  ShoppingCartOutlined, 
  UserOutlined, 
  SearchOutlined, 
  LogoutOutlined, 
  ShoppingOutlined,
  AppstoreOutlined,
  MenuOutlined,
  RightOutlined,
  ThunderboltOutlined
} from "@ant-design/icons";
import { useState, useEffect } from "react";
import { Dropdown, Badge, Divider } from "antd";
import { useCart } from "../contexts/CartContext";
import { categoryService } from "../services/categoryService";

export default function MainLayout() {
  const { cartCount } = useCart();
  const [user, setUser] = useState(null);
  const [categories, setCategories] = useState([]);
  const navigate = useNavigate();

  const API_URL = "http://localhost:8080";

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const tree = await categoryService.getCategoryTree();
        setCategories(tree);
      } catch (error) {
        console.error("Error fetching categories:", error);
      }
    };
    fetchCategories();

    const userData = localStorage.getItem("user");
    if (userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (error) {
        console.error("Error parsing user data:", error);
      }
    }
  }, []);

  const getFullImageUrl = (url) => {
    if (!url) return "/images/cat-placeholder.png";
    if (url.startsWith("http")) return url;
    // Vì ảnh hiện đã ở thư mục public của FE, ta trả về url trực tiếp
    return url;
  };

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
      <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
         <div className="container mx-auto px-4">
            <div className="flex items-center h-16 gap-2 md:gap-8">
               {/* Logo */}
               <Link to="/" className="flex-shrink-0">
                  <img src="/logo-exeshop.png" alt="Logo" className="h-9 w-auto" />
               </Link>

               {/* Category Menu Trigger */}
               <div className="nav-megamenu-trigger h-full flex items-center">
                  <button className="flex items-center gap-2 px-3 h-10 bg-primary text-white font-bold text-[12px] uppercase hover:opacity-90 transition-all rounded-lg cursor-pointer tracking-wider border-none">
                     <MenuOutlined className="text-base" />
                     <span className="hidden lg:block">Danh mục</span>
                  </button>
                  
                  <div className="megamenu-dropdown" style={{ top: '64px' }}>
                     {categories.map((cat) => (
                        <div key={cat.id} className="megamenu-item group/mitem">
                           <div className="flex items-center gap-2">
                              <AppstoreOutlined className="text-gray-400" />
                              <span>{cat.name}</span>
                           </div>
                           <RightOutlined className="text-[10px] opacity-30" />

                           <div className="megamenu-sub-panel">
                              <div className="grid grid-cols-4 gap-4">
                              {cat.children && cat.children.map((child) => (
                                 <Link 
                                    key={child.id} 
                                    to={`/products?category=${child.id}`} 
                                    className="cat-level-2-card group/card"
                                 >
                                    <img 
                                       src={getFullImageUrl(child.iconUrl) || "/images/placeholder.png"} 
                                       alt={child.name} 
                                       className="cat-level-2-img group-hover/card:scale-110 transition-transform" 
                                       onError={(e) => { e.target.src = "/images/placeholder.png"; }} 
                                    />
                                    <span className="cat-level-2-title">{child.name}</span>
                                 </Link>
                              ))}
                              </div>
                           </div>
                        </div>
                     ))}
                  </div>
               </div>

               {/* Search - Hơi nhỏ lại để fit */}
               <div className="flex-1 max-w-md hidden md:block">
                  <div className="relative">
                     <input
                        type="text"
                        placeholder="Tìm kiếm sản phẩm..."
                        className="w-full px-4 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:border-primary focus:bg-white transition-all outline-none text-sm"
                     />
                     <button className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                        <SearchOutlined />
                     </button>
                  </div>
               </div>

               {/* Actions */}
               <div className="flex items-center gap-4 ml-auto">
                  <Link to="/cart" className="flex items-center gap-2 p-2 text-gray-700 hover:text-primary transition-all hover:bg-gray-50 rounded-lg group">
                     <Badge count={cartCount} size="small" color="var(--primary-color)">
                        <ShoppingCartOutlined className="text-xl" />
                     </Badge>
                     <span className="hidden sm:block font-bold text-[10px] uppercase group-hover:text-primary">Giỏ hàng</span>
                  </Link>

                  {user ? (
                     <Dropdown menu={{ items: menuItems }} trigger={["click"]}>
                        <div className="flex items-center gap-2 cursor-pointer group p-1 hover:bg-gray-50 rounded-lg transition-all">
                           <img
                               src={user.avatarUrl || "/user-default.jpg"}
                               alt="Avatar"
                               className="w-8 h-8 rounded-full border border-gray-100"
                           />
                           <span className="hidden sm:block text-[11px] font-bold text-gray-700 group-hover:text-primary">
                             {user.fullName}
                           </span>
                        </div>
                     </Dropdown>
                  ) : (
                     <Link to="/login" className="px-5 py-2.5 bg-primary text-white text-[11px] font-black rounded-lg hover:shadow-lg hover:shadow-orange-200 transition-all uppercase tracking-widest">
                        Đăng nhập
                     </Link>
                  )}
               </div>
            </div>
         </div>

         {/* Sub Nav Row - Mỏng hơn */}
         <div className="border-t border-gray-100 bg-gray-50/50 hidden md:block">
            <div className="container mx-auto px-4">
               <nav className="flex items-center justify-center h-9">
                  <ul className="flex items-center gap-12 h-full m-0 p-0 list-none">
                     <li>
                        <Link to="/" className="text-[11px] font-bold text-gray-500 hover:text-primary uppercase tracking-widest transition-all">Trang chủ</Link>
                     </li>
                     <li>
                        <Link to="/build-pc" className="flex items-center gap-1.5 text-[11px] font-bold text-gray-500 hover:text-primary uppercase tracking-widest transition-all">
                           <ThunderboltOutlined className="text-[14px]" /> 
                           <span>Xây dựng máy tính</span>
                        </Link>
                     </li>
                     <li>
                        <Link to="/contact" className="text-[11px] font-bold text-gray-500 hover:text-primary uppercase tracking-widest transition-all">Liên hệ</Link>
                     </li>
                     <li>
                        <Link to="/deals" className="text-[11px] font-bold text-gray-500 hover:text-primary uppercase tracking-widest transition-all">Khuyến mãi</Link>
                     </li>
                  </ul>
               </nav>
            </div>
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
