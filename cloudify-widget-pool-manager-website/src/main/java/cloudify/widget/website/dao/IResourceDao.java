package cloudify.widget.website.dao;

import cloudify.widget.website.models.ResourceModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/20/14
 * Time: 4:34 PM
 */
public interface IResourceDao {

    public Long create( Long accountId, String name, String contentType, byte[] content ) throws IOException;

    public boolean delete( Long accountId, Long resourceId);

    public ResourceModel readResourceDetails(Long accountId, Long resourceId);

    public List<ResourceModel> listResourcesDetails( Long accountId );

    public byte[] readResourceContent( Long accountId, Long resourceId );
}
