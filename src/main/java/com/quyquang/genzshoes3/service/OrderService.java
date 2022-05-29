package com.quyquang.genzshoes3.service;

import com.quyquang.genzshoes3.entity.Order;
import com.quyquang.genzshoes3.model.dto.OrderDetailDTO;
import com.quyquang.genzshoes3.model.dto.OrderInfoDTO;
import com.quyquang.genzshoes3.model.request.CreateOrderRequest;
import com.quyquang.genzshoes3.model.request.UpdateDetailOrder;
import com.quyquang.genzshoes3.model.request.UpdateStatusOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    Page<Order> adminGetListOrders(String id, String name, String phone, String status, String product, int page);

    Order createOrder(CreateOrderRequest createOrderRequest, long userId);

    void updateDetailOrder(UpdateDetailOrder updateDetailOrder, long id, long userId);

    Order findOrderById(long id);

    void updateStatusOrder(UpdateStatusOrderRequest updateStatusOrderRequest, long orderId, long userId);

    List<OrderInfoDTO> getListOrderOfPersonByStatus(int status, long userId);

    OrderDetailDTO userGetDetailById(long id, long userId);

    void userCancelOrder(long id, long userId);

    //Đếm số lượng đơn hàng
    long getCountOrder();

    // lấy số lượng mua
    int getQuantityById(long id, long userId);

}
