package id.co.msiglife.pollux.entity;

import lombok.*;

        import javax.persistence.*;
        import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "LST_MENU_PLX", schema = "EKA")
public class MenuPlx {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LSMP_ID")
    private Long lsmpId;

    @Column(name = "MENU_ID")
    private Long menuId;

    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "LABEL")
    private String label;

    @Column(name = "TYPE")
    private Integer type;

    @Column(name = "PATH")
    private String path;

    @Column(name = "ICON")
    private String icon;

    @Column(name = "LAST_UPDATED_BY")
    private Long lastUpdatedBy;

    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "UPDATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @Column(name = "DELETED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;

    @Column(name = "PATH_IDS")
    private String pathIds;

    // Optional bidirectional relationship or transient fields can be added here
}

