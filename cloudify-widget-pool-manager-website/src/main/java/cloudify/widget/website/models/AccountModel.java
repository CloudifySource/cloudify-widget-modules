package cloudify.widget.website.models;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/27/14
 * Time: 10:46 AM
 */
public class AccountModel {

    public Long id;

    public String uuid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountModel that = (AccountModel) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AccountModel{id=" + id + ", uuid='" + uuid + '\'' + '}';
    }
}
