import React, { useState, useEffect } from "react";
import { Card, Statistic, Row, Col, Spin, Empty, Tabs, Alert, List, Tag, Button, Space } from "antd";
import {
  DollarOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  ShoppingOutlined,
  NotificationOutlined,
  ArrowRightOutlined,
  BarChartOutlined,
  DashboardOutlined
} from "@ant-design/icons";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  Cell,
} from "recharts";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../services/api";

const { TabPane } = Tabs;
const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884d8"];

export default function DashboardPage() {
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    totalRevenue: 0,
    totalOrders: 0,
    totalUsers: 0,
    totalProducts: 0,
    monthlyRevenue: [],
    topProducts: [],
    notifications: []
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = await axiosInstance.get("/admin/dashboard/stats");
        setStats(response.data);
      } catch (error) {
        console.error("Error fetching stats:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="h-96 flex items-center justify-center">
        <Spin size="large" tip="Đang tải dữ liệu..." />
      </div>
    );
  }

  const OverviewTab = () => (
    <div className="flex flex-col gap-6">
      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 order-1">
        <Card className="shadow-sm border-none bg-white overflow-hidden relative group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:scale-110 transition-transform">
              <DollarOutlined className="text-6xl text-green-600" />
          </div>
          <Statistic
            title={<span className="text-gray-500 font-medium tracking-tight">Tổng doanh thu</span>}
            value={stats.totalRevenue}
            precision={0}
            valueStyle={{ color: "#059669", fontWeight: "900", fontSize: "1.75rem" }}
            suffix="₫"
          />
        </Card>
        <Card className="shadow-sm border-none bg-white overflow-hidden relative group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:scale-110 transition-transform">
              <ShoppingCartOutlined className="text-6xl text-blue-600" />
          </div>
          <Statistic
            title={<span className="text-gray-500 font-medium tracking-tight">Đơn hàng</span>}
            value={stats.totalOrders}
            valueStyle={{ color: "#2563eb", fontWeight: "900", fontSize: "1.75rem" }}
          />
        </Card>
        <Card className="shadow-sm border-none bg-white overflow-hidden relative group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:scale-110 transition-transform">
              <UserOutlined className="text-6xl text-red-600" />
          </div>
          <Statistic
            title={<span className="text-gray-500 font-medium tracking-tight">Người dùng</span>}
            value={stats.totalUsers}
            valueStyle={{ color: "#dc2626", fontWeight: "900", fontSize: "1.75rem" }}
          />
        </Card>
        <Card className="shadow-sm border-none bg-white overflow-hidden relative group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:scale-110 transition-transform">
              <ShoppingOutlined className="text-6xl text-purple-600" />
          </div>
          <Statistic
            title={<span className="text-gray-500 font-medium tracking-tight">Sản phẩm</span>}
            value={stats.totalProducts}
            valueStyle={{ color: "#7c3aed", fontWeight: "900", fontSize: "1.75rem" }}
          />
        </Card>
      </div>

      {/* Notifications Box - Moved to bottom and changed to cards */}
      {stats.notifications && stats.notifications.length > 0 && (
        <div className="order-2 mt-4">
           <div className="flex items-center gap-2 mb-4">
              <NotificationOutlined className="text-orange-500 text-lg" /> 
              <span className="text-gray-800 font-black uppercase tracking-wider">Thông báo hệ thống gần đây</span>
           </div>
           <div className="flex flex-col gap-3">
              {stats.notifications.map((item, index) => (
                <Card 
                  key={index}
                  hoverable
                  className="border-none shadow-sm rounded-xl overflow-hidden cursor-pointer hover:shadow-md transition-all border-l-4 border-l-orange-500"
                  bodyStyle={{ padding: '16px 24px' }}
                  onClick={() => navigate(item.target || "/admin")}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <Tag color={item.type === 'STOCK' ? 'volcano' : 'purple'} className="font-bold border-none px-3 py-1 rounded-full uppercase text-[10px]">
                        {item.type === 'STOCK' ? 'KHO HÀNG' : 'HỆ THỐNG'}
                      </Tag>
                      <span className="font-bold text-gray-700">{item.message}</span>
                    </div>
                    <ArrowRightOutlined className="text-gray-300" />
                  </div>
                </Card>
              ))}
           </div>
        </div>
      )}
    </div>
  );

  const StatsTab = () => (
    <div className="flex flex-col gap-6">
      <Card title={<div className="font-bold flex items-center gap-2 text-red-600"><BarChartOutlined /> Sản phẩm bán chạy</div>} className="shadow-sm border-none rounded-2xl">
        {stats.topProducts && stats.topProducts.length > 0 ? (
          <div style={{ width: '100%', height: 350 }}>
            <ResponsiveContainer>
              <BarChart data={stats.topProducts} layout="vertical" margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                <XAxis type="number" hide />
                <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} width={150} tick={{fill: '#666', fontSize: 11}} />
                <Tooltip 
                  cursor={{fill: 'transparent'}}
                  contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                />
                <Bar dataKey="sales" radius={[0, 4, 4, 0]}>
                  {stats.topProducts.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <Empty description="Chưa có dữ liệu sản phẩm" />
        )}
      </Card>

      <Card title={<div className="font-bold flex items-center gap-2 text-blue-600"><BarChartOutlined /> Biến động doanh thu 6 tháng gần nhất</div>} className="shadow-sm border-none rounded-2xl">
        {stats.monthlyRevenue && stats.monthlyRevenue.length > 0 ? (
          <div style={{ width: '100%', height: 450 }}>
            <ResponsiveContainer>
              <AreaChart data={stats.monthlyRevenue} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorRev" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#e30019" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#e30019" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#999', fontSize: 12}} />
                <YAxis hide />
                <Tooltip 
                  contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                  formatter={(value) => [`${value.toLocaleString()} ₫`, "Doanh thu"]}
                />
                <Area type="monotone" dataKey="revenue" stroke="#e30019" fillOpacity={1} fill="url(#colorRev)" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <Empty description="Chưa có dữ liệu doanh thu" />
        )}
      </Card>
    </div>
  );

  return (
    <div className="dashboard-container">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-black uppercase tracking-tight text-gray-800 m-0">
          Admin Control Center
        </h1>
      </div>

      <Tabs 
        defaultActiveKey="overview" 
        className="admin-tabs custom-tabs"
        items={[
          {
            key: 'overview',
            label: <span className="flex items-center gap-2"><DashboardOutlined /> Tổng quan</span>,
            children: <OverviewTab />,
          },
          {
            key: 'stats',
            label: <span className="flex items-center gap-2"><BarChartOutlined /> Thống kê doanh thu</span>,
            children: <StatsTab />,
          },
        ]}
      />
    </div>
  );
}
