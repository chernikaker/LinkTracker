package backend.academy.scrapper.db.orm.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "tg_user")
public class OrmUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false, unique = true)
    private long chatId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<OrmSubscription> subscriptions;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<OrmTag> tags;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<OrmFilter> filters;
}
