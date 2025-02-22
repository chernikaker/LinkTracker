package backend.academy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LinkUpdate {
    private long id;
    private String url;
    private String description;
    private List<Long> tgChatIds;

    @Override
    public String toString() {
        return "LinkUpdate [id=" + id + ", url=" + url + ", description=" + description+"]";
    }
}
