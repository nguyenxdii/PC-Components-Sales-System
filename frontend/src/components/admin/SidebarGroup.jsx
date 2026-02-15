import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { RightOutlined, DownOutlined } from "@ant-design/icons";
import SidebarItem from "./SidebarItem";

export default function SidebarGroup({ group }) {
  const location = useLocation();

  // Check if this group contains the active menu item
  const hasActiveItem = group.items.some(
    (item) => item.url === location.pathname,
  );

  const [isExpanded, setIsExpanded] = useState(hasActiveItem);

  // Auto-expand when active item changes
  useEffect(() => {
    if (hasActiveItem) {
      setIsExpanded(true);
    }
  }, [hasActiveItem]);

  return (
    <div className="mb-4">
      {/* Group Header - Clickable */}
      <div
        className="flex items-center justify-between px-4 py-2 cursor-pointer hover:bg-gray-50 rounded-md mx-2 transition-colors"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
          {group.title}
        </h3>
        {isExpanded ? (
          <DownOutlined style={{ fontSize: "10px", color: "#9ca3af" }} />
        ) : (
          <RightOutlined style={{ fontSize: "10px", color: "#9ca3af" }} />
        )}
      </div>

      {/* Menu Items - Collapsible */}
      {isExpanded && (
        <div className="mt-1 space-y-1">
          {group.items.map((item, index) => (
            <SidebarItem key={index} item={item} />
          ))}
        </div>
      )}
    </div>
  );
}
