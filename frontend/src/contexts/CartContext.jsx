import React, { createContext, useContext, useState, useEffect } from "react";
import { cartService } from "../services/cartService";
import { message } from "antd";

const CartContext = createContext();

export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState({ items: [], totalAmount: 0 });
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Tự động kiểm tra Auth state để dọn dẹp giỏ hàng
    const checkAuthAndFetchCart = () => {
      const userData = localStorage.getItem("user");
      const token = localStorage.getItem("token");
      
      if (!userData || !token) {
        if (user) {
          setUser(null);
          setCart({ items: [], totalAmount: 0 });
        }
        return;
      }

      const parsedUser = JSON.parse(userData);
      // Nếu là user mới hoặc chưa có user trong state
      if (!user || user.id !== parsedUser.id) {
        setUser(parsedUser);
        fetchCart(parsedUser.id);
      }
    };

    checkAuthAndFetchCart();
    
    // Polling nhẹ để bắt kịp thay đổi localStorage (Login/Logout)
    const intervalId = setInterval(checkAuthAndFetchCart, 1000);
    return () => clearInterval(intervalId);
  }, [user]);

  const fetchCart = async (userId) => {
    if (!userId) {
       setCart({ items: [], totalAmount: 0 });
       return;
    }
    try {
      setLoading(true);
      const response = await cartService.getCart(userId);
      setCart(response.data);
    } catch (error) {
      console.error("Error fetching cart:", error);
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (productId, quantity = 1) => {
    const userData = localStorage.getItem("user");
    if (!userData) {
      message.warning("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
      return;
    }
    const currentUser = JSON.parse(userData);

    try {
      setLoading(true);
      const response = await cartService.addToCart(currentUser.id, productId, quantity);
      setCart(response.data);
      message.success("Đã thêm sản phẩm vào giỏ hàng!");
    } catch (error) {
      const errorMsg = error.response?.data?.message || "Không thể thêm vào giỏ hàng!";
      message.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const updateQuantity = async (itemId, quantity) => {
    const userData = localStorage.getItem("user");
    if (!userData) return;
    const currentUser = JSON.parse(userData);
    
    try {
      const response = await cartService.updateQuantity(itemId, currentUser.id, quantity);
      setCart(response.data);
    } catch (error) {
      message.error("Lỗi khi cập nhật số lượng!");
    }
  };

  const removeFromCart = async (itemId) => {
    const userData = localStorage.getItem("user");
    if (!userData) return;
    const currentUser = JSON.parse(userData);
    
    try {
      const response = await cartService.removeItem(itemId, currentUser.id);
      setCart(response.data);
      message.success("Đã xóa sản phẩm khỏi giỏ hàng!");
    } catch (error) {
      message.error("Lỗi khi xóa sản phẩm!");
    }
  };

  const cartCount = cart.items ? cart.items.reduce((sum, item) => sum + item.quantity, 0) : 0;

  return (
    <CartContext.Provider
      value={{
        cart,
        loading,
        addToCart,
        updateQuantity,
        removeFromCart,
        cartCount,
        refreshCart: () => {
          const userData = localStorage.getItem("user");
          if (userData) fetchCart(JSON.parse(userData).id);
        }
      }}
    >
      {children}
    </CartContext.Provider>
  );
};
