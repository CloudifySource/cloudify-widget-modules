package cloudify.widget.softlayer;

/**
 * Created by sefi on 03/03/15.
 */
public class SoftlayerDataCenter {

    private Long id;
    private String name;
    private String longName;

    public SoftlayerDataCenter(Long id, String name, String longName) {
        this.id = id;
        this.name = name;
        this.longName = longName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        return longName;
    }
}
