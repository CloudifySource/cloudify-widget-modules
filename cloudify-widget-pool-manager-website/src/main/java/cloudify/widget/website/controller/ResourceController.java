package cloudify.widget.website.controller;

import cloudify.widget.website.dao.IResourceDao;
import cloudify.widget.website.exceptions.InternalServerError;
import cloudify.widget.website.models.AccountModel;
import cloudify.widget.website.models.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/20/14
 * Time: 5:37 PM
 */
@Controller
public class ResourceController {
    @Autowired
    private IResourceDao resourceDao;

    private static Logger logger = LoggerFactory.getLogger(ResourceController.class);


   @RequestMapping(value = "/admin/accounts/{accountId}/resources/{resourceId}/delete", method = RequestMethod.POST)
    public @ResponseBody boolean deleteResource(@PathVariable("accountId") Long accountId, @PathVariable("resourceId") Long resourceId) {
        return resourceDao.delete(accountId, resourceId);
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/resources", method = RequestMethod.POST)
    public
    @ResponseBody
    ResourceModel createResource(
            @PathVariable("accountId") Long accountId,
            String name,
            String contentType,
            @RequestParam("file") byte[] file
    ) {

        if ( file.length > 1000000) {
            throw new InternalServerError("file too big");
        }
        if ( file.length != 0 ) {
            try {
                return resourceDao.readResourceDetails( accountId, resourceDao.create(accountId, name, contentType,  file));
            } catch (Exception e) {
                throw new RuntimeException("unable to load file :: " + e.getMessage(), e);
            }
        } else {
            throw new InternalServerError("You failed to upload " + name + " because the file was empty.");
        }
    }


    @RequestMapping(value = "/admin/accounts/{accountId}/resources", method = RequestMethod.GET)
    public @ResponseBody List<ResourceModel> readResources( @PathVariable("accountId") Long accountId ) {
        return resourceDao.listResourcesDetails(accountId);
    }

    @RequestMapping( value = "/admin/accounts/{accountId}/resources/{resourceId}", method = RequestMethod.GET)
    public @ResponseBody ResourceModel readResource( @PathVariable("accountId") Long accountId, @PathVariable("resourceId") Long resourceId ){
        return resourceDao.readResourceDetails( accountId, resourceId);
    }

    @RequestMapping( value = "/account/resources", method = RequestMethod.GET)
    public @ResponseBody List<ResourceModel> readAccountResources( @ModelAttribute("account") AccountModel accountModel ){
        return resourceDao.listResourcesDetails( accountModel.getId() );
    }

    @RequestMapping( value = "/account/resources/{resourceId}", method = RequestMethod.GET)
    public @ResponseBody ResourceModel readAccountResourceDetails( @ModelAttribute("account") AccountModel accountModel, @PathVariable("resourceId") Long resourceId ){
        return readResource(accountModel.getId(), resourceId );
    }

    @RequestMapping( value = "/account/resources", method = RequestMethod.POST)
    public @ResponseBody ResourceModel createAccountResourceModel (
            @ModelAttribute("account") AccountModel accountModel,
            @RequestHeader("File-Name") String fileName,
            @RequestHeader("File-Content-Type") String contentType,
            @RequestBody byte[] file){
        return createResource(accountModel.getId(), fileName, contentType, file);
    }

    @RequestMapping( value = "/account/resources/{resourceId}/delete", method = RequestMethod.POST)
    public @ResponseBody boolean deleteAccountResource( @ModelAttribute("account") AccountModel accountModel, @PathVariable("resourceId") Long resourceId ){
        return deleteResource(accountModel.getId(), resourceId );
    }


    @ModelAttribute("account")
    public AccountModel getUser(HttpServletRequest request)
    {
        return (AccountModel) request.getAttribute("account");
    }

    public IResourceDao getResourceDao() {
        return resourceDao;
    }

    public void setResourceDao(IResourceDao resourceDao) {
        this.resourceDao = resourceDao;
    }
}
