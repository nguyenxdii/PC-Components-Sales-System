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
    const userData = localStorage.getItem("user");
    if (userData) {
      const parsedUser = JSON.parse(userData);
      setUser(parsedUser);
      fetchCart(parsedUser.id);
    }
  }, []);

  const fetchCart = async (userId) => {
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
    let currentUser = user;
    if (!currentUser) {
      const userData = localStorage.getItem("user");
      if (userData) {
        currentUser = JSON.parse(userData);
        setUser(currentUser);
      }
    }

    if (!currentUser) {
      message.warning("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
      return;
    }

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
    let currentUser = user || JSON.parse(localStorage.getItem("user"));
    if (!currentUser) return;
    
    try {
      const response = await cartService.updateQuantity(itemId, currentUser.id, quantity);
      setCart(response.data);
    } catch (error) {
      message.error("Lỗi khi cập nhật số lượng!");
    }
  };

  const removeFromCart = async (itemId) => {
    let currentUser = user || JSON.parse(localStorage.getItem("user"));
    if (!currentUser) return;
    
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
        refreshCart: () => user && fetchCart(user.id),
      }}
    >
      {children}
    </CartContext.Provider>
  );
};
