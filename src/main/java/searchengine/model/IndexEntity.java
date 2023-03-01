package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "indexes")
public class IndexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "INT", nullable = false)
    private int page_id;

    @Column(columnDefinition = "INT", nullable = false)
    private int lemma_id;

    @Column(columnDefinition = "FLOAT", nullable = false)
    private float rank;
}
