import { Card, Row, Col, Statistic } from "antd";
import {
  DollarOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  ShoppingOutlined,
} from "@ant-design/icons";

export default function DashboardPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>

      {/* Statistics Cards */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        <Card className="flex-1">
          <Statistic
            title="Tổng doanh thu"
            // value={112893000}
            precision={0}
            styles={{ content: { color: "#3f8600", whiteSpace: "nowrap" } }}
            prefix={<DollarOutlined />}
            suffix="₫"
          />
        </Card>
        <Card className="flex-1">
          <Statistic
            title="Đơn hàng"
            // value={93}
            prefix={<ShoppingCartOutlined />}
            styles={{ content: { color: "#1890ff", whiteSpace: "nowrap" } }}
          />
        </Card>
        <Card className="flex-1">
          <Statistic
            title="Người dùng"
            // value={256}
            prefix={<UserOutlined />}
            styles={{ content: { color: "#ff4d4f", whiteSpace: "nowrap" } }}
          />
        </Card>
        <Card className="flex-1">
          <Statistic
            title="Sản phẩm"
            // value={187}
            prefix={<ShoppingOutlined />}
            styles={{ content: { color: "#722ed1", whiteSpace: "nowrap" } }}
          />
        </Card>
      </div>

      <Card title="Đơn hàng gần đây" className="mt-6">
        {/* <p>Danh sách đơn hàng sẽ hiển thị ở đây...</p> */}
      </Card>

      {/* Charts Section */}
      <div className="grid grid-cols-2 gap-4">
        <Card title="Sản phẩm bán chạy">
          {/* <p>Biểu đồ sản phẩm bán chạy...</p> */}
        </Card>
        <Card title="Doanh thu theo tháng">
          {/* <p>Biểu đồ doanh thu...</p> */}
        </Card>
      </div>
    </div>
  );
}
