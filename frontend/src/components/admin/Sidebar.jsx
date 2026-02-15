import { useNavigate } from "react-router-dom";
import { LogoutOutlined, UserOutlined } from "@ant-design/icons";
import { Avatar, Dropdown } from "antd";
import SidebarGroup from "./SidebarGroup";
import { sidebarConfig } from "./sidebarConfig";
import { authAPI } from "../../services/api";

export default function Sidebar({ user }) {
  const navigate = useNavigate();

  const handleLogout = () => {
    authAPI.logout();
  };

  const userMenuItems = [
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: "Đăng xuất",
      onClick: handleLogout,
    },
  ];

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col h-screen fixed left-0 top-0">
      {/* Logo/Brand */}
      <div
        className="h-16 flex items-center justify-center border-b border-gray-200 cursor-pointer flex-shrink-0"
        onClick={() => navigate("/admin/dashboard")}
      >
        <img
          src="/logo-exeshop.png"
          alt="EXEShop Logo"
          className="h-10 w-auto object-contain"
        />
      </div>

      {/* Navigation with custom scrollbar */}
      <div className="flex-1 overflow-y-auto py-4 custom-scrollbar">
        {sidebarConfig.menuGroups.map((group, index) => (
          <SidebarGroup key={index} group={group} />
        ))}
      </div>

      {/* User Profile */}
      <div className="border-t border-gray-200 p-4 flex-shrink-0">
        <Dropdown menu={{ items: userMenuItems }} placement="topRight">
          <div className="flex items-center gap-3 cursor-pointer hover:bg-gray-50 p-2 rounded-md transition-colors">
            <Avatar
              style={{ backgroundColor: "#ff4d4f" }}
              icon={<UserOutlined />}
            />
            <div className="flex-1 min-w-0">
              <div className="text-sm font-medium text-gray-900 truncate">
                {user?.fullName || "Admin"}
              </div>
              <div className="text-xs text-gray-500 truncate">
                {user?.email || "admin@exeshop.vn"}
              </div>
            </div>
          </div>
        </Dropdown>
      </div>

      {/* Custom Scrollbar Styles */}
      <style jsx>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }

        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
        }

        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: #d1d5db;
          border-radius: 3px;
        }

        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: #9ca3af;
        }
      `}</style>
    </aside>
  );
}
