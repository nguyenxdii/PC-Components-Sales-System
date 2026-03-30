import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { Typography, Breadcrumb, Skeleton, Empty, Tag, Button, Pagination } from "antd";
import { 
  HomeOutlined, 
  ShoppingCartOutlined,
  TagOutlined,
  ThunderboltOutlined,
  FilterOutlined,
  RightOutlined
} from "@ant-design/icons";
import { productService } from "../../services/productService";
import { categoryService } from "../../services/categoryService";
import { useCart } from "../../contexts/CartContext";

const { Title, Text } = Typography;

export default function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const categoryId = searchParams.get("category");
  const brandId = searchParams.get("brand");
  const sortParam = searchParams.get("sort") || "newest";
  const pageParam = parseInt(searchParams.get("page") || "0");

  const [products, setProducts] = useState([]);
  const [pagination, setPagination] = useState({ totalPages: 0, totalElements: 0 });
  const [categories, setCategories] = useState([]);
  const [currentCategory, setCurrentCategory] = useState(null);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCart();
 
  const getFullImageUrl = (url) => {
    if (!url) return "/images/cat-placeholder.png";
    if (url.startsWith("http")) return url;
    return url;
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(price || 0);
  };

  useEffect(() => {
    const fetchInitData = async () => {
        try {
            const catTree = await categoryService.getCategoryTree();
            setCategories(catTree);
        } catch (error) {
            console.error("Lỗi khi tải danh mục:", error);
        }
    };
    fetchInitData();
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const params = { 
          category: categoryId,
          brand: brandId,
          sort: sortParam,
          page: pageParam
        };
        const response = await productService.getAllProducts(params);
        setProducts(response.content || []);
        setPagination({
            totalPages: response.totalPages || 0,
            totalElements: response.totalElements || 0
        });

        if (categoryId) {
          // Tìm category hiện tại từ cây hoặc fetch chi tiết (tạm thời tìm phẳng)
          const allCats = await categoryService.getAllCategories();
          const current = allCats.find(c => String(c.id) === categoryId);
          setCurrentCategory(current);
        } else {
          setCurrentCategory(null);
        }
      } catch (error) {
        console.error("Lỗi khi tải danh sách sản phẩm:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
    window.scrollTo(0, 0);
  }, [categoryId, brandId, sortParam, pageParam]);

  const handleSortChange = (e) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set("sort", e.target.value);
    newParams.set("page", "0"); // Reset về trang đầu
    setSearchParams(newParams);
  };

  const handlePageChange = (page) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set("page", String(page - 1));
    setSearchParams(newParams);
  };

  const handleCategorySelect = (id) => {
    const newParams = new URLSearchParams(searchParams);
    if (id) newParams.set("category", id);
    else newParams.delete("category");
    newParams.set("page", "0");
    setSearchParams(newParams);
  };

  return (
    <div className="min-h-screen bg-gray-50/30 pb-20">
      {/* Page Header / Breadcrumb - Harmonized */}
      <div className="bg-white border-b border-gray-100 shadow-sm">
        <div className="container mx-auto px-4 py-8">
          <Breadcrumb 
            className="mb-4 breadcrumb-premium"
            items={[
              { title: <Link to="/"><HomeOutlined className="mr-1" /> Trang chủ</Link> },
              { title: "Sản phẩm" },
              currentCategory ? { title: currentCategory.name } : null
            ].filter(Boolean)}
          />
          
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
              <Title level={1} className="product-list-title !mb-2">
                {currentCategory ? currentCategory.name : "Tất cả sản phẩm"}
              </Title>
              <div className="product-list-count flex items-center gap-2">
                <ThunderboltOutlined className="text-secondary" />
                <span>Tìm thấy {pagination.totalElements} sản phẩm công nghệ chất lượng</span>
              </div>
            </div>
            
            <div className="flex items-center gap-3 bg-white p-1 rounded-xl">
                <span className="text-[10px] font-black uppercase text-gray-400 tracking-widest ml-2">Sắp xếp:</span>
                <select 
                    value={sortParam}
                    onChange={handleSortChange}
                    className="bg-gray-50 text-[11px] font-bold text-gray-700 outline-none cursor-pointer px-4 h-10 border border-gray-100 rounded-lg hover:bg-gray-100 transition-colors uppercase tracking-widest"
                >
                    <option value="newest">Mới nhất</option>
                    <option value="price_asc">Giá: Thấp đến Cao</option>
                    <option value="price_desc">Giá: Cao đến Thấp</option>
                </select>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content - Full Width */}
      <div className="container mx-auto px-4 py-10">
        <div className="flex flex-col gap-10">
          {/* Product Grid Area */}
          <div className="flex-1">
            {loading ? (
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
                {[...Array(10)].map((_, i) => (
                  <div key={i} className="bg-white p-5 rounded-2xl border border-gray-100 h-[420px]">
                    <Skeleton.Image className="!w-full !h-48 mb-6 rounded-xl" active />
                    <Skeleton active paragraph={{ rows: 3 }} />
                  </div>
                ))}
              </div>
            ) : products.length > 0 ? (
              <>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6 animate-fadeIn transition-all">
                    {products.map((product) => (
                    <div
                        key={product.id}
                        className="bg-white rounded-2xl p-5 hover:shadow-[0_20px_50px_rgba(0,0,0,0.1)] transition-all duration-500 transform hover:-translate-y-2 group border border-gray-100 flex flex-col h-[450px] relative overflow-hidden"
                    >
                        {/* Discount Badge */}
                        {product.salePrice && product.salePrice < product.price && (
                            <div className="absolute top-4 left-4 z-10 bg-red-500 text-white text-[10px] font-black px-2.5 py-1.5 rounded-lg shadow-md uppercase tracking-tighter animate-pulse">
                                Giảm {Math.round((1 - product.salePrice / product.price) * 100)}%
                            </div>
                        )}

                        <div className="relative aspect-square overflow-hidden bg-white mb-6 rounded-xl flex items-center justify-center p-4">
                            <Link to={`/product/${product.slug}`} className="w-full h-full">
                                <img
                                    src={getFullImageUrl(product.mainImageUrl)}
                                    alt={product.name}
                                    className="w-full h-full object-contain mix-blend-multiply group-hover:scale-110 transition-transform duration-700"
                                    onError={(e) => { e.target.src = "/images/cat-placeholder.png"; }}
                                />
                            </Link>
                        </div>

                        <div className="flex-1 flex flex-col">
                            <div className="flex items-center gap-2 mb-2 opacity-50">
                                <TagOutlined className="text-[10px] text-primary" />
                                <Text className="text-[9px] font-black uppercase tracking-[0.2em] text-gray-500">
                                    {product.brand?.name || "Premium Series"}
                                </Text>
                            </div>
                            
                            <Link to={`/product/${product.slug}`} className="block mb-4 flex-1">
                                <h3 className="font-bold text-gray-900 line-clamp-2 text-xs leading-relaxed group-hover:text-primary transition-colors duration-300 uppercase italic tracking-tight">
                                    {product.name}
                                </h3>
                            </Link>

                            <div className="mt-auto">
                                <div className="flex flex-col mb-5">
                                    {product.salePrice && product.salePrice < product.price ? (
                                        <>
                                            <span className="text-secondary font-black text-lg tracking-tighter leading-none mb-1">
                                                {formatPrice(product.salePrice)}
                                            </span>
                                            <span className="text-gray-400 text-[11px] line-through font-bold opacity-60">
                                                {formatPrice(product.price)}
                                            </span>
                                        </>
                                    ) : (
                                        <span className="text-secondary font-black text-lg tracking-tighter">
                                            {formatPrice(product.price)}
                                        </span>
                                    )}
                                </div>

                                <button 
                                    onClick={(e) => {
                                        e.preventDefault();
                                        addToCart(product.id, 1);
                                    }}
                                    className="btn-premium-premium"
                                >
                                    <ShoppingCartOutlined className="text-sm" />
                                    <span>Thêm vào giỏ hàng</span>
                                </button>
                            </div>
                        </div>
                    </div>
                    ))}
                </div>

                {/* Pagination Support - Ant Design Style */}
                <div className="mt-16 flex justify-center">
                    <Pagination 
                        current={pageParam + 1}
                        total={pagination.totalElements}
                        pageSize={21}
                        onChange={handlePageChange}
                        showSizeChanger={false}
                        className="premium-pagination"
                    />
                </div>
              </>
            ) : (
              <div className="bg-white py-24 rounded-3xl border border-dashed border-gray-200">
                <Empty 
                    description={
                        <div className="flex flex-col items-center">
                            <Text strong className="text-gray-400 mb-2">Không tìm thấy sản phẩm nào</Text>
                            <Text type="secondary" className="text-xs">Vui lòng chọn danh mục khác hoặc hãng sản xuất khác.</Text>
                        </div>
                    }
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
