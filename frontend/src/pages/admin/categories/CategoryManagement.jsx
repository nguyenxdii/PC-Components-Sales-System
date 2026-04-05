import React, { useState, useEffect } from "react";
import {
  Table,
  Button,
  Space,
  Popconfirm,
  message,
  Tag,
  Modal,
  Input,
  Switch,
  Form,
  Select,
  Upload,
  Typography,
  Drawer,
  Divider,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  UploadOutlined,
  ExclamationCircleOutlined,
  RightOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import { categoryService } from "../../../services/categoryService";

const { confirm } = Modal;
const { Option } = Select;
const { Title, Text } = Typography;

const CategoryManagement = () => {
  const [categories, setCategories] = useState([]);
  const [categoryTree, setCategoryTree] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(false);

  // Modal & Form states
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isDeleteModalVisible, setIsDeleteModalVisible] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [isAddMode, setIsAddMode] = useState(false);
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState([]);
  const [actionLoading, setActionLoading] = useState(false);
  const [expandedRowKeys, setExpandedRowKeys] = useState([]);
  const [searchText, setSearchText] = useState("");

  const filteredCategories = categories.filter(c => 
    c.name.toLowerCase().includes(searchText.toLowerCase()) || 
    c.slug.toLowerCase().includes(searchText.toLowerCase())
  );

  useEffect(() => {
    fetchData();
    fetchBrands();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const allData = await categoryService.getAllCategories();
      const buildTree = (items) => {
        const rootItems = [];
        const itemMap = {};
        items.forEach((item) => {
          itemMap[item.id] = { ...item, children: [] };
        });
        items.forEach((item) => {
          if (item.parent && item.parent.id) {
            if (itemMap[item.parent.id]) {
              itemMap[item.parent.id].children.push(itemMap[item.id]);
            }
          } else {
            rootItems.push(itemMap[item.id]);
          }
        });
        const cleanEmptyChildren = (nodes) => {
          nodes.forEach((node) => {
            if (node.children.length === 0) {
              delete node.children;
            } else {
              cleanEmptyChildren(node.children);
            }
          });
        };
        cleanEmptyChildren(rootItems);
        return rootItems;
      };
      const tableTreeData = buildTree(allData);
      setCategories(tableTreeData);
      setCategoryTree(tableTreeData);
    } catch (error) {
      console.error(error);
      message.error("Lỗi khi tải dữ liệu danh mục");
    } finally {
      setLoading(false);
    }
  };

  const fetchBrands = async () => {
    try {
      const { brandService } = await import("../../../services/brandService");
      const data = await brandService.getActiveBrands();
      setBrands(data);
    } catch (error) {
      console.error("Lỗi tải thương hiệu", error);
    }
  };

  const handleToggleStatus = async (record, e) => {
    e.stopPropagation();
    try {
      await categoryService.toggleCategoryStatus(record.id);
      message.success(`Đã ${record.isActive ? "ẩn" : "hiện"} danh mục`);
      fetchData();
    } catch (error) {
      message.error("Lỗi đổi trạng thái");
    }
  };

  const columns = [
    {
      title: "Icon",
      dataIndex: "iconUrl",
      key: "iconUrl",
      width: 70,
      render: (url) =>
        url ? (
          <img
            src={url}
            alt="icon"
            className="w-10 h-10 object-contain rounded"
          />
        ) : (
          <div className="w-10 h-10 bg-gray-50 rounded flex items-center justify-center text-gray-300">
            -
          </div>
        ),
    },
    {
      title: "Tên Danh Mục",
      dataIndex: "name",
      key: "name",
      render: (text, record) => {
        const isChild = !!record.parent;
        const hasChildren = record.children && record.children.length > 0;
        const isExpanded = expandedRowKeys.includes(record.id);

        const handleToggle = (e) => {
          e.stopPropagation();
          if (isExpanded) {
            setExpandedRowKeys(expandedRowKeys.filter(key => key !== record.id));
          } else {
            setExpandedRowKeys([...expandedRowKeys, record.id]);
          }
        };

        return (
          <div style={{ 
            paddingLeft: isChild ? 24 : 0, 
            display: 'flex', 
            alignItems: 'center',
            gap: '4px',
            cursor: hasChildren ? 'pointer' : 'default'
          }} onClick={hasChildren ? handleToggle : undefined}>
            {isChild && <span className="text-gray-300">—</span>}
            <Text 
              strong={!record.parent} 
              className={record.parent ? "text-gray-500" : "text-gray-800"}
            >
              {text}
            </Text>
            {hasChildren && (
              <RightOutlined 
                style={{ 
                  fontSize: '10px', 
                  transition: 'transform 0.3s',
                  transform: isExpanded ? 'rotate(90deg)' : 'rotate(0deg)',
                  marginLeft: '4px',
                  color: '#999'
                }} 
              />
            )}
          </div>
        );
      },
    },
    {
      title: "Đường dẫn (Slug)",
      dataIndex: "slug",
      key: "slug",
      render: (slug) => <Text type="secondary" className="text-xs">{slug}</Text>,
    },
    {
      title: "Cấp độ",
      key: "level",
      width: 120,
      render: (_, record) => (
        <Tag color={record.parent ? "default" : "blue"}>
          {record.parent ? "Danh mục con" : "Danh mục gốc"}
        </Tag>
      ),
    },
    {
      title: "Liên kết",
      key: "links",
      width: 150,
      render: (_, record) => {
        const brandCount = record.brands?.length || 0;
        return (
          <Text type="secondary" style={{ fontSize: "12px" }}>
            {brandCount} thương hiệu
          </Text>
        );
      },
    },
    {
      title: "Trạng thái",
      dataIndex: "isActive",
      key: "isActive",
      width: 100,
      align: "center",
      render: (isActive, record) => {
        return (
          <Switch
            checked={isActive}
            size="small"
            onChange={(checked, e) => handleToggleStatus(record, e)}
          />
        );
      },
    },
  ];

  const onRowClick = (record) => {
    setSelectedCategory(record);
    setIsAddMode(false);
    form.setFieldsValue({
      name: record.name,
      slug: record.slug,
      isActive: record.isActive,
      parentId: record.parent ? record.parent.id : null,
      brandIds: record.brands ? record.brands.map((b) => b.id) : [],
    });
    if (record.iconUrl) {
      setFileList([{ uid: "-1", name: "icon", status: "done", url: record.iconUrl }]);
    } else {
      setFileList([]);
    }
    setIsModalVisible(true);
  };

  const handleCreateNew = () => {
    setSelectedCategory(null);
    setIsAddMode(true);
    form.resetFields();
    setFileList([]);
    setIsModalVisible(true);
  };

  const handleDelete = () => {
    if (!selectedCategory) return;
    setIsDeleteModalVisible(true);
  };

  const handleConfirmDelete = async () => {
    try {
      await categoryService.deleteCategoryPermanent(selectedCategory.id);
      message.success("Đã xóa vĩnh viễn");
      setIsDeleteModalVisible(false);
      setIsModalVisible(false);
      fetchData();
    } catch (error) {
      message.error("Lỗi khi xóa");
    }
  };

  const onFinish = async (values) => {
    setActionLoading(true);
    try {
      const formData = new FormData();
      formData.append("name", values.name);
      if (values.slug) formData.append("slug", values.slug);
      formData.append("isActive", values.isActive);
      if (values.parentId) formData.append("parentId", values.parentId);
      if (values.brandIds) values.brandIds.forEach(id => formData.append("brandIds", id));
      if (fileList[0]?.originFileObj) formData.append("file", fileList[0].originFileObj);

      if (isAddMode) {
        await categoryService.createCategory(formData);
        message.success("Thêm thành công");
      } else {
        await categoryService.updateCategory(selectedCategory.id, formData);
        message.success("Cập nhật thành công");
      }
      setIsModalVisible(false);
      fetchData();
    } catch (error) {
      message.error("Lỗi khi lưu dữ liệu");
    } finally {
      setActionLoading(false);
    }
  };

  const renderTreeOptions = (nodes, level = 1) => {
    let options = [];
    nodes.forEach((node) => {
      const prefix = "\u00A0\u00A0\u00A0".repeat(level - 1) + (level > 1 ? "|_ " : "");
      
      // Không cho phép chọn danh mục cấp 3 làm cha (vì tối đa là 3 cấp)
      const isDisabled = selectedCategory?.id === node.id || level >= 3;
      
      options.push(
        <Select.Option key={node.id} value={node.id} disabled={isDisabled}>
          {prefix}{node.name} {level >= 3 ? "(Tối đa cấp độ)" : ""}
        </Select.Option>
      );
      if (node.children) options = options.concat(renderTreeOptions(node.children, level + 1));
    });
    return options;
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <div>
          <Title level={2}>Quản Lý Danh Mục (Categories)</Title>
          <Text type="secondary">
            Hệ thống phân cấp linh kiện & thiết bị. Quản lý danh mục cha/con và thương hiệu liên kết.
          </Text>
        </div>
        <Button
          type="primary"
          size="large"
          icon={<PlusOutlined />}
          onClick={handleCreateNew}
          className="rounded-lg h-10 px-6"
        >
          Thêm Mới
        </Button>
      </div>

      <div className="mb-6 flex gap-4">
        <Input
          placeholder="Tìm kiếm danh mục..."
          prefix={<SearchOutlined className="text-gray-400" />}
          allowClear
          value={searchText}
          onChange={e => setSearchText(e.target.value)}
          className="h-10 rounded-lg w-80"
        />
      </div>

      <Table
        className="category-table"
          columns={columns}
          dataSource={filteredCategories}
          rowKey="id"
          loading={loading}
          pagination={false}
          onRow={(record) => ({
            onClick: () => onRowClick(record),
          })}
          rowClassName={(record) => (record.parent ? "category-row-child transition-all" : "category-row-parent group transition-all")}
          expandable={{
            expandedRowKeys,
            onExpandedRowsChange: (keys) => setExpandedRowKeys(keys),
            expandIcon: () => null,
            expandIconColumnIndex: 1,
            rowExpandable: (record) => record.children && record.children.length > 0,
          }}
        />

      <Modal
        title={
          <div className="pt-2 pb-4 border-b border-gray-100 mb-4 flex items-center gap-3">
            <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isAddMode ? 'bg-blue-50 text-blue-500' : 'bg-indigo-50 text-indigo-500'}`}>
               {isAddMode ? <PlusOutlined /> : <EditOutlined />}
            </div>
            <div>
              <Text type="secondary" className="text-[10px] uppercase font-bold tracking-wider block leading-none mb-1">Cấu hình hệ thống</Text>
              <Title level={4} className="!m-0">{isAddMode ? "Thêm Danh Mục Mới" : `Chỉnh sửa: ${selectedCategory?.name}`}</Title>
            </div>
          </div>
        }
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={null}
        width={550}
        centered
        className="category-modal"
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={onFinish} initialValues={{ isActive: true }}>
          <div className="grid grid-cols-2 gap-x-6">
            <div className="col-span-2 flex justify-center mb-8">
              <div className="relative group">
                <Upload
                  listType="picture-card"
                  fileList={fileList}
                  onChange={({ fileList: fl }) => setFileList(fl.slice(-1))}
                  beforeUpload={() => false}
                  maxCount={1}
                  className="category-upload-main"
                >
                  {fileList.length >= 1 ? null : (
                    <div className="flex flex-col items-center">
                      <PlusOutlined className="text-xl text-gray-400 mb-2" />
                      <Text className="text-xs text-gray-400">Tải biểu tượng</Text>
                    </div>
                  )}
                </Upload>
                {fileList.length > 0 && (
                  <div className="absolute -bottom-2 w-full text-center">
                    <Tag className="rounded-full bg-white/80 backdrop-blur-sm border-gray-200 text-[10px]">Thay đổi ảnh</Tag>
                  </div>
                )}
              </div>
            </div>

            <Form.Item 
              name="name" 
              label={<span className="font-medium text-gray-700">Tên Danh Mục</span>} 
              className="col-span-2"
              rules={[{ required: true, message: "Nhập tên danh mục" }]}
            >
              <Input placeholder="Ví dụ: Card đồ họa, CPU..." className="h-11 rounded-xl bg-gray-50 border-none focus:bg-white transition-all shadow-sm" />
            </Form.Item>

            <Form.Item 
              name="parentId" 
              label={<span className="font-medium text-gray-700">Danh mục cha</span>}
            >
              <Select allowClear placeholder="-- Danh mục gốc --" showSearch className="category-select h-11" dropdownStyle={{ borderRadius: '12px' }}>
                {renderTreeOptions(categoryTree)}
              </Select>
            </Form.Item>

            <Form.Item 
              name="slug" 
              label={<span className="font-medium text-gray-700">Đường dẫn (Slug)</span>}
            >
              <Input placeholder="vd: cpu-intel" className="h-11 rounded-xl bg-gray-50 border-none focus:bg-white transition-all shadow-sm" />
            </Form.Item>

            <Form.Item 
              name="brandIds" 
              label={<span className="font-medium text-gray-700">Thương hiệu liên kết</span>} 
              className="col-span-2"
            >
              <Select mode="multiple" placeholder="Chọn thương hiệu tiêu biểu..." allowClear className="category-select min-h-[44px]">
                {brands.map(b => (
                  <Option key={b.id} value={b.id}>{b.name}</Option>
                ))}
              </Select>
            </Form.Item>

            <div className="col-span-2 flex items-center justify-between p-4 bg-blue-50/50 rounded-2xl mb-8">
              <div>
                <Text strong className="block text-blue-900">Hiển thị danh mục</Text>
                <Text type="secondary" className="text-xs">Bật để khách hàng có thể nhìn thấy danh mục này</Text>
              </div>
              <Form.Item name="isActive" valuePropName="checked" className="!mb-0">
                <Switch checkedChildren="Hiện" unCheckedChildren="Ẩn" />
              </Form.Item>
            </div>
          </div>

          <Divider className="my-6 border-gray-100" />
          
          <div className="flex justify-between items-center bg-gray-50 -mx-6 -mb-6 p-6 rounded-b-3xl">
            <div className="flex gap-2">
              {!isAddMode && (
                <Button 
                  danger 
                  icon={<DeleteOutlined />} 
                  onClick={handleDelete} 
                  type="text" 
                  className="hover:bg-red-50 rounded-lg font-medium"
                >
                  Xóa danh mục
                </Button>
              )}
            </div>
            <div className="flex gap-3">
              <Button 
                onClick={() => setIsModalVisible(false)} 
                className="h-11 px-6 rounded-xl border-gray-200 text-gray-600 hover:text-gray-800"
              >
                Hủy bỏ
              </Button>
              <Button 
                type="primary" 
                onClick={() => form.submit()} 
                loading={actionLoading}
                className="h-11 px-8 rounded-xl bg-blue-600 border-none shadow-lg shadow-blue-200"
              >
                {isAddMode ? "Tạo ngay" : "Lưu thay đổi"}
              </Button>
            </div>
          </div>
        </Form>
      </Modal>

      {/* Modern Delete Confirmation Modal */}
      <Modal
        open={isDeleteModalVisible}
        onCancel={() => setIsDeleteModalVisible(false)}
        footer={null}
        centered
        width={400}
        className="delete-modal"
      >
        <div className="text-center py-4">
          <div className="w-20 h-20 bg-red-50 text-red-500 rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
            <ExclamationCircleOutlined style={{ fontSize: '40px' }} />
          </div>
          <Title level={3} className="!mb-2">Xác nhận xóa?</Title>
          <Text type="secondary" className="block mb-8">
            Bạn có chắc chắn muốn xóa danh mục <Text strong>"{selectedCategory?.name}"</Text>? 
            Hành động này không thể hoàn tác.
          </Text>
          <div className="flex flex-col gap-3">
            <Button 
              danger 
              type="primary" 
              size="large" 
              onClick={handleConfirmDelete}
              className="h-12 rounded-xl font-bold border-none bg-red-500 shadow-lg shadow-red-200"
            >
              Xác định xóa vĩnh viễn
            </Button>
            <Button 
              type="text" 
              size="large" 
              onClick={() => setIsDeleteModalVisible(false)}
              className="h-12 rounded-xl font-medium text-gray-500 hover:bg-gray-100"
            >
              Hủy bỏ (Giữ lại)
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default CategoryManagement;
