import React, { useState, useEffect, useMemo } from "react";
import { Table, Tag, Button, message, Space, Card, Modal, Select, Typography, Input } from "antd";
import { EyeOutlined, SyncOutlined, CheckCircleOutlined, ClockCircleOutlined, CloseCircleOutlined, SearchOutlined } from "@ant-design/icons";
import axiosInstance from "../../../services/api";

const { Title, Text } = Typography;
const { Option } = Select;

const OrderManagement = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const response = await axiosInstance.get("/orders/admin/all");
      const sortedData = (response.data || []).sort((a, b) => b.id - a.id);
      setOrders(sortedData);
    } catch (error) {
      message.error("Không thể tải danh sách đơn hàng");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const filteredOrders = useMemo(() => {
    return orders.filter(order => {
      const matchesSearch = 
        order.orderCode?.toLowerCase().includes(searchText.toLowerCase()) ||
        order.user?.fullName?.toLowerCase().includes(searchText.toLowerCase());
      
      const matchesStatus = statusFilter === "ALL" || order.status === statusFilter;
      
      return matchesSearch && matchesStatus;
    });
  }, [orders, searchText, statusFilter]);

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      await axiosInstance.put(`/orders/admin/${orderId}/status`, { status: newStatus });
      message.success("Cập nhật trạng thái thành công");
      fetchOrders();
    } catch (error) {
      message.error("Lỗi khi cập nhật trạng thái");
    }
  };

  const getStatusTag = (status) => {
    switch (status) {
      case "PENDING": return <Tag icon={<ClockCircleOutlined />} color="warning">Chờ thanh toán</Tag>;
      case "PROCESSING": return <Tag icon={<CheckCircleOutlined />} color="success">Thành công</Tag>;
      case "SHIPPED": return <Tag icon={<SyncOutlined spin />} color="geekblue">Đang giao hàng</Tag>;
      case "DELIVERED": return <Tag icon={<CheckCircleOutlined />} color="success">Đã giao hàng</Tag>;
      case "CANCELLED": return <Tag icon={<CloseCircleOutlined />} color="error">Đã hủy</Tag>;
      default: return <Tag color="default">{status}</Tag>;
    }
  };

  const columns = [
    { title: "Mã đơn", dataIndex: "orderCode", key: "orderCode" },
    { 
      title: "Khách hàng", 
      key: "user",
      render: (_, record) => record.user?.fullName || <Text type="secondary">Ẩn danh</Text>
    },
    { 
      title: "Tổng tiền", 
      dataIndex: "finalPrice", 
      render: (price) => `${price ? price.toLocaleString() : 0} ₫` 
    },
    { 
      title: "Trạng thái", 
      dataIndex: "status", 
      render: (status) => getStatusTag(status) 
    },
    {
      title: "Cập nhật",
      key: "update",
      render: (_, record) => (
        <Select 
          value={record.status} 
          style={{ width: 140 }} 
          onChange={(val) => handleStatusChange(record.id, val)}
          size="small"
        >
          <Option value="PENDING">Chờ thanh toán</Option>
          <Option value="PROCESSING">Thành công</Option>
          <Option value="SHIPPED">Đang giao hàng</Option>
          <Option value="DELIVERED">Đã giao hàng</Option>
          <Option value="CANCELLED">Hủy đơn</Option>
        </Select>
      )
    },
    {
      title: "Thao tác",
      key: "action",
      render: (_, record) => (
        <Button icon={<EyeOutlined />} onClick={() => { setSelectedOrder(record); setIsModalVisible(true); }}>Chi tiết</Button>
      )
    }
  ];

  return (
    <Card 
      title={<div className="font-bold text-lg">Quản lý đơn hàng</div>} 
      className="shadow-sm border-none rounded-2xl"
      extra={
        <Space>
           <Input 
              placeholder="Tìm mã đơn, tên khách..." 
              prefix={<SearchOutlined />} 
              onChange={e => setSearchText(e.target.value)}
              style={{ width: 250 }}
              allowClear
           />
           <Select 
              defaultValue="ALL" 
              style={{ width: 150 }}
              onChange={val => setStatusFilter(val)}
           >
              <Option value="ALL">Tất cả trạng thái</Option>
              <Option value="PENDING">Chờ thanh toán</Option>
              <Option value="PROCESSING">Thành công</Option>
              <Option value="SHIPPED">Đang giao hàng</Option>
              <Option value="DELIVERED">Đã giao hàng</Option>
              <Option value="CANCELLED">Đã hủy</Option>
           </Select>
        </Space>
      }
    >
      <Table columns={columns} dataSource={filteredOrders} rowKey="id" loading={loading} />
      
      <Modal 
        title="Chi tiết đơn hàng"
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={null}
        width={700}
      >
        {selectedOrder && (
          <div className="space-y-4">
             <div className="grid grid-cols-2 gap-4">
                <div><Text type="secondary">Mã đơn:</Text> <Text strong>{selectedOrder.orderCode}</Text></div>
                <div><Text type="secondary">Ngày đặt:</Text> <Text strong>{new Date(selectedOrder.createdAt).toLocaleString()}</Text></div>
                <div><Text type="secondary">Điện thoại:</Text> <Text strong>{selectedOrder.receiverPhone}</Text></div>
                <div><Text type="secondary">Địa chỉ:</Text> <Text strong>{selectedOrder.shippingAddress}</Text></div>
                {selectedOrder.voucher && (
                  <div>
                    <Text type="secondary">Mã giảm giá:</Text> <Tag color="green" className="font-bold ml-1">{selectedOrder.voucher.code}</Tag>
                  </div>
                )}
                <div>
                   <Text type="secondary">Phí vận chuyển:</Text> <Text strong>{selectedOrder.shippingFee === 0 ? "Miễn phí" : new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(selectedOrder.shippingFee)}</Text>
                </div>
             </div>
             <Table 
                dataSource={selectedOrder.orderDetails} 
                pagination={false} 
                size="small"
                columns={[
                    { title: "Sản phẩm", dataIndex: ["product", "name"] },
                    { title: "SL", dataIndex: "quantity" },
                    { title: "Giá", dataIndex: "priceAtPurchase", render: (p) => `${p.toLocaleString()} ₫` }
                ]}
             />
          </div>
        )}
      </Modal>
    </Card>
  );
};

export default OrderManagement;
