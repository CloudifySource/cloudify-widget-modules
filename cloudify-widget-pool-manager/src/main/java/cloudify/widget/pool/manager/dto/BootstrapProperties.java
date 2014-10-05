package cloudify.widget.pool.manager.dto;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 8:53 PM
 */
public class BootstrapProperties {

    private String publicIp; // optional
    private String privateIp; // optional
    private String cloudifyUrl;
    private String preBootstrapScript; // optional
    private String recipeUrl; // optional
    private String recipeRelativePath; // optional
    private String randomPasswordRegex = "[a-zA-Z0-9]{16}"; //optional
    private String script;

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public String getCloudifyUrl() {
        return cloudifyUrl;
    }

    public void setCloudifyUrl(String cloudifyUrl) {
        this.cloudifyUrl = cloudifyUrl;
    }

    public String getPreBootstrapScript() {
        return preBootstrapScript;
    }

    public void setPreBootstrapScript(String preBootstrapScript) {
        this.preBootstrapScript = preBootstrapScript;
    }

    public String getRecipeUrl() {
        return recipeUrl;
    }

    public void setRecipeUrl(String recipeUrl) {
        this.recipeUrl = recipeUrl;
    }

    public String getRecipeRelativePath() {
        return recipeRelativePath;
    }

    public void setRecipeRelativePath(String recipeRelativePath) {
        this.recipeRelativePath = recipeRelativePath;
    }

    public String getRandomPasswordRegex() {
        return randomPasswordRegex;
    }

    public void setRandomPasswordRegex(String randomPasswordRegex) {
        this.randomPasswordRegex = randomPasswordRegex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BootstrapProperties that = (BootstrapProperties) o;

        if (!cloudifyUrl.equals(that.cloudifyUrl)) return false;
        if (preBootstrapScript != null ? !preBootstrapScript.equals(that.preBootstrapScript) : that.preBootstrapScript != null)
            return false;
        if (privateIp != null ? !privateIp.equals(that.privateIp) : that.privateIp != null) return false;
        if (publicIp != null ? !publicIp.equals(that.publicIp) : that.publicIp != null) return false;
        if (recipeRelativePath != null ? !recipeRelativePath.equals(that.recipeRelativePath) : that.recipeRelativePath != null)
            return false;
        if (recipeUrl != null ? !recipeUrl.equals(that.recipeUrl) : that.recipeUrl != null) return false;
        if (randomPasswordRegex != null ? !randomPasswordRegex.equals(that.randomPasswordRegex) : that.randomPasswordRegex != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = publicIp != null ? publicIp.hashCode() : 0;
        result = 31 * result + (privateIp != null ? privateIp.hashCode() : 0);
        result = 31 * result + cloudifyUrl.hashCode();
        result = 31 * result + (preBootstrapScript != null ? preBootstrapScript.hashCode() : 0);
        result = 31 * result + (recipeUrl != null ? recipeUrl.hashCode() : 0);
        result = 31 * result + (recipeRelativePath != null ? recipeRelativePath.hashCode() : 0);
        result = 31 * result + (randomPasswordRegex != null ? randomPasswordRegex.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BootstrapProperties{" +
                "publicIp='" + publicIp + '\'' +
                ", privateIp='" + privateIp + '\'' +
                ", cloudifyUrl='" + cloudifyUrl + '\'' +
                ", preBootstrapScript='" + preBootstrapScript + '\'' +
                ", recipeUrl='" + recipeUrl + '\'' +
                ", recipeRelativePath='" + recipeRelativePath + '\'' +
                '}';
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
