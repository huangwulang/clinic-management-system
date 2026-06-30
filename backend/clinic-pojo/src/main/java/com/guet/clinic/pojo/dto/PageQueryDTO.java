package com.guet.clinic.pojo.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    private int page = 1;
    private int size = 10;
    private String keyword;
}
