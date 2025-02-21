package backend.academy.bot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LinkTrackingObject {

    private String link;
    private String[] tags;
    private  String[] filters;
    private UserState state = UserState.TRACKING_LINK;

}
