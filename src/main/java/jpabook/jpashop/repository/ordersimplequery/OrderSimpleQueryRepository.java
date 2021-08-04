package jpabook.jpashop.repository.ordersimplequery;

import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    /**
     * 성능상 이점이 있지만, 재사용성이 낮음.
     * 사실상, 화면에 종속되어 있는 쿼리.
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order o " +
                        " join o.member m " +
                        " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
