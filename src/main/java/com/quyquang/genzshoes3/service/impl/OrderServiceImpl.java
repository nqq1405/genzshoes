package com.quyquang.genzshoes3.service.impl;

import com.quyquang.genzshoes3.entity.*;
import com.quyquang.genzshoes3.exception.BadRequestException;
import com.quyquang.genzshoes3.exception.InternalServerException;
import com.quyquang.genzshoes3.exception.NotFoundException;
import com.quyquang.genzshoes3.model.dto.OrderDetailDTO;
import com.quyquang.genzshoes3.model.dto.OrderInfoDTO;
import com.quyquang.genzshoes3.model.request.CreateOrderRequest;
import com.quyquang.genzshoes3.model.request.UpdateDetailOrder;
import com.quyquang.genzshoes3.model.request.UpdateStatusOrderRequest;
import com.quyquang.genzshoes3.repository.*;
import com.quyquang.genzshoes3.service.OrderService;
import com.quyquang.genzshoes3.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.quyquang.genzshoes3.config.Constants.*;

@Controller
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductSizeRepository sizeRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private StatisticRepository statisticRepository;

    @Override
    public Page<Order> adminGetListOrders(String id, String name, String phone, String status, String product, int page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        int limit = 10;
        Pageable pageable = PageRequest.of(page, limit, Sort.by("created_at").descending());
        return orderRepository.adminGetListOrder(id, name, phone, status, product, pageable);
    }

    @Override
    public Order createOrder(CreateOrderRequest createOrderRequest, long userId) {

        //Kiểm tra sản phẩm có tồn tại
        Optional<Product> product = productRepository.findById(createOrderRequest.getProductId());
        if (product.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại!");
        }

        //Kiểm tra size có sẵn
        ProductSize productSize = productSizeRepository.checkProductAndSizeAvailable(createOrderRequest.getProductId(), createOrderRequest.getSize());
        if (productSize == null) {
            throw new BadRequestException("Size giày sản phẩm tạm hết, Vui lòng chọn sản phẩm khác!");
        }

        //Kiểm tra giá sản phẩm
        if (product.get().getSalePrice() != createOrderRequest.getProductPrice()) {
            throw new BadRequestException("Giá sản phẩm thay đổi, Vui lòng đặt hàng lại!");
        }

        if(sizeRepository.checkProductAndSizeAvailable(
                createOrderRequest.getProductId(), createOrderRequest.getSize()).getQuantity() < createOrderRequest.getQuantity()){
            throw new BadRequestException("Size sản phẩm này hiện tại "+ sizeRepository.checkProductAndSizeAvailable(
                    createOrderRequest.getProductId(), createOrderRequest.getSize()).getQuantity() +" trong kho!!");
        }
        Optional<Promotion> promotion = promotionRepository.findByCouponCode(createOrderRequest.getCouponCode());
        if (promotion.isEmpty()) {
            throw new NotFoundException("Mã khuyến mãi không tồn tại!");
        }
        Promotion promotion1 = promotion.get();
        Order order = new Order();
        User user = new User();
        user.setId(userId);
        order.setCreatedBy(user);
        order.setBuyer(user);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        order.setReceiverAddress(createOrderRequest.getReceiverAddress());
        order.setReceiverName(createOrderRequest.getReceiverName());
        order.setReceiverPhone(createOrderRequest.getReceiverPhone());
        order.setNote(createOrderRequest.getNote());
        order.setSize(createOrderRequest.getSize());
        order.setPrice(createOrderRequest.getProductPrice());
        order.setTotalPrice(createOrderRequest.getTotalPrice());
        order.setStatus(ORDER_STATUS);
        order.setQuantity(createOrderRequest.getQuantity());
        order.setProduct(product.get());
        order.setPromotion(new Order.UsedPromotion(promotion1.getCouponCode(),
                promotion1.getDiscountType(),
                promotion1.getDiscountValue(),
                promotion1.getMaximumDiscountValue()));
        orderRepository.save(order);
        return order;

    }

    @Override
    public void updateDetailOrder(UpdateDetailOrder updateDetailOrder, long id, long userId) {
        //Kiểm trả có đơn hàng
        Optional<Order> rs = orderRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }

        Order order = rs.get();
        //Kiểm tra trạng thái đơn hàng
        if (order.getStatus() != ORDER_STATUS) {
            throw new BadRequestException("Chỉ cập nhật đơn hàng ở trạng thái chờ lấy hàng");
        }

        //Kiểm tra size sản phẩm
        Optional<Product> product = productRepository.findById(updateDetailOrder.getProductId());
        if (product.isEmpty()) {
            throw new BadRequestException("Sản phẩm không tồn tại");
        }
        //Kiểm tra giá
        if (product.get().getSalePrice() != updateDetailOrder.getProductPrice()) {
            throw new BadRequestException("Giá sản phẩm thay đổi vui lòng đặt hàng lại");
        }

        ProductSize productSize = productSizeRepository.checkProductAndSizeAvailable(updateDetailOrder.getProductId(), updateDetailOrder.getSize());
        if (productSize == null) {
            throw new BadRequestException("Size giày sản phẩm tạm hết, Vui lòng chọn sản phẩm khác");
        }

        //Kiểm tra khuyến mại
        if (updateDetailOrder.getCouponCode() != "") {
            Promotion promotion = promotionService.checkPromotion(updateDetailOrder.getCouponCode());
            if (promotion == null) {
                throw new NotFoundException("Mã khuyến mãi không tồn tại hoặc chưa được kích hoạt");
            }
            long promotionPrice = promotionService.calculatePromotionPrice(updateDetailOrder.getProductPrice(), promotion);
            if (promotionPrice != updateDetailOrder.getTotalPrice()) {
                throw new BadRequestException("Tổng giá trị đơn hàng thay đổi. Vui lòng kiểm tra và đặt lại đơn hàng");
            }
            Order.UsedPromotion usedPromotion = new Order.UsedPromotion(updateDetailOrder.getCouponCode(), promotion.getDiscountType(), promotion.getDiscountValue(), promotion.getMaximumDiscountValue());
            order.setPromotion(usedPromotion);
        }

        order.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        order.setProduct(product.get());
        order.setSize(updateDetailOrder.getSize());
        order.setPrice(updateDetailOrder.getProductPrice());
        order.setTotalPrice(updateDetailOrder.getTotalPrice());


        order.setStatus(ORDER_STATUS);
        User user = new User();
        user.setId(userId);
        order.setModifiedBy(user);
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            throw new InternalServerException("Lỗi khi cập nhật");
        }
    }


    @Override
    public Order findOrderById(long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }
        return order.get();
    }

    @Override
    public void updateStatusOrder(UpdateStatusOrderRequest updateStatusOrderRequest, long orderId, long userId) {
        Optional<Order> rs = orderRepository.findById(orderId);
        if (rs.isEmpty()) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }
        Order order = rs.get();
        //Kiểm tra trạng thái của đơn hàng
        boolean check = false;
        for (Integer status : LIST_ORDER_STATUS) {
            if (status == updateStatusOrderRequest.getStatus()) {
                check = true;
                break;
            }
        }
        if (!check) {
            throw new BadRequestException("Trạng thái đơn hàng không hợp lệ");
        }
        //Cập nhật trạng thái đơn hàng
        if (order.getStatus() == ORDER_STATUS) {
            //Đơn hàng ở trạng thái chờ lấy hàng
            if (updateStatusOrderRequest.getStatus() == ORDER_STATUS) {
                order.setReceiverPhone(updateStatusOrderRequest.getReceiverPhone());
                order.setReceiverName(updateStatusOrderRequest.getReceiverName());
                order.setReceiverAddress(updateStatusOrderRequest.getReceiverAddress());
                //Đơn hàng ở trạng thái đang vận chuyển
            } else if (updateStatusOrderRequest.getStatus() == DELIVERY_STATUS) {
                //Trừ đi một sản phẩm
                productSizeRepository.minusOneProductBySize(order.getProduct().getId(), order.getSize(), order.getQuantity());
                //Đơn hàng ở trạng thái đã giao hàng
            } else if (updateStatusOrderRequest.getStatus() == COMPLETED_STATUS) {
                //Trừ đi một sản phẩm và cộng một sản phẩm vào sản phẩm đã bán và cộng tiền
                productSizeRepository.minusOneProductBySize(order.getProduct().getId(), order.getSize(), order.getQuantity());
                productRepository.plusOneProductTotalSold(order.getProduct().getId(), order.getQuantity());
                statistic(order.getTotalPrice(), order.getQuantity(), order);
            } else if (updateStatusOrderRequest.getStatus() != CANCELED_STATUS) {
                throw new BadRequestException("Không thế chuyển sang trạng thái này");
            }
            //Đơn hàng ở trạng thái đang giao hàng
        } else if (order.getStatus() == DELIVERY_STATUS) {
            //Đơn hàng ở trạng thái đã giao hàng
            if (updateStatusOrderRequest.getStatus() == COMPLETED_STATUS) {
                //Cộng một sản phẩm vào sản phẩm đã bán và cộng tiền
                productRepository.plusOneProductTotalSold(order.getProduct().getId(), order.getQuantity());
                statistic(order.getTotalPrice(), order.getQuantity(), order);
                //Đơn hàng ở trạng thái đã hủy
            } else if (updateStatusOrderRequest.getStatus() == RETURNED_STATUS) {
                //Cộng lại một sản phẩm đã bị trừ
                productSizeRepository.plusOneProductBySize(order.getProduct().getId(), order.getSize(), order.getQuantity());
                //Đơn hàng ở trạng thái đã trả hàng
            } else if (updateStatusOrderRequest.getStatus() == CANCELED_STATUS) {
                //Cộng lại một sản phẩm đã bị trừ
                productSizeRepository.plusOneProductBySize(order.getProduct().getId(), order.getSize(), order.getQuantity());
            } else if (updateStatusOrderRequest.getStatus() != DELIVERY_STATUS) {
                throw new BadRequestException("Không thế chuyển sang trạng thái này");
            }
            //Đơn hàng ở trạng thái đã giao hàng
        } else if (order.getStatus() == COMPLETED_STATUS) {
            //Đơn hàng đang ở trạng thái đã hủy
            if (updateStatusOrderRequest.getStatus() == RETURNED_STATUS) {
                //Cộng một sản phẩm đã bị trừ và trừ đi một sản phẩm đã bán và trừ số tiền
                productSizeRepository.plusOneProductBySize(order.getProduct().getId(), order.getSize(), order.getQuantity());
                productRepository.minusOneProductTotalSold(order.getProduct().getId());
                updateStatistic(order.getTotalPrice(), order.getQuantity(), order);
            } else if (updateStatusOrderRequest.getStatus() != COMPLETED_STATUS) {
                throw new BadRequestException("Không thế chuyển sang trạng thái này");
            }
        } else {
            if (order.getStatus() != updateStatusOrderRequest.getStatus()) {
                throw new BadRequestException("Không thế chuyển đơn hàng sang trạng thái này");
            }
        }

        User user = new User();
        user.setId(userId);
        order.setModifiedBy(user);
        order.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        order.setNote(updateStatusOrderRequest.getNote());
        order.setStatus(updateStatusOrderRequest.getStatus());
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            throw new InternalServerException("Lỗi khi cập nhật trạng thái");
        }
    }

    @Override
    public List<OrderInfoDTO> getListOrderOfPersonByStatus(int status, long userId) {
        List<OrderInfoDTO> list = orderRepository.getListOrderOfPersonByStatus(status, userId);

        for (OrderInfoDTO dto : list) {
            for (int i = 0; i < SIZE_VN.size(); i++) {
                if (SIZE_VN.get(i) == dto.getSizeVn()) {
                    dto.setSizeUs(SIZE_US[i]);
                    dto.setSizeCm(SIZE_CM[i]);
                }
            }
        }
        return list;
    }

    @Override
    public OrderDetailDTO userGetDetailById(long id, long userId) {
        OrderDetailDTO order = orderRepository.userGetDetailById(id, userId);
        if (order == null) {
            return null;
        }

        if (order.getStatus() == ORDER_STATUS) {
            order.setStatusText("Chờ lấy hàng");
        } else if (order.getStatus() == DELIVERY_STATUS) {
            order.setStatusText("Đang giao hàng");
        } else if (order.getStatus() == COMPLETED_STATUS) {
            order.setStatusText("Đã giao hàng");
        } else if (order.getStatus() == CANCELED_STATUS) {
            order.setStatusText("Đơn hàng đã trả lại");
        } else if (order.getStatus() == RETURNED_STATUS) {
            order.setStatusText("Đơn hàng đã hủy");
        }

        for (int i = 0; i < SIZE_VN.size(); i++) {
            if (SIZE_VN.get(i) == order.getSizeVn()) {
                order.setSizeUs(SIZE_US[i]);
                order.setSizeCm(SIZE_CM[i]);
            }
        }

        return order;
    }

    @Override
    public void userCancelOrder(long id, long userId) {
        Optional<Order> rs = orderRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Đơn hàng không tồn tại");
        }
        Order order = rs.get();
        if (order.getBuyer().getId() != userId) {
            throw new BadRequestException("Bạn không phải chủ nhân đơn hàng");
        }
        if (order.getStatus() != ORDER_STATUS) {
            throw new BadRequestException("Trạng thái đơn hàng không phù hợp để hủy. Vui lòng liên hệ với shop để được hỗ trợ");
        }

        order.setStatus(CANCELED_STATUS);
        orderRepository.save(order);
    }

    @Override
    public long getCountOrder() {
        return orderRepository.count();
    }

    @Override
    public int getQuantityById(long id, long userId) {
        return orderRepository.getQuantityByIdAnduserId(id,userId);
    }

    public void statistic(long amount, int quantity, Order order) {
        Statistic statistic = statisticRepository.findByCreatedAT();
        if (statistic != null){
            statistic.setOrder(order);
            statistic.setSales(statistic.getSales() + amount);
            statistic.setQuantity(statistic.getQuantity() + quantity);
            statistic.setProfit(statistic.getSales() - (statistic.getQuantity() * order.getProduct().getPrice()));
            statisticRepository.save(statistic);
        }else {
            Statistic statistic1 = new Statistic();
            statistic1.setOrder(order);
            statistic1.setSales(amount);
            statistic1.setQuantity(quantity);
            statistic1.setProfit(amount - (quantity * order.getProduct().getPrice()));
            statistic1.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            statisticRepository.save(statistic1);
        }
    }

    public void updateStatistic(long amount, int quantity, Order order) {
        Statistic statistic = statisticRepository.findByCreatedAT();
        if (statistic != null) {
            statistic.setOrder(order);
            statistic.setSales(statistic.getSales() - amount);
            statistic.setQuantity(statistic.getQuantity() - quantity);
            statistic.setProfit(statistic.getSales() - (statistic.getQuantity() * order.getProduct().getPrice()));
            statisticRepository.save(statistic);
        }
    }
}
