import React from "react";
import { Card, Button, Typography } from "antd";
import { ShoppingCartOutlined, ArrowRightOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";
import { useCart } from "../../contexts/CartContext";

const { Text, Title } = Typography;

const PremiumProductCard = ({ product }) => {
  const { addToCart } = useCart();
  if (!product) return null;

  const formatPrice = (price) =>
    new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(price);

  const getFullImageUrl = (url) => {
    if (!url) return "/images/cat-placeholder.png";
    const cleanUrl = url.replace(/\\/g, "/");
    if (cleanUrl.startsWith("http")) return cleanUrl;
    return cleanUrl;
  };

  // Tính toán sản phẩm mới (trong vòng 3 ngày)
  const isNew = (() => {
    if (!product.createdAt) return false;
    const createdDate = new Date(product.createdAt);
    const now = new Date();
    const diffTime = Math.abs(now - createdDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 3;
  })();

  // Tính toán giảm giá linh hoạt cho cả Product entity và SectionProduct DTO
  const originalPrice = product.originalPrice || product.price;
  const salePrice = product.salePrice;
  const hasDiscount = salePrice && salePrice < originalPrice;

  const discount = product.discountPercent || 
    (hasDiscount ? Math.round(((originalPrice - salePrice) / originalPrice) * 100) : 0);

  const finalPrice = hasDiscount ? salePrice : originalPrice;
  const displayOldPrice = hasDiscount ? originalPrice : null;

  return (
    <div className="group h-full">
      <Card
        hoverable
        className="h-full border-none shadow-[0_4px_20px_rgb(0,0,0,0.02)] hover:shadow-[0_20px_40px_rgb(0,0,0,0.08)] transition-all duration-500 rounded-[32px] overflow-hidden"
        cover={
          <div className="relative aspect-[4/5] overflow-hidden bg-[#fafafa] flex items-center justify-center group/img">
            <Link
              to={`/product/${product.slug}`}
              className="block w-full h-full"
            >
              <img
                alt={product.name}
                src={getFullImageUrl(product.mainImageUrl)}
                className="w-full h-full object-contain p-4 group-hover/img:scale-110 transition-transform duration-700"
              />
            </Link>

            {/* Badges - Top Right */}
            <div className="absolute top-4 right-4 flex flex-col items-end gap-2 z-20">
              {discount > 0 && (
                <div className="bg-red-600 text-white text-[11px] font-bold px-2.5 py-1 rounded-bl-2xl rounded-tr-lg shadow-lg shadow-red-500/20 uppercase tracking-tight flex items-center gap-1 animate-pulse">
                  <span className="text-[13px]">-{discount}%</span>
                </div>
              )}
              {isNew && (
                <div className="bg-[#00b5ad] text-white text-[10px] font-bold px-3 py-1 rounded-lg shadow-lg shadow-teal-500/20 uppercase tracking-tight">
                  NEW
                </div>
              )}
              {product.isHot && (
                <div className="bg-black text-white text-[10px] font-bold px-3 py-1 rounded-lg shadow-lg shadow-black/20 uppercase tracking-tighter">
                  HOT
                </div>
              )}
            </div>

            {/* Quick Actions */}
            <div className="absolute -bottom-16 group-hover/img:bottom-6 left-1/2 -translate-x-1/2 transition-all duration-500 z-10">
              <Button
                shape="circle"
                type="primary"
                icon={<ShoppingCartOutlined style={{ fontSize: "20px" }} />}
                className="!bg-orange-500 border-none shadow-2xl hover:scale-125 transition-all w-14 h-14 flex items-center justify-center p-0"
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  addToCart(product.productId || product.id, 1);
                }}
              />
            </div>
          </div>
        }
        styles={{ body: { padding: "20px", paddingTop: "12px" } }}
      >
        <div className="flex flex-col gap-2.5">
          {/* Category/Brand */}
          <Text className="text-[10px] font-semibold text-orange-500 uppercase tracking-widest block opacity-80">
            {product.brand?.name || "PREMIUM COLLECTION"}
          </Text>

          <Link to={`/product/${product.slug}`}>
            <Title
              level={5}
              className="!m-0 !text-sm !font-medium text-slate-800 line-clamp-2 hover:text-orange-500 transition-colors h-10 leading-relaxed uppercase"
            >
              {product.name}
            </Title>
          </Link>

          <div className="flex items-end justify-between mt-1 pt-3 border-t border-gray-50/50">
            <div className="flex flex-col gap-0.5">
              {displayOldPrice && (
                <Text
                  delete
                  className="text-[11px] text-gray-400 font-medium leading-none opacity-70"
                >
                  {formatPrice(displayOldPrice)}
                </Text>
              )}
              <Text className="text-base font-bold text-slate-900 leading-none">
                {formatPrice(finalPrice)}
              </Text>
            </div>

            <Link to={`/product/${product.slug}`}>
              <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center group-hover:bg-orange-500 group-hover:text-white transition-all text-gray-400">
                <ArrowRightOutlined className="text-[12px]" />
              </div>
            </Link>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default PremiumProductCard;
