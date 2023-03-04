package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ManyToOne(cascade = {CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PageEntity pageEntity;

    @Column(columnDefinition = "INT", nullable = false)
    private int lemma_id;

    @Column(columnDefinition = "FLOAT", nullable = false, name = "`rank`")
    private float rank;

    public IndexEntity(PageEntity pageEntity, int lemma_id, float rank) {
        this.pageEntity = pageEntity;
        this.lemma_id = lemma_id;
        this.rank = rank;
    }
}
