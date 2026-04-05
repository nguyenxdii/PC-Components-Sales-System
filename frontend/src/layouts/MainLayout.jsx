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
import { useState, useEffect, useRef } from "react";
import { Dropdown, Badge, Avatar } from "antd";
import { useCart } from "../contexts/CartContext";
import { categoryService } from "../services/categoryService";
import { productService } from "../services/productService";

export default function MainLayout() {
  const { cartCount } = useCart();
  const [user, setUser] = useState(null);
  const [categories, setCategories] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [showSearch, setShowSearch] = useState(false);
  const searchTimer = useRef(null);
  const searchRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const tree = await categoryService.getCategoryTree();
        setCategories(tree || []);
      } catch (error) {
        console.error("Error fetching categories:", error);
      }
    };
    fetchCategories();

    const userData = localStorage.getItem("user");
    if (userData) {
      try { setUser(JSON.parse(userData)); } catch (e) {}
    }

    // Đóng search dropdown khi click ra ngoài
    const handleClickOutside = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) {
        setShowSearch(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Live search với debounce
  const handleSearch = (value) => {
    setSearchQuery(value);
    if (searchTimer.current) clearTimeout(searchTimer.current);
    if (!value.trim()) { setSearchResults([]); setShowSearch(false); return; }
    searchTimer.current = setTimeout(async () => {
      try {
        const res = await productService.getAllProducts({ keyword: value, limit: 6 });
        setSearchResults(res.products || []);
        setShowSearch(true);
      } catch (e) { console.error(e); }
    }, 300);
  };

  const getFullImageUrl = (url) => {
    if (!url) return "/images/cat-placeholder.png";
    const cleanUrl = url.replace(/\\/g, '/');
    if (cleanUrl.startsWith("http")) return cleanUrl;
    // Assume relative path for PC components
    return cleanUrl;
  };

  const formatPrice = (price) => new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price || 0);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
    navigate("/login");
  };

  const userMenuItems = [
    ...(user?.role === 'ADMIN' ? [{ key: 'admin', label: 'Quản trị hệ thống', icon: <AppstoreOutlined />, onClick: () => navigate("/admin") }] : []),
    { key: 'profile', label: 'Thông tin tài khoản', icon: <UserOutlined />, onClick: () => navigate("/profile") },
    { key: 'orders', label: 'Đơn hàng của tôi', icon: <ShoppingOutlined />, onClick: () => navigate("/orders") },
    { key: 'builds', label: 'PC đã build', icon: <ThunderboltOutlined />, onClick: () => navigate("/saved-builds") },
    { type: 'divider' },
    { key: 'logout', label: 'Đăng xuất', icon: <LogoutOutlined />, danger: true, onClick: handleLogout }
  ];

  return (
    <div className="flex flex-col min-h-screen bg-[#f8fafc]">
      <header className="bg-white border-b border-gray-100 sticky top-0 z-[100] shadow-sm">
        <div className="container mx-auto px-4">
          <div className="flex items-center h-16 gap-4 md:gap-8">
            
            {/* Logo */}
            <Link to="/" className="flex-shrink-0">
              <img src="/logo-exeshop.png" alt="Logo" className="h-9 w-auto" />
            </Link>

            {/* NÚT DANH MỤC */}
            <div className="cat-menu-wrapper h-full flex items-center">
              <button className="flex items-center gap-2 btn-primary px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-wider transition-all cursor-pointer border-none shadow-orange-200">
                <MenuOutlined className="text-sm" />
                <span className="hidden lg:inline">Danh mục</span>
              </button>

              {/* DROPDOWN DANH MỤC DỌC */}
              <div className="cat-dropdown">
                <ul className="cat-list">
                  <li className="cat-list-item">
                    <Link to="/products" className="cat-list-link !text-blue-600 font-bold">
                      <span>Tất cả sản phẩm</span>
                    </Link>
                  </li>
                  <div className="h-px bg-gray-100 mx-4 my-1"></div>

                  {Array.isArray(categories) && categories.map((cat) => (
                    <li key={cat.id} className="cat-list-item">
                      <Link to={`/products?category=${cat.id}`} className="cat-list-link">
                        <span>{cat.name}</span>
                        {cat.children && cat.children.length > 0 && (
                          <RightOutlined className="text-[10px] text-gray-300" />
                        )}
                      </Link>

                      {/* SUB-PANEL */}
                      {cat.children && cat.children.length > 0 && (
                        <div className="cat-sub-panel">
                          <div className="grid grid-cols-3 gap-4">
                            {cat.children.map((sub) => (
                              <Link 
                                key={sub.id}
                                to={`/products?category=${sub.id}`}
                                className="cat-sub-card"
                              >
                                <div className="cat-sub-img">
                                  <img 
                                    src={getFullImageUrl(sub.iconUrl || sub.imageUrl)} 
                                    alt={sub.name}
                                    onError={(e) => { e.target.src = "/images/cat-placeholder.png"; }}
                                  />
                                </div>
                                <span className="cat-sub-name">{sub.name}</span>
                              </Link>
                            ))}
                          </div>
                        </div>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            {/* THANH TÌM KIẾM LIVE SEARCH */}
            <div className="flex-1 max-w-md relative hidden md:block" ref={searchRef}>
              <div className="relative">
                <input 
                  type="text" 
                  placeholder="Tìm kiếm linh kiện..." 
                  value={searchQuery}
                  onChange={(e) => handleSearch(e.target.value)}
                  onFocus={() => { if (searchResults.length > 0) setShowSearch(true); }}
                  className="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-2.5 text-sm outline-none focus:bg-white focus:border-primary transition-all font-medium"
                />
                <SearchOutlined className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400" />
              </div>

              {/* SEARCH DROPDOWN */}
              {showSearch && searchResults.length > 0 && (
                <div className="search-dropdown">
                  {searchResults.map((p) => (
                    <Link 
                      key={p.id}
                      to={`/product/${p.slug}`}
                      className="search-item group"
                      onClick={() => { setShowSearch(false); setSearchQuery(""); }}
                    >
                      <img 
                        src={getFullImageUrl(p.mainImageUrl)} 
                        alt={p.name}
                        className="search-item-img transition-transform group-hover:scale-110"
                        onError={(e) => { e.target.src = "/images/cat-placeholder.png"; }}
                      />
                      <div className="flex-1 min-w-0">
                        <p className="text-[13px] text-slate-700 font-bold truncate m-0 group-hover:text-primary transition-colors">{p.name}</p>
                        <p className="text-sm text-red-500 font-black m-0 mt-0.5">{formatPrice(p.salePrice || p.price)}</p>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </div>

            {/* Actions */}
            <div className="flex items-center gap-4 ml-auto">
              <Link to="/cart" className="flex items-center gap-2.5 no-underline group shrink-0">
                <Badge count={cartCount} offset={[0, 0]} size="small" color="#f57224">
                  <div className="w-9 h-9 rounded-full bg-gray-50 hover-bg-primary flex items-center justify-center text-gray-600 transition-all">
                    <ShoppingCartOutlined className="text-lg" />
                  </div>
                </Badge>
                <span className="text-[10px] font-black uppercase tracking-widest text-slate-600 hover-text-primary transition-all hidden sm:inline">Giỏ hàng</span>
              </Link>
              
              {user ? (
                <Dropdown menu={{ items: userMenuItems }} placement="bottomRight" arrow={{ pointAtCenter: true }}>
                  <div className="flex items-center gap-2 cursor-pointer group shrink-0 pl-2">
                    <Avatar 
                      src={user.avatarUrl ? getFullImageUrl(user.avatarUrl) : "/user-default.jpg"} 
                      icon={<UserOutlined />} 
                      className="border-2 border-transparent hover-bg-primary transition-all w-9 h-9 shadow-sm"
                    />
                    <span className="text-[11px] font-bold text-slate-700 hidden lg:inline">{user.fullName}</span>
                  </div>
                </Dropdown>
              ) : (
                <Link to="/login" className="px-5 py-2.5 btn-primary rounded-lg transition-all font-black text-[11px] uppercase tracking-widest shrink-0 border-none no-underline flex items-center">
                  Đăng nhập
                </Link>
              )}
            </div>
          </div>
        </div>

        {/* SUB NAV ROW */}
        <div className="border-t border-gray-50 bg-white hidden md:block">
          <div className="container mx-auto px-4">
            <nav className="flex items-center justify-center h-10">
              <ul className="flex items-center gap-12 h-full m-0 p-0 list-none">
                <li>
                  <Link to="/" className="text-[11px] font-black text-slate-500 hover-text-primary uppercase tracking-[0.1em] transition-all no-underline">Trang chủ</Link>
                </li>
                <li>
                  <Link to="/products" className="text-[11px] font-black text-slate-500 hover-text-primary uppercase tracking-[0.1em] transition-all no-underline">Tất cả sản phẩm</Link>
                </li>
                <li>
                  <Link to="/build-pc" className="flex items-center gap-2 text-[11px] font-black text-slate-500 hover-text-primary uppercase tracking-[0.1em] transition-all no-underline">
                    <ThunderboltOutlined className="text-[14px]" /> 
                    <span>Xây dựng máy tính</span>
                  </Link>
                </li>
                <li>
                  <Link to="/contact" className="text-[11px] font-black text-slate-500 hover-text-primary uppercase tracking-[0.1em] transition-all no-underline">Liên hệ</Link>
                </li>

              </ul>
            </nav>
          </div>
        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="bg-slate-900 text-white pt-16 pb-8 mt-auto">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
            <div className="space-y-4">
              <Link to="/" className="flex items-center gap-2 no-underline">
                <img src="/logo-exeshop.png" alt="Logo" className="h-8 w-auto brightness-0 invert" />
              </Link>
              <p className="text-[12px] text-gray-400 leading-relaxed font-medium">Hệ thống cung cấp linh kiện PC và Gaming Gear chuyên nghiệp. Cam kết mang lại giá trị tốt nhất cho cộng đồng game thủ.</p>
            </div>
            
            <div>
              <h3 className="text-[12px] font-black uppercase tracking-widest mb-6 text-gray-200">Sản phẩm</h3>
              <ul className="list-none p-0 space-y-3 text-[12px] text-gray-400 font-medium">
                <li className="hover:text-primary cursor-pointer transition-colors">CPU - Bộ vi xử lý</li>
                <li className="hover:text-primary cursor-pointer transition-colors">VGA - Card màn hình</li>
                <li className="hover:text-primary cursor-pointer transition-colors">Laptop Gaming</li>
              </ul>
            </div>
            
            <div>
              <h3 className="text-[12px] font-black uppercase tracking-widest mb-6 text-gray-200">Hỗ trợ</h3>
              <ul className="list-none p-0 space-y-3 text-[12px] text-gray-400 font-medium">
                <li><Link to="/contact" className="hover:text-primary transition-colors text-gray-400 no-underline">Trung tâm bảo hành</Link></li>
                <li className="hover:text-primary cursor-pointer transition-colors">Giao hàng & Thanh toán</li>
                <li className="hover:text-primary cursor-pointer transition-colors">Bản đồ cửa hàng</li>
              </ul>
            </div>
            
            <div className="space-y-4">
              <h3 className="text-[12px] font-black uppercase tracking-widest mb-6 text-gray-200">Kết nối</h3>
              <div className="flex gap-4">
                <span className="w-9 h-9 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary transition-all cursor-pointer">FB</span>
                <span className="w-9 h-9 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary transition-all cursor-pointer">IG</span>
                <span className="w-9 h-9 rounded-full bg-slate-800 flex items-center justify-center hover:bg-primary transition-all cursor-pointer">YT</span>
              </div>
            </div>
          </div>
          
          <div className="pt-8 border-t border-slate-800 text-center">
            <p className="text-[10px] text-gray-500 font-bold uppercase tracking-widest">© 2024 PC Components Sales System. Build your dream.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
