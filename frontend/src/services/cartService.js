import api from "./api";

export const cartService = {
  getCart: (userId) => api.get(`/carts/user/${userId}`),
  
  addToCart: (userId, productId, quantity) => 
    api.post("/carts/add", { userId, productId, quantity }),
    
  updateQuantity: (itemId, userId, quantity) => 
    api.put(`/carts/item/${itemId}?userId=${userId}&quantity=${quantity}`),
    
  removeItem: (itemId, userId) => 
    api.delete(`/carts/item/${itemId}?userId=${userId}`),
};
