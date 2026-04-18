package com.cshm.campussecondhandmark.common.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {
    Long id;

    LocalDateTime created_time;

    LocalDateTime updated_time;
}
