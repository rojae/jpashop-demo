package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.mapping;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    /**
     * V2. DTO 사용
     * N+1 문제 발생
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * V3. Collection Fetch Join
     * 페이징 불가능.
     * 데이터전송 네트워크 상으로 전달되는 데이터가 많음
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();

        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * V3.1. Collection Fetch Join with fetch Size
     * 페이징 가능.
     *  (그러나 V3에 비해서, To Many 관계 갯수 만큼 Query를 더 실행)
     * 최적화된 결과 = 데이터전송 네트워크 전송 유리
     * default_batch_fetch_size 추가로 인한 inQuery 미리 100개씩 로딩.
     * 1+N -> 1+1
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ){
        // To One 관계는 우선 FetchJoin으로 가져옴
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        // To Many 관계는 LAZY loading
        // default_batch_fetch_size를 통한 성능개선
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * V4. JPA DTO 직접 조회
     * 1:1 관계는 단순 조회
     * 1:n 관계는 각각 별도로 처리한다.
     * N + 1 문제 발생
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * V5. JPA DTO 직접 조회 (컬렉션 최적화)
     * 1:1 관계는 단순 조회
     * 1:n 관계는 각각 별도로 처리한다.
     * N + 1 -> 1 + 1
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * V6
     * Flat하게 sql을 통해서, 데이터 추출 (쿼리는 1번이지만, 느릴 수 있음)
     * Query 1번
     * 애플리케이션 메모리를 사용하여, api spec에 조립하여 반환 필요. (애플리케이션에서 추가작업)
     * Flat된 중복 데이터는 페이징이 불가능.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }

    @Getter
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        /**
         * DTO안에 entity를 wrapping하지 말자.
         * value object인 address는 괜찮음.
         */
        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }

    @Getter
    static class OrderItemDto{
        private String itemName;    // 상품명
        private int orderPrice;     // 주문 가격
        private int count;          // 주문 수량

        public OrderItemDto(OrderItem orderItem){
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getItem().getPrice();
            count = orderItem.getCount();
        }
    }
}
