package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pages", indexes = @Index(name = "path_index", columnList = "path"))//unique Наверно лишний - у разных сайтов одинаковые страницы?
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity siteEntity;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

}
