package com.quickbite.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignment_states")
@Data
@NoArgsConstructor
public class AssignmentState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer lastAssignedPartnerId;
}
