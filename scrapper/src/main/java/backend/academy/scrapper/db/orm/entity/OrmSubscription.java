package backend.academy.scrapper.db.orm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscription")
public class OrmSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "link_id", nullable = false, referencedColumnName = "id")
    @ManyToOne
    private OrmLink link;

    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    @ManyToOne
    private OrmUser user;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<OrmFilter> filters;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name="subscription_tag",
        joinColumns = @JoinColumn(name = "subscription_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    List<OrmTag> tags;

    public OrmSubscription(OrmLink link, OrmUser user, List<OrmFilter> filters, List<OrmTag> tags) {
        this(null, link, user, filters, tags);
    }

    public OrmSubscription(OrmLink link, OrmUser user) {
        this(link, user, null, null);
    }

    @PreRemove
    private void preRemove() {
        this.user.subscriptions().remove(this);
        this.link.subscriptions().remove(this);
    }
}
