package cloudify.widget.website.controller;

import cloudify.widget.pool.manager.NodeManagementExecutor;
import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.dto.PoolStatus;
import cloudify.widget.website.dao.IAccountDao;
import cloudify.widget.website.dao.IPoolDao;
import cloudify.widget.website.models.AccountModel;
import cloudify.widget.website.models.PoolConfigurationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@SuppressWarnings("UnusedDeclaration")
@Controller
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private IPoolDao poolDao;

    @Autowired
    private PoolManagerApi poolManagerApi;

    @Autowired
    private NodeManagementExecutor nodeManagementExecutor;


    public void setPoolManagerApi(PoolManagerApi poolManagerApi) {
        this.poolManagerApi = poolManagerApi;
    }

    @RequestMapping(value = "/account/pools", method = RequestMethod.GET)
    @ResponseBody
    public List<PoolConfigurationModel> getPools(@ModelAttribute("account") AccountModel accountModel) {
        return poolDao.readPools(accountModel);
    }

    @RequestMapping(value = "/account/pools/{poolId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PoolConfigurationModel> getPoolConfiguration(@ModelAttribute("account") AccountModel accountModel, @PathVariable("poolId") Long poolId) {

        PoolConfigurationModel poolConfigurationModel = poolDao.readPoolByIdAndAccountId(poolId, accountModel.getId());
        ResponseEntity<PoolConfigurationModel> retValue;

        if (poolConfigurationModel == null) {
            retValue = new ResponseEntity<PoolConfigurationModel>(HttpStatus.NOT_FOUND);
        } else {
            retValue = new ResponseEntity<PoolConfigurationModel>(poolConfigurationModel, HttpStatus.OK);
        }

        return retValue;
    }

    @RequestMapping(value = "/account/pools", method = RequestMethod.POST)
    @ResponseBody
    public Long createPool(@ModelAttribute("account") AccountModel accountModel, @RequestBody String poolSettingJson) {
        try {
            Long poolId = poolDao.createPool(accountModel.getId(), poolSettingJson);
            nodeManagementExecutor.start(poolDao.readPoolById(poolId).poolSettings);
            return poolId;
        } catch (Exception e) {
            logger.error("unable to createPool", e);
            return null;
//            return new ResponseEntity<String>( HttpStatus.INTERNAL_SERVER_ERROR );
        }
    }

    @RequestMapping(value = "/account/pools/{poolId}", method = RequestMethod.POST)
    @ResponseBody
    public boolean updatePoolConfiguration(@ModelAttribute("account") AccountModel accountModel,
                                           @PathVariable("poolId") Long poolId, @RequestBody String poolSettingJson) {

        boolean updated = false;
        try {
            updated = poolDao.updatePool(poolId, accountModel.getId(), poolSettingJson);
        } catch (Exception e) {
            logger.error("failed to update pool", e);
            e.printStackTrace();
        }
        nodeManagementExecutor.update(poolDao.readPoolById(poolId).poolSettings);
        return updated;
    }

    @RequestMapping(value = "/account/pools/{poolId}/delete", method = RequestMethod.POST)
    @ResponseBody
    public boolean deletePoolConfiguration(@ModelAttribute("account") AccountModel accountModel,
                                           @PathVariable("poolId") Long poolId) {

        nodeManagementExecutor.stop(poolDao.readPoolById(poolId).poolSettings);
        return poolDao.deletePool(poolId, accountModel.getId());
    }

    @RequestMapping(value = "/account/pools/{poolId}/clean", method = RequestMethod.POST)
    @ResponseBody
    public void cleanPool(@ModelAttribute("account") AccountModel accountModel,
                          @PathVariable("poolId") Long poolId) {

        PoolSettings poolSettings = poolDao.readPoolByIdAndAccountId(poolId, accountModel.getId()).getPoolSettings();
        poolManagerApi.cleanPool(poolSettings);
    }

    @RequestMapping(value = "/account/pools/{poolId}/status", method = RequestMethod.GET)
    @ResponseBody
    public PoolStatus getPoolStatus(@ModelAttribute("account") AccountModel accountModel,
                                    @PathVariable("poolId") Long poolId) {
        try {
            PoolStatus retValue = null;
            PoolConfigurationModel poolConfiguration = poolDao.readPoolByIdAndAccountId(poolId, accountModel.getId());
            if (poolConfiguration != null) {
                PoolSettings poolSettings = poolConfiguration.getPoolSettings();
                if (poolSettings != null) {
                    retValue = poolManagerApi.getStatus(poolSettings);
                }
            }

            return retValue;
        } catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "/account/pools/{poolId}/occupy", method = RequestMethod.GET)
    @ResponseBody
    public NodeModel occupyPoolNode(@ModelAttribute("account") AccountModel accountModel, @PathVariable("poolId") Long poolId, @RequestBody String expires) {

        long expiresLong = System.currentTimeMillis() + 20 * 60 * 1000;  // default to 20 minutes.

        try {
            expiresLong = Long.parseLong(expires);
        } catch (Exception e) {
            logger.warn("using default expires because expires was NaN = [{}]", expires);
        }
        logger.info("occupying pool node with expires : " + expiresLong);
        PoolSettings poolSettings = poolDao.readPoolByIdAndAccountId(poolId, accountModel.getId()).getPoolSettings();
        logger.info("read pool successfully. moving on with occupying node");
        return poolManagerApi.occupy(poolSettings, expiresLong);
    }

    @RequestMapping(value = "/account/pools/{poolId}/nodes/{nodeId}/expire", method = RequestMethod.POST)
    @ResponseBody
    public void expirePoolNode(@ModelAttribute("account") AccountModel accountModel, @PathVariable("poolId") Long poolId, @PathVariable Long nodeId) {
        PoolSettings poolSettings = poolDao.readPoolByIdAndAccountId(poolId, accountModel.getId()).getPoolSettings();
        poolManagerApi.expireNode(poolSettings, nodeId);
    }

    @RequestMapping(value = "/account/pools/status", method = RequestMethod.GET)
    @ResponseBody
    public String getPoolsStatus(@ModelAttribute("account") AccountModel accountModel) {
        try {
            return "TBD pools status";
        } catch (Exception e) {
            logger.error("unable to retrieve pool status", e);
            return null;
        }
    }

    // who's loading the "account" attribute on the request?
    @ModelAttribute("account")
    public AccountModel getUser(HttpServletRequest request) {
        return (AccountModel) request.getAttribute("account");
    }

}