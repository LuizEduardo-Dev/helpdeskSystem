package com.helpdesk.helpdesk.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "statuses")
@Data
@EqualsAndHashCode(of = "id")

public class Status {
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;

    @jakarta.persistence.Column(nullable = false, unique = true)
    private String name;
}
