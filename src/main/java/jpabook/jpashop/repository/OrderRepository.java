package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAll(){
        return em.createQuery("select o from Order o", Order.class).getResultList();
    }

    public List<Order> findAll(OrderSearch orderSearch){

        return em.createQuery("select o from Order o join o.member m " +
                " where o.status = :status " +
                " and m.name like :name ", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setMaxResults(1000)    // 최대 1000건
                .getResultList();

    }

    // Fetch Join 기법
    // SQL의 결과를 통해서, 객체에 삽입
    // 하이버네이트의 정책과 연관없음.
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o " +
                        " join fetch o.member m" +
                        " join fetch o.delivery d ", Order.class
        ).getResultList();
    }

    // Fetch Join distinct 추가
    // DB Query에서는 distinct가 의미 없지만
    // JPA에서는 JOIN으로 인해, select 뻥튀기 되는 값을 버려준다.
    // distinct를 사용하면, Order의 키값 id의 중복을 제거한다.
    // 즉 JPA의 distinct는 entity의 id값이 같으면, collection에 중복을 제거한다.
    // 단점 : 페이징이 불가능. (메모리에서 limit, sort 처리)
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o " +
                        " join fetch o.member m " +
                        " join fetch o.delivery d " +
                        " join fetch o.orderItems oi " +
                        " join fetch oi.item i ", Order.class)
        .getResultList();
    }
}
