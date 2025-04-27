package ru.otus.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "airports")
public class Airport {

    @Id
    private String code;

    private String name;

    private String city;

    private String country;
}

