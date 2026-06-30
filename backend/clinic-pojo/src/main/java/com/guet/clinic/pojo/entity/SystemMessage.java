package com.guet.clinic.pojo.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SystemMessage extends BaseEntity {
    private Long receiverId;
    private String title;
    private String content;
    private String messageType;
    private Boolean readStatus;
    private LocalDateTime readAt;
}
