package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import jpabook.jpashop.repository.ordersimplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {
            order.getMember().getName();        //Lazy 강제 초기화
            order.getDelivery().getAddress();   //Lazy 강제 초기환
        }
        return all;
    }

    /**
     * V2. DTO 사용
     * - LAZY 정책으로 인한, 성능 저하
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        // IF ORDER가 2개
        // N + 1 -> 1 + N (N=2)
        // N + 1 -> 1 + Member(N) + Delivery(N)
        List<Order> orders = orderRepository.findAll();

        // ORDER를 루프를 돌려서, SimpleOrderDto 진행
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * V3. FetchJoin 사용
     * - N+! 문제 해결.
     * - 1번의 Query로 해결.
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * V4. FetchJoin 사용, 원하는 DTO로 변경
     * - orderRepository.findOrderDtos 추가
     * - 재사용성이 사실상 제로.
     * - V3보다 약간 성능이 좋음. (네트워크 전송 성능, 그러나 큰 요즘은 의미없음)
     *     ** 성능 테스트 필요.
     * - 결과가 엔티티가 아니기 때문에, 변경 캐싱은 불가.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();   // LAZY 초기화 (영속성 컨텍스트가 값이 없으면, 추가 쿼리)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }

}
