package jpabook.jpashop.repository.order.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OrderItemQueryDto {
    @JsonIgnore
    private Long id;
    private String itemName;
    private int orderPrice;
    private int count;

    public OrderItemQueryDto(Long id, String itemName, int orderPrice, int count) {
        this.id = id;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
