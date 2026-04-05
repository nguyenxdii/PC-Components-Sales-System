import React, { useState, useEffect } from 'react';
import { Modal, Input, List, Button, Tag, Typography, Spin, Empty } from 'antd';
import { SearchOutlined, ShoppingCartOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { productService } from '../../../services/productService';

const { Text, Title } = Typography;

const ComponentSelectorModal = ({ visible, onCancel, onSelect, slotName, categorySlug, currentSocket, currentRam }) => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    if (visible) {
      fetchProducts();
    }
  }, [visible, categorySlug, searchTerm, currentSocket, currentRam]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const isCompatibilitySensitive = ['cpu-bo-vi-xu-ly', 'mainboard-bo-mach-chu', 'ram-bo-nho-trong'].includes(categorySlug);
      
      const params = {
        categorySlug: categorySlug,
        searchTerm: searchTerm,
        socketType: (isCompatibilitySensitive && categorySlug !== 'ram-bo-nho-trong') ? currentSocket : undefined,
        ramType: isCompatibilitySensitive ? currentRam : undefined,
        page: 0,
        sort: 'newest'
      };
      
      const response = await productService.getAllProducts(params);
      setProducts(response.content || []);
    } catch (error) {
      console.error('Error fetching products for builder:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price || 0);
  };

  return (
    <Modal
      title={<Title level={4}>Chọn {slotName}</Title>}
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={800}
      className="premium-modal"
    >
      <div className="mb-6">
        <Input
          placeholder="Tìm kiếm linh kiện..."
          prefix={<SearchOutlined className="text-gray-400" />}
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className="rounded-xl h-12 border-gray-100 bg-gray-50 hover:bg-white focus:bg-white transition-all shadow-sm"
        />
      </div>

      <div className="max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
        {loading ? (
          <div className="flex justify-center py-20">
            <Spin size="large" />
          </div>
        ) : products.length > 0 ? (
          <List
            itemLayout="horizontal"
            dataSource={products}
            renderItem={item => (
              <List.Item
                className="hover:bg-gray-50 p-4 rounded-2xl transition-all mb-3 border border-gray-100 group"
                actions={[
                  <Button 
                    type="primary" 
                    className="btn-premium-premium !h-10 !px-6"
                    onClick={() => onSelect(item)}
                  >
                    Chọn
                  </Button>
                ]}
              >
                <List.Item.Meta
                  avatar={
                    <div className="w-16 h-16 rounded-xl bg-white p-2 border border-gray-100 flex items-center justify-center overflow-hidden">
                      <img 
                        src={item.mainImageUrl || "/images/cat-placeholder.png"} 
                        alt={item.name} 
                        className="w-full h-full object-contain group-hover:scale-110 transition-transform" 
                      />
                    </div>
                  }
                  title={
                    <div className="flex flex-col">
                      <Text className="text-[10px] font-black uppercase text-primary tracking-widest mb-1">
                        {item.brand?.name || "Premium"}
                      </Text>
                      <Text strong className="text-sm line-clamp-1 group-hover:text-primary transition-colors">
                        {item.name}
                      </Text>
                    </div>
                  }
                  description={
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-3">
                        <Text className="text-secondary font-black text-base">
                          {formatPrice(item.salePrice || item.price)}
                        </Text>
                        {item.salePrice && item.salePrice < item.price && (
                          <Text delete className="text-gray-400 text-xs">
                            {formatPrice(item.price)}
                          </Text>
                        )}
                      </div>
                      <div className="flex gap-2">
                         {item.socketType && <Tag color="orange" className="rounded-md border-none">{item.socketType}</Tag>}
                         {item.ramType && <Tag color="green" className="rounded-md border-none">{item.ramType}</Tag>}
                         <Tag className="rounded-md border-none bg-gray-100 text-gray-500">BH {item.warrantyPeriod || 36} tháng</Tag>
                      </div>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        ) : (
          <Empty description="Không tìm thấy linh kiện phù hợp với cấu hình hiện tại." className="py-10" />
        )}
      </div>
    </Modal>
  );
};

export default ComponentSelectorModal;
