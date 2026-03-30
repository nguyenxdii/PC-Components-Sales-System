import api from './api';

export const orderService = {
    createOrder: (orderData) => {
        return api.post('/orders', orderData);
    },
    getUserOrders: (userId) => {
        return api.get(`/orders/user/${userId}`);
    },
    getOrderById: (orderId) => {
        return api.get(`/orders/${orderId}`);
    },
    checkStatus: (orderCode) => {
        return api.get(`/orders/check-status/${orderCode}`);
    }
};
