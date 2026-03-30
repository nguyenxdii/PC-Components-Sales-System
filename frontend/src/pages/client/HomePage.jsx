import React, { useState, useEffect } from "react";
import Slider from "react-slick";
import { Link } from "react-router-dom";
import {
  ShoppingCartOutlined,
  LeftOutlined,
  RightOutlined,
  TagOutlined,
  ThunderboltOutlined,
} from "@ant-design/icons";
import { Typography, Tag } from "antd";
import { productService } from "../../services/productService";
import { categoryService } from "../../services/categoryService";
import { bannerService } from "../../services/bannerService";
import { sectionService } from "../../services/sectionService";
import { useCart } from "../../contexts/CartContext";
import CountdownTimer from "../../components/client/CountdownTimer";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";

const { Text } = Typography;

// Custom arrow components
function NextArrow(props) {
  const { onClick } = props;
  return (
    <div
      className="absolute right-4 top-1/2 -translate-y-1/2 z-10 w-12 h-12 bg-white/80 hover:bg-white rounded-full flex items-center justify-center cursor-pointer shadow-lg transition-all"
      onClick={onClick}
    >
      <RightOutlined style={{ fontSize: "20px", color: "#000" }} />
    </div>
  );
}

function PrevArrow(props) {
  const { onClick } = props;
  return (
    <div
      className="absolute left-4 top-1/2 -translate-y-1/2 z-10 w-12 h-12 bg-white/80 hover:bg-white rounded-full flex items-center justify-center cursor-pointer shadow-lg transition-all"
      onClick={onClick}
    >
      <LeftOutlined style={{ fontSize: "20px", color: "#000" }} />
    </div>
  );
}

export default function HomePage() {
  const [banners, setBanners] = useState([]);
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCart();

  const getFullImageUrl = (url) => {
    if (!url) return "/images/cat-placeholder.png";
    if (url.startsWith("http")) return url;
    return url;
  };

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [bannerData, sectionData] = await Promise.all([
          bannerService.getActiveBanners(),
          sectionService.getActiveSections()
        ]);
        setBanners(bannerData);
        setSections(sectionData);
      } catch (error) {
        console.error("Lỗi khi tải dữ liệu trang chủ:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const sliderSettings = {
    dots: true,
    infinite: true,
    speed: 500,
    slidesToShow: 1,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3000,
    nextArrow: <NextArrow />,
    prevArrow: <PrevArrow />,
  };

  const sectionSliderSettings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 5,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 4000,
    nextArrow: <NextArrow />,
    prevArrow: <PrevArrow />,
    responsive: [
      { breakpoint: 1024, settings: { slidesToShow: 3.2, arrows: false } },
      { breakpoint: 640, settings: { slidesToShow: 2.1, arrows: false } },
    ],
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(price || 0);
  };

  return (
    <div className="min-h-screen bg-gray-50/50">
      {/* Hero Banner Section */}
      {(!loading && banners.length > 0) && (
        <div className="container mx-auto px-4 py-4 md:py-6 animate-fadeIn">
          <div className="rounded-2xl overflow-hidden shadow-2xl relative border border-gray-100 bg-white">
              <Slider {...sliderSettings}>
                {banners.map((banner) => (
                  <div key={banner.id}>
                    <Link to={banner.link || "#"}>
                      <div className="aspect-[21/9] md:aspect-[21/7] relative bg-gray-50">
                        <img
                          src={getFullImageUrl(banner.imageUrl)}
                          alt={banner.name}
                          className="w-full h-full object-cover transition-opacity duration-300"
                          onLoad={(e) => e.target.style.opacity = 1}
                          style={{ opacity: 0 }}
                          onError={(e) => e.target.src = "/images/cat-placeholder.png"}
                        />
                        <div className="absolute inset-0 bg-gradient-to-r from-black/10 to-transparent pointer-events-none"></div>
                      </div>
                    </Link>
                  </div>
                ))}
              </Slider>
          </div>
        </div>
      )}

      {/* Dynamic Sections Loop */}
      <div className="container mx-auto px-4 pb-20 space-y-12 mt-6">
        {sections.map((section) => (
          <div key={section.id} className="animate-fadeIn">
            <div className={`rounded-t-2xl px-6 py-4 flex flex-col md:flex-row md:items-center justify-between gap-4 ${
                section.type === "FLASH_SALE" 
                ? "bg-gradient-to-r from-red-600 to-rose-500 shadow-lg shadow-red-200" 
                : "bg-white border-b border-gray-100 shadow-sm"
            }`}>
              <div className="flex items-center gap-4">
                <h2 className={`${
                    section.type === "FLASH_SALE" ? "text-white" : "text-gray-900"
                } text-xl font-black italic uppercase tracking-tighter flex items-center gap-2 leading-none uppercase`}>
                  {section.type === "FLASH_SALE" && <ThunderboltOutlined className="text-yellow-300 mr-1" />}
                  {section.name}
                </h2>
                
                {section.type === "FLASH_SALE" && section.endAt && (
                   <CountdownTimer targetDate={section.endAt} />
                )}
              </div>
              
              <Link to={`/products?category=${section.slug}`} className={`${
                  section.type === "FLASH_SALE" ? "text-white/90 hover:text-white" : "text-primary hover:opacity-80"
              } text-xs font-bold uppercase tracking-widest flex items-center gap-1.5 transition-all`}>
                XEM TẤT CẢ <RightOutlined style={{ fontSize: '10px' }} />
              </Link>
            </div>

            <div className={`p-6 rounded-b-2xl shadow-sm border border-t-0 ${
                section.type === "FLASH_SALE" ? "bg-white border-red-100" : "bg-white border-gray-50"
            }`}>
              {section.products && section.products.length > 0 ? (
                <Slider {...sectionSliderSettings}>
                  {section.products.map((item) => (
                    <div key={item.productId} className="px-2 h-full py-4">
                       <Link to={`/product/${item.slug}`} className="product-card-old bg-white rounded-xl p-4 group flex flex-col h-[420px] relative overflow-hidden block">
                          {item.discountPercent && (
                             <div className="absolute top-2 left-2 z-10 bg-red-500 text-white text-[10px] font-black px-2 py-1 rounded shadow-sm uppercase tracking-tighter">
                                Giảm {item.discountPercent}%
                             </div>
                          )}
                          
                          <div className="relative aspect-square overflow-hidden bg-white mb-4 rounded-lg flex items-center justify-center p-2">
                             <img
                                src={getFullImageUrl(item.mainImageUrl)}
                                alt={item.name}
                                className="w-full h-full object-contain transition-transform duration-500 group-hover:scale-110"
                                onError={(e) => e.target.src = "/images/cat-placeholder.png"}
                             />
                          </div>

                          <div className="flex-1 flex flex-col">
                             <div className="flex items-center gap-1.5 mb-1.5 opacity-50">
                                <TagOutlined className="text-[10px]" />
                                <Text className="text-[9px] font-extrabold uppercase tracking-widest">Premium Collection</Text>
                             </div>

                             <div className="mb-3 block flex-1">
                                <h3 className="product-title text-xs font-bold text-gray-800 line-clamp-2 min-h-[32px] transition-colors duration-300 leading-tight uppercase">
                                  {item.name}
                                </h3>
                             </div>

                             <div className="mt-auto">
                                <div className="flex flex-col mb-4">
                                   <Text className="text-[#f57224] font-black text-base tracking-tight leading-none mb-1">
                                      {formatPrice(item.salePrice || item.originalPrice)}
                                   </Text>
                                   {(item.salePrice && item.salePrice < item.originalPrice) && (
                                      <Text delete className="text-[10px] text-gray-400 font-bold">
                                         {formatPrice(item.originalPrice)}
                                      </Text>
                                   )}
                                </div>

                                <button 
                                  onClick={(e) => {
                                      e.preventDefault();
                                      e.stopPropagation();
                                      addToCart(item.productId, 1);
                                  }}
                                  className="btn-cart-slide"
                                >
                                   <ShoppingCartOutlined /> Thêm giỏ hàng
                                </button>
                             </div>
                          </div>
                       </Link>
                    </div>
                  ))}
                </Slider>
              ) : (
                <div className="py-12 flex items-center justify-center">
                    <Text type="secondary" italic>Chưa có sản phẩm trong khung này.</Text>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
