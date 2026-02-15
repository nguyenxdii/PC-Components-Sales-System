import Slider from "react-slick";
import {
  ShoppingCartOutlined,
  LeftOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { products, categories } from "../../data/dummyData";
import { useState, useEffect } from "react";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";

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

  // Load banners từ thư mục public/images/banners
  useEffect(() => {
    // danh sách banner
    const bannerFiles = ["banner 1.png", "banner 2.jpg", "banner 3.png"];

    const loadedBanners = bannerFiles.map((file, index) => ({
      id: index + 1,
      image: `/images/banners/${file}`,
    }));

    setBanners(loadedBanners);
  }, []);

  // Settings cho slider với custom arrows
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

  const flashSaleSettings = {
    dots: false,
    infinite: true,
    speed: 500,
    slidesToShow: 5,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 2000,
    responsive: [
      {
        breakpoint: 1024,
        settings: { slidesToShow: 3 },
      },
      {
        breakpoint: 640,
        settings: { slidesToShow: 2 },
      },
    ],
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(price);
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <div className="container mx-auto px-4 py-6">
        <div className="grid grid-cols-12 gap-4">
          {/* Category Menu */}
          <div className="col-span-3">
            <div className="bg-white rounded-lg shadow-sm p-4">
              <h3 className="font-bold text-lg mb-4 text-gray-800">
                DANH MỤC SẢN PHẨM
              </h3>
              <ul className="space-y-2">
                {categories.map((cat) => (
                  <li key={cat.id}>
                    <a
                      href="#"
                      className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-gray-100 transition-colors"
                    >
                      <span className="text-xl">{cat.icon}</span>
                      <span className="text-sm text-gray-700">{cat.name}</span>
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Banner Slider */}
          <div className="col-span-9">
            <div className="rounded-lg overflow-hidden shadow-lg relative">
              {banners.length > 0 && (
                <Slider {...sliderSettings}>
                  {banners.map((banner) => (
                    <div key={banner.id}>
                      <div className="h-96 relative">
                        <img
                          src={banner.image}
                          alt={`Banner ${banner.id}`}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    </div>
                  ))}
                </Slider>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Flash Sale Section */}
      <div className="bg-white py-8 my-6">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-3xl font-bold text-red-600">⚡ FLASH SALE</h2>
            <div className="text-lg text-gray-600">Kết thúc sau: 02:45:30</div>
          </div>

          <Slider {...flashSaleSettings}>
            {products.slice(0, 8).map((product) => (
              <div key={product.id} className="px-2">
                <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-lg transition-shadow">
                  <img
                    src={product.image}
                    alt={product.name}
                    className="w-full h-48 object-cover rounded-md mb-3"
                  />
                  <h3 className="text-sm text-gray-800 line-clamp-2 h-10 mb-2">
                    {product.name}
                  </h3>
                  <div className="space-y-1">
                    <div className="text-gray-400 line-through text-sm">
                      {formatPrice(product.oldPrice)}
                    </div>
                    <div className="text-red-600 font-bold text-xl">
                      {formatPrice(product.price)}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </Slider>
        </div>
      </div>

      {/* Product Grid */}
      <div className="container mx-auto px-4 py-8">
        <h2 className="text-2xl font-bold mb-6 text-gray-800">
          SẢN PHẨM NỔI BẬT
        </h2>

        <div className="grid grid-cols-5 gap-4">
          {products.map((product) => (
            <div
              key={product.id}
              className="bg-white rounded-lg shadow-sm hover:shadow-xl transition-shadow p-4 group"
            >
              <div className="relative mb-4">
                <img
                  src={product.image}
                  alt={product.name}
                  className="w-full h-48 object-cover rounded-md"
                />
                {/* Discount badge */}
                <div className="absolute top-2 right-2 bg-red-500 text-white px-2 py-1 rounded-md text-xs font-bold">
                  -
                  {Math.round(
                    ((product.oldPrice - product.price) / product.oldPrice) *
                      100,
                  )}
                  %
                </div>
              </div>

              <h3 className="text-sm text-gray-800 line-clamp-2 h-10 mb-3">
                {product.name}
              </h3>

              <div className="mb-3">
                <div className="text-gray-400 line-through text-sm">
                  {formatPrice(product.oldPrice)}
                </div>
                <div className="text-red-600 font-bold text-lg">
                  {formatPrice(product.price)}
                </div>
              </div>

              <button className="w-full bg-primary text-white py-2 rounded-md hover:bg-red-600 transition-colors flex items-center justify-center gap-2 group-hover:scale-105 transform transition-transform">
                <ShoppingCartOutlined />
                <span>Thêm vào giỏ</span>
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
