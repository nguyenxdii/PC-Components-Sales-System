import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { Typography, Breadcrumb, Skeleton, Empty, Pagination, Collapse } from "antd";
import { 
  FilterOutlined,
  ArrowRightOutlined
} from "@ant-design/icons";
import { productService } from "../../services/productService";
import { categoryService } from "../../services/categoryService";
import { brandService } from "../../services/brandService";
import { sectionService } from "../../services/sectionService";
import { useCart } from "../../contexts/CartContext";
import PremiumProductCard from "../../components/client/PremiumProductCard";

const { Title, Text } = Typography;

export default function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const categoryId = searchParams.get("category");
  const sectionId = searchParams.get("sectionId");
  const brandId = searchParams.get("brand");
  const priceParam = searchParams.get("price");
  const sortParam = searchParams.get("sort") || "newest";
  const pageParam = parseInt(searchParams.get("page") || "0");
  const keywordParam = searchParams.get("keyword");

  const [products, setProducts] = useState([]);
  const [pagination, setPagination] = useState({ totalPages: 0, totalElements: 0 });
  const [brands, setBrands] = useState([]);
  const [currentCategory, setCurrentCategory] = useState(null);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCart();
 
  const formatPrice = (price) => {
    return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(price || 0);
  };

  useEffect(() => {
    const fetchBrands = async () => {
      try {
        const allBrands = await brandService.getActiveBrands();
        setBrands(allBrands || []);
      } catch (error) {
        console.error("Lỗi khi tải brands:", error);
      }
    };
    fetchBrands();
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const params = { 
          category: categoryId,
          sectionId: sectionId,
          brand: brandId,
          priceRange: priceParam,
          sort: sortParam,
          page: pageParam,
          keyword: keywordParam
        };
        // Xóa params undefined
        Object.keys(params).forEach(k => { if (!params[k]) delete params[k]; });
        
        const response = await productService.getAllProducts(params);
        setProducts(response.content || []);
        setPagination({
          totalPages: response.totalPages || 0,
          totalElements: response.totalElements || 0
        });

        if (categoryId) {
          try {
            const allCats = await categoryService.getAllCategories();
            const current = allCats.find(c => String(c.id) === categoryId);
            setCurrentCategory(current);
          } catch (err) {
            setCurrentCategory(null);
          }
        } else if (sectionId) {
          try {
            const allSections = await sectionService.getAllSections();
            const current = allSections.find(s => String(s.id) === sectionId);
            if (current) {
              setCurrentCategory({ name: current.name });
            }
          } catch (err) {
            setCurrentCategory(null);
          }
        } else {
          setCurrentCategory(null);
        }
      } catch (error) {
        console.error("Lỗi khi tải sản phẩm:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
    window.scrollTo(0, 0);
  }, [categoryId, sectionId, brandId, priceParam, sortParam, pageParam, keywordParam]);

  const updateFilters = (key, value) => {
    const newParams = new URLSearchParams(searchParams);
    if (value) newParams.set(key, value);
    else newParams.delete(key);
    newParams.set("page", "0");
    setSearchParams(newParams);
  };

  const handlePageChange = (page) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set("page", String(page - 1));
    setSearchParams(newParams);
  };

  const priceRanges = [
    { label: "Tất cả mức giá", value: "" },
    { label: "Dưới 5 triệu", value: "0-5000000" },
    { label: "5tr - 15tr", value: "5000000-15000000" },
    { label: "15tr - 30tr", value: "15000000-30000000" },
    { label: "Trên 30 triệu", value: "30000000-999000000" }
  ];

  const breadcrumbItems = [
    { title: <Link to="/" className="text-gray-400 hover:text-primary transition-colors no-underline uppercase text-[10px] font-medium tracking-widest">Trang chủ</Link> },
    { title: <Link to="/products" className="text-gray-400 hover:text-primary transition-colors no-underline uppercase text-[10px] font-medium tracking-widest">Sản phẩm</Link> },
    ...(currentCategory ? [{ title: <span className="text-primary uppercase text-[10px] font-semibold tracking-widest">{currentCategory.name}</span> }] : [])
  ];

  const filterItems = [
    {
      key: 'brand',
      label: <span className="text-[11px] font-bold uppercase tracking-widest text-slate-800">Thương hiệu</span>,
      children: (
        <div className="flex flex-col gap-2 max-h-60 overflow-y-auto pr-2 custom-scrollbar">
          <div 
            className={`px-3 py-2 rounded-lg text-xs cursor-pointer transition-all ${!brandId ? 'bg-primary text-white font-semibold' : 'bg-gray-50 text-slate-600 hover:bg-gray-100'}`}
            onClick={() => updateFilters("brand", "")}
          >
            Tất cả
          </div>
          {brands.map(b => (
            <div 
              key={b.id}
              className={`px-3 py-2 rounded-lg text-xs cursor-pointer transition-all ${brandId === String(b.id) ? 'bg-primary text-white font-semibold' : 'bg-gray-50 text-slate-600 hover:bg-gray-100'}`}
              onClick={() => updateFilters("brand", String(b.id))}
            >
              {b.name}
            </div>
          ))}
        </div>
      )
    },
    {
      key: 'price',
      label: <span className="text-[11px] font-bold uppercase tracking-widest text-slate-800">Mức giá</span>,
      children: (
        <div className="flex flex-col gap-2">
          {priceRanges.map(range => (
            <div 
              key={range.value}
              className={`px-3 py-2 rounded-lg text-xs cursor-pointer transition-all ${priceParam === range.value || (!priceParam && range.value === "") ? 'bg-orange-50 text-primary font-semibold border border-orange-100' : 'bg-gray-50 text-slate-600 hover:bg-gray-100'}`}
              onClick={() => updateFilters("price", range.value)}
            >
              {range.label}
            </div>
          ))}
        </div>
      )
    }
  ];

  return (
    <div className="min-h-screen pb-20 bg-[#f8fafc]">
      <div className="bg-white border-b border-gray-50">
        <div className="container mx-auto px-4 py-8">
          <Breadcrumb className="mb-4" items={breadcrumbItems} />
          
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
              <h1 className="text-2xl font-black text-slate-900 uppercase italic m-0">
                {keywordParam ? `Kết quả: "${keywordParam}"` : (currentCategory ? currentCategory.name : "Tất cả linh kiện")}
              </h1>
              <p className="text-[11px] font-medium text-gray-400 mt-1 uppercase tracking-widest">
                Tìm thấy {pagination.totalElements} sản phẩm phù hợp
              </p>
            </div>
            
            <div className="flex items-center gap-3">
              <span className="text-xs text-gray-400">Sắp xếp:</span>
              <select 
                value={sortParam}
                onChange={(e) => updateFilters("sort", e.target.value)}
                className="bg-white text-xs text-slate-700 outline-none cursor-pointer px-4 py-2 border border-gray-200 rounded-lg focus:border-primary transition-all"
              >
                <option value="newest">Mới nhất</option>
                <option value="price_asc">Giá tăng dần</option>
                <option value="price_desc">Giá giảm dần</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="flex flex-col md:flex-row gap-10">
          
          {/* Sidebar */}
          <aside className="w-full md:w-56 flex-shrink-0">
            <div className="bg-white rounded-2xl p-6 border border-gray-100 sticky top-28">
              <div className="flex items-center gap-2 mb-6 pb-4 border-b border-gray-50">
                <FilterOutlined className="text-primary text-sm" />
                <span className="text-xs font-bold uppercase tracking-widest text-slate-800">Bộ lọc</span>
              </div>
              <Collapse ghost items={filterItems} defaultActiveKey={['brand', 'price']} expandIconPosition="end" />
            </div>
          </aside>

          {/* Grid */}
          <div className="flex-1">
            {loading ? (
              <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {[...Array(8)].map((_, i) => (
                  <div key={i} className="bg-white p-5 rounded-2xl border border-gray-100">
                    <Skeleton active paragraph={{ rows: 3 }} />
                  </div>
                ))}
              </div>
            ) : products.length > 0 ? (
              <>
                <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                  {products.map((product) => (
                    <PremiumProductCard key={product.id} product={product} />
                  ))}
                </div>

                <div className="mt-12 flex justify-center">
                  <Pagination 
                    current={pageParam + 1}
                    total={pagination.totalElements}
                    pageSize={21}
                    onChange={handlePageChange}
                    showSizeChanger={false}
                  />
                </div>
              </>
            ) : (
              <div className="bg-white py-24 rounded-2xl border border-dashed border-gray-200 flex justify-center text-center px-6">
                <Empty description={<span className="text-gray-400 text-sm">Không tìm thấy sản phẩm nào phù hợp</span>} />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
