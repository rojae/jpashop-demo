package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class BookForm {
    private Long id;

    @NotEmpty(message = "이름은 필수입니다")
    private String name;

    @Min(value = 1, message = "1보다 커야합니다.")
    private int price;

    @Min(value = 1, message = "1보다 커야합니다.")
    private int stockQuantity;

    @NotEmpty(message = "저자는 필수입니다")
    private String author;

    private String isbn;

}
