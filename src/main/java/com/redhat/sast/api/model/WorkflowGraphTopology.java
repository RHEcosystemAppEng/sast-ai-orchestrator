package com.redhat.sast.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "workflow_graph_topology",
        indexes = {@Index(name = "idx_workflow_graph_topology_topology_id", columnList = "topology_id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class WorkflowGraphTopology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "topology_id", unique = true, nullable = false)
    private Integer topologyId;

    @Column(name = "description")
    private String description;

    @Column(name = "edges", columnDefinition = "jsonb", nullable = false)
    private String edges;
}
