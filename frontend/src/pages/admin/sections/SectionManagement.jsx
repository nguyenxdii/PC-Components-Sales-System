import React, { useState, useEffect } from "react";
import { 
  Table, Button, Space, Modal, Form, Input, 
  Select, Switch, DatePicker, message, Typography, Card,
  Popconfirm, Tag, List, Avatar, InputNumber
} from "antd";
import { 
  PlusOutlined, EditOutlined, DeleteOutlined, 
  MenuOutlined, ThunderboltOutlined, OrderedListOutlined,
  SearchOutlined
} from "@ant-design/icons";
import { 
  DndContext, 
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import dayjs from "dayjs";
import { sectionService } from "../../../services/sectionService";
import { productService } from "../../../services/productService";

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const SectionManagement = () => {
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingSection, setEditingSection] = useState(null);
  const [form] = Form.useForm();
  
  // Quản lý sản phẩm trong Section
  const [isProductModalVisible, setIsProductModalVisible] = useState(false);
  const [currentSection, setCurrentSection] = useState(null);
  const [allProducts, setAllProducts] = useState([]);
  const [searchProduct, setSearchProduct] = useState("");

  const isAnyModalOpen = isModalVisible || isProductModalVisible;
 
  // Row thành phần cho Kéo thả
  const SortableRow = ({ children, ...props }) => {
    const rowKey = props['data-row-key'];
    const {
      attributes,
      listeners,
      setNodeRef,
      transform,
      transition,
      isDragging,
    } = useSortable({ id: rowKey });
 
    const style = isAnyModalOpen ? { ...props.style } : {
      ...props.style,
      transform: transform ? CSS.Transform.toString(transform) : undefined,
      transition,
      zIndex: isDragging ? 9999 : 0,
      position: isDragging ? 'relative' : undefined,
      background: isDragging ? '#f5f5f5' : undefined,
    };
 
    return (
      <tr {...props} ref={setNodeRef} style={style} {...attributes}>
        {React.Children.map(children, (child) => {
          if (child.key === 'sort') {
            return React.cloneElement(child, {
              children: (
                <MenuOutlined
                  {...listeners}
                  style={{ cursor: 'grab', color: '#999' }}
                />
              ),
            });
          }
          return child;
        })}
      </tr>
    );
  };

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  useEffect(() => {
    fetchSections();
    fetchInitialProducts();
  }, []);

  const fetchSections = async () => {
    setLoading(true);
    try {
      const data = await sectionService.getAllSections();
      setSections(data.sort((a, b) => a.displayOrder - b.displayOrder));
    } catch (error) {
      message.error("Lỗi khi tải danh sách section");
    } finally {
      setLoading(false);
    }
  };

  const fetchInitialProducts = async () => {
    try {
      // Admin cần xem nhiều sản phẩm hơn mặc định (21)
      const response = await productService.getAllProducts({ size: 1000 });
      setAllProducts(response.content || []);
    } catch (error) {}
  };

  const getFullImageUrl = (url) => {
    if (!url) return "/images/cat-placeholder.png";
    if (url.startsWith("http")) return url;
    return url;
  };

  const handleAddProductToSection = async (productId) => {
    try {
      const updatedSection = await sectionService.addProductToSection(currentSection.id, productId);
      setCurrentSection(updatedSection);
      setSections(prev => prev.map(s => s.id === updatedSection.id ? updatedSection : s));
      message.success("Đã thêm sản phẩm vào khung");
    } catch (error) {
      console.error(error);
      message.error(error.response?.data?.message || "Lỗi khi thêm sản phẩm");
    }
  };

  const handleRemoveProductFromSection = async (productId) => {
    try {
      const updatedSection = await sectionService.removeProductFromSection(currentSection.id, productId);
      setCurrentSection(updatedSection);
      setSections(prev => prev.map(s => s.id === updatedSection.id ? updatedSection : s));
      message.success("Đã gỡ sản phẩm khỏi khung");
    } catch (error) {
      console.error(error);
      message.error(error.response?.data?.message || "Lỗi khi gỡ sản phẩm");
    }
  };

  const onDragEnd = async ({ active, over }) => {
    if (active.id !== over.id) {
      const oldIndex = sections.findIndex(i => i.id === active.id);
      const newIndex = sections.findIndex(i => i.id === over.id);
      const newSections = arrayMove(sections, oldIndex, newIndex);
      setSections(newSections);
      try {
        await sectionService.reorderSections(newSections.map(s => s.id));
        message.success("Đã cập nhật thứ tự hiển thị");
      } catch (error) {
        message.error("Lỗi khi sắp xếp lại");
        fetchSections();
      }
    }
  };

  const handleAdd = () => {
    setEditingSection(null);
    form.resetFields();
    setIsModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditingSection(record);
    form.setFieldsValue({
      name: record.name,
      type: record.type,
      isActive: record.isActive,
      rangeTime: record.startAt && record.endAt ? [dayjs(record.startAt), dayjs(record.endAt)] : null,
      hasDiscount: record.hasDiscount,
      discountType: record.discountType || "PERCENT",
      discountValue: record.discountValue
    });
    setIsModalVisible(true);
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        name: values.name,
        type: values.type,
        isActive: values.isActive,
        displayOrder: editingSection ? editingSection.displayOrder : sections.length,
        startAt: values.rangeTime ? values.rangeTime[0].toISOString() : null,
        endAt: values.rangeTime ? values.rangeTime[1].toISOString() : null,
        hasDiscount: values.hasDiscount,
        discountType: values.discountType,
        discountValue: values.discountValue,
      };

      if (editingSection) {
        await sectionService.updateSection(editingSection.id, payload);
        message.success("Cập nhật section thành công");
      } else {
        await sectionService.createSection(payload);
        message.success("Thêm section mới thành công");
      }
      setIsModalVisible(false);
      fetchSections();
    } catch (error) {
      message.error("Lỗi khi lưu section");
    }
  };

  const handleDelete = async (id) => {
    try {
      await sectionService.deleteSection(id);
      message.success("Xóa thành công");
      fetchSections();
    } catch (error) {
      message.error("Lỗi khi xóa");
    }
  };

  const columns = [
    { title: "", key: "sort", width: 50 },
    {
      title: "Tên khung",
      dataIndex: "name",
      key: "name",
      render: (text, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{text}</Text>
          <Text type="secondary" style={{ fontSize: '11px' }}>Slug: {record.slug}</Text>
        </Space>
      )
    },
    {
      title: "Loại chuyên mục",
      dataIndex: "type",
      key: "type",
      render: (type) => {
        let color = "blue";
        let label = "Bộ sưu tập";
        if (type === "FLASH_SALE") { color = "red"; label = "Flash Sale ⚡"; }
        if (type === "NEW_ARRIVAL") { color = "green"; label = "Hàng mới về"; }
        return <Tag color={color} className="rounded-full px-3">{label}</Tag>;
      }
    },
    {
      title: "Giảm giá",
      dataIndex: "hasDiscount",
      key: "discount",
      render: (hasDiscount, record) => hasDiscount ? (
        <Tag color="volcano" className="font-bold">
          -{record.discountValue}{record.discountType === 'PERCENT' ? '%' : 'đ'}
        </Tag>
      ) : <Tag color="default">Không</Tag>
    },
    {
      title: "Thời gian Flash Sale",
      key: "time",
      render: (_, record) => record.type === "FLASH_SALE" ? (
        <Space direction="vertical" size={0}>
          <Text type="secondary" style={{ fontSize: '11px' }}>Bắt đầu: {dayjs(record.startAt).format("DD/MM HH:mm")}</Text>
          <Text type="danger" style={{ fontSize: '11px' }}>Kết thúc: {dayjs(record.endAt).format("DD/MM HH:mm")}</Text>
        </Space>
      ) : "-"
    },
    {
      title: "Sản phẩm",
      dataIndex: "products",
      key: "productsCount",
      render: (products) => <Tag color="orange" className="font-bold">{products?.length || 0} SP</Tag>
    },
    {
      title: "Bật/Tắt",
      dataIndex: "isActive",
      key: "isActive",
      render: (isActive, record) => (
        <Switch 
          checked={isActive} 
          onChange={async (checked) => {
             const payload = { ...record, isActive: checked };
             await sectionService.updateSection(record.id, payload);
             fetchSections();
          }} 
        />
      ),
    },
    {
      title: "Thao tác",
      key: "action",
      render: (_, record) => (
        <Space size="middle">
          <Button type="primary" ghost icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Button icon={<OrderedListOutlined />} onClick={() => { setCurrentSection(record); setIsProductModalVisible(true); }} />
          <Popconfirm title="Xóa khung này?" onConfirm={() => handleDelete(record.id)} okText="Xóa">
            <Button type="primary" danger ghost icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="p-6 overflow-hidden">
      <Card className="shadow-sm border-0 rounded-xl">
        <div className="flex justify-between items-center mb-6">
          <div>
            <Title level={3} className="!mb-0 uppercase italic font-black tracking-tighter">Bố cục Trang chủ Động</Title>
            <Text type="secondary" className="text-xs font-bold uppercase tracking-widest">Kéo thả icon <MenuOutlined /> để sắp xếp thứ tự hiển thị</Text>
          </div>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd} size="large" className="font-bold uppercase text-xs tracking-widest h-11 px-8 rounded-lg shadow-lg shadow-primary/20 bg-primary hover:bg-primary/90 border-none transition-all hover:scale-105 active:scale-95">
            Thêm Khung mới
          </Button>
        </div>

        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={onDragEnd}>
          <SortableContext items={sections.map(s => s.id)} strategy={verticalListSortingStrategy}>
            <Table
              components={{ body: { row: SortableRow } }}
              rowKey="id"
              columns={columns}
              dataSource={sections}
              loading={loading}
              pagination={false}
              className="mt-4"
            />
          </SortableContext>
        </DndContext>
      </Card>

      <Modal title={<Text className="font-black uppercase italic tracking-tighter text-lg">{editingSection ? "🏠 Sửa cấu hình khung" : "✨ Tạo khung sản phẩm mới"}</Text>} open={isModalVisible} onOk={handleModalOk} onCancel={() => setIsModalVisible(false)} destroyOnClose width={600}>
        <Form form={form} layout="vertical" className="mt-4">
          <Form.Item name="name" label={<Text className="text-[10px] font-bold uppercase tracking-widest text-gray-400">Tên khung hiển thị</Text>} rules={[{ required: true }]}>
            <Input className="rounded-lg h-10 font-bold" placeholder="Ví dụ: LINH KIỆN ASUS RA MẮT" />
          </Form.Item>
          
          <div className="grid grid-cols-2 gap-4">
            <Form.Item name="type" label={<Text className="text-[10px] font-bold uppercase tracking-widest text-gray-400">Loại chuyên mục</Text>} initialValue="COLLECTION">
              <Select className="h-10">
                <Select.Option value="COLLECTION">Bộ sưu tập</Select.Option>
                <Select.Option value="FLASH_SALE">⚡ Flash Sale</Select.Option>
                <Select.Option value="NEW_ARRIVAL">Hàng mới về</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item name="isActive" label={<Text className="text-[10px] font-bold uppercase tracking-widest text-gray-400">Trạng thái</Text>} valuePropName="checked" initialValue={true}>
              <Switch className="bg-gray-200" />
            </Form.Item>
          </div>

          <Form.Item noStyle shouldUpdate={(prev, current) => prev.type !== current.type}>
            {({ getFieldValue }) => getFieldValue('type') === 'FLASH_SALE' && (
              <Form.Item name="rangeTime" label={<Text className="text-[10px] font-bold uppercase tracking-widest text-gray-400">Thời gian diễn ra Sale</Text>} rules={[{ required: true }]}>
                <RangePicker showTime size="small" className="w-full h-10 rounded-lg" />
              </Form.Item>
            )}
          </Form.Item>

          <div className="p-4 bg-gray-50 rounded-xl border border-gray-100 mb-6 mt-4">
            <div className="flex items-center justify-between mb-4">
               <Text className="text-[10px] font-bold uppercase tracking-widest">Cấu hình giảm giá chung</Text>
               <Form.Item name="hasDiscount" valuePropName="checked" noStyle>
                  <Switch size="small" />
               </Form.Item>
            </div>
            <Form.Item noStyle shouldUpdate={(prev, current) => prev.hasDiscount !== current.hasDiscount}>
               {({ getFieldValue }) => getFieldValue('hasDiscount') && (
                  <div className="grid grid-cols-2 gap-4 animate-fadeIn">
                     <Form.Item name="discountType" label="Loại giảm" initialValue="PERCENT">
                        <Select size="small">
                           <Select.Option value="PERCENT">Giảm theo %</Select.Option>
                           <Select.Option value="AMOUNT">Giảm tiền mặt (đ)</Select.Option>
                        </Select>
                     </Form.Item>
                     <Form.Item noStyle shouldUpdate={(prev, current) => prev.discountType !== current.discountType}>
                        {({ getFieldValue }) => (
                           <Form.Item name="discountValue" label={getFieldValue('discountType') === 'AMOUNT' ? "Số tiền giảm" : "Phần trăm giảm"} rules={[{ required: true, message: 'Nhập giá trị' }]}>
                              <InputNumber className="w-full" size="small" min={0} max={getFieldValue('discountType') === 'PERCENT' ? 100 : 1000000000} formatter={value => getFieldValue('discountType') === 'AMOUNT' ? `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',') : value} parser={value => value.replace(/\$\s?|(,*)/g, '')} addonAfter={getFieldValue('discountType') === 'AMOUNT' ? 'đ' : '%'} />
                           </Form.Item>
                        )}
                     </Form.Item>
                  </div>
               )}
            </Form.Item>
          </div>
        </Form>
      </Modal>

      <Modal title={<Text className="font-black uppercase italic tracking-tighter text-lg">📦 Sản phẩm trong: {currentSection?.name}</Text>} open={isProductModalVisible} onCancel={() => setIsProductModalVisible(false)} footer={null} width={800} destroyOnClose>
         <div className="mb-4">
             <Input prefix={<SearchOutlined />} placeholder="Tìm sản phẩm để thêm vào khung..." onChange={(e) => setSearchProduct(e.target.value)} className="h-11 rounded-xl" />
         </div>
         <div style={{ maxHeight: '400px', overflowY: 'auto' }} className="pr-2 custom-scrollbar">
             <List itemLayout="horizontal" dataSource={allProducts.filter(p => p.name.toLowerCase().includes(searchProduct.toLowerCase()))} renderItem={(item) => {
                 const isInSection = currentSection?.products?.some(p => p.productId === item.id);
                 return (
                     <List.Item actions={[
                         isInSection ? 
                         ( <Button danger type="link" onClick={() => handleRemoveProductFromSection(item.id)} className="font-bold text-xs uppercase">Gỡ bỏ</Button> ) : 
                         ( <Button type="link" onClick={() => handleAddProductToSection(item.id)} className="font-bold text-xs uppercase text-primary">Thêm vào</Button> )
                     ]}>
                         <List.Item.Meta avatar={<Avatar shape="square" size={48} src={getFullImageUrl(item.mainImageUrl || item.imageUrl)} className="border border-gray-100" />} title={<Text className="text-xs font-bold text-gray-800 line-clamp-1">{item.name}</Text>} description={<Text className="text-[10px] font-black text-primary uppercase">{new Intl.NumberFormat('vi-VN').format(item.price)}đ</Text>} />
                     </List.Item>
                 );
             }} />
         </div>
      </Modal>
    </div>
  );
};

export default SectionManagement;
