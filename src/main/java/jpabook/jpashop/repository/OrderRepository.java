package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em){
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

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
        QOrder order = QOrder.order;
        QMember member = QMember.member;

        return query.select(order)
                .from(order)
                .join(order.member, member)
                .where(eqStatus(orderSearch.getOrderStatus()), likeName(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression eqStatus(OrderStatus orderStatus){
        if(orderStatus == null){
            return null;
        }
        return QOrder.order.status.eq(orderStatus);
    }

    private BooleanExpression likeName(String memberName){
        if(!StringUtils.hasText(memberName)){
            return null;
        }
        return QMember.member.name.like(memberName);
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

    // Fetch Join
    // 페이징 기능 추가
    // 필요한 To Many관계는 LAZY로 로딩처리
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o " +
                        " join fetch o.member m" +
                        " join fetch o.delivery d ", Order.class
        )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
