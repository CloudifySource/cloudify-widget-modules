package cloudify.widget.pool.manager.dto;

import java.util.List;

/**
 * Created by sefi on 7/21/14.
 */
public class EmailSettings {

    private Boolean turnedOn;

    private List<String> recipients;

    public Boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(Boolean turnedOn) {
        this.turnedOn = turnedOn;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
}
