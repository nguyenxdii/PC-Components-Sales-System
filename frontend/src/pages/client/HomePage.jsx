import React, { useState, useEffect } from "react";
import Slider from "react-slick";
import { Link } from "react-router-dom";
import {
  LeftOutlined,
  RightOutlined,
} from "@ant-design/icons";
import { bannerService } from "../../services/bannerService";
import { sectionService } from "../../services/sectionService";
import FlashSaleSection from "../../components/client/FlashSaleSection";
import NewArrivalSection from "../../components/client/NewArrivalSection";
import StandardSection from "../../components/client/StandardSection";
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
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);

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
    autoplaySpeed: 3000,
    nextArrow: <NextArrow />,
    prevArrow: <PrevArrow />,
    responsive: [
      { breakpoint: 1024, settings: { slidesToShow: 3.2, arrows: false } },
      { breakpoint: 640, settings: { slidesToShow: 2.1, arrows: false } },
    ],
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
                      <div className="w-full relative bg-gray-50 flex items-center justify-center overflow-hidden">
                        <img
                          src={getFullImageUrl(banner.imageUrl)}
                          alt={banner.name}
                          className="w-full h-auto max-h-[450px] object-contain transition-opacity duration-300"
                          onLoad={(e) => {
                            e.target.style.opacity = 1;
                          }}
                          style={{ opacity: 0 }}
                          onError={(e) => (e.target.src = "/images/cat-placeholder.png")}
                        />
                        <div className="absolute inset-0 bg-gradient-to-r from-black/5 to-transparent pointer-events-none"></div>
                      </div>
                    </Link>
                  </div>
                ))}
              </Slider>
          </div>
        </div>
      )}

      {/* Dynamic Sections Loop */}
      <div className="container mx-auto px-4 pb-20 space-y-16 mt-6">
        {sections.map((section) => {
          if (section.type === "FLASH_SALE") {
            return (
              <FlashSaleSection 
                key={section.id} 
                section={section} 
                sliderSettings={sectionSliderSettings} 
              />
            );
          } else if (section.slug === "moi-nhat" || section.name.toLowerCase().includes("mới")) {
            return (
              <NewArrivalSection 
                key={section.id} 
                section={section} 
                sliderSettings={sectionSliderSettings} 
              />
            );
          } else {
            return (
              <StandardSection 
                key={section.id} 
                section={section} 
                sliderSettings={sectionSliderSettings} 
              />
            );
          }
        })}
      </div>
    </div>
  );
}
