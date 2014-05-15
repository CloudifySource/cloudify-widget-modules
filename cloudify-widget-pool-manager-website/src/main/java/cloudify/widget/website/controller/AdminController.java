package cloudify.widget.website.controller;

import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.website.dao.IAccountDao;
import cloudify.widget.website.dao.IPoolDao;
import cloudify.widget.website.dao.IResourceDao;
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
import java.util.*;


@SuppressWarnings("UnusedDeclaration")
@Controller
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private IPoolDao poolDao;

    @Autowired
    private IResourceDao resourceDao;

    @Autowired
    private PoolManagerApi poolManagerApi;

    public void setPoolManagerApi(PoolManagerApi poolManagerApi) {
        this.poolManagerApi = poolManagerApi;
    }

    @RequestMapping(value = "/checkAdmin/{guymograbi}", method = RequestMethod.GET)
    @ResponseBody
    public String showIndex() {
        logger.info("showing index");
        return "hello world!";
    }


    @RequestMapping(value = "/admin/account", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AccountModel> getAccount(@ModelAttribute("account") AccountModel accountModel) {

        ResponseEntity<AccountModel> retValue;
        if (accountModel == null) {
            retValue = new ResponseEntity<AccountModel>(HttpStatus.NOT_FOUND);
        } else {
            retValue = new ResponseEntity<AccountModel>(accountModel, HttpStatus.OK);
        }

        return retValue;
    }

    @RequestMapping(value = "/admin/accounts", method = RequestMethod.GET)
    @ResponseBody
    public List<AccountModel> getAccounts() {
        logger.info("getting accounts...");
        return accountDao.readAccounts();
    }

    @RequestMapping(value = "/admin/pools", method = RequestMethod.GET)
    @ResponseBody
    public List<PoolConfigurationModel> getPools() {
        return poolDao.readPools();
    }

    @RequestMapping(value = "/admin/accounts", method = RequestMethod.POST)
    @ResponseBody
    public AccountModel createAccount() {
        String accountUuid = UUID.randomUUID().toString();

        AccountModel accountModel = new AccountModel();
        accountModel.setUuid(accountUuid);

        Long accountId = accountDao.createAccount(accountModel);
        accountModel.setId(accountId);

        return accountModel;
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/delete", method = RequestMethod.POST)
    @ResponseBody
    public void deleteAccount( @PathVariable("accountId") Long accountId ){
        accountDao.deleteAccount( accountId );
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/pools", method = RequestMethod.GET)
    @ResponseBody
    public List<PoolConfigurationModel> getAccountPools(@PathVariable("accountId") Long accountId) {
        return poolDao.readPools(accountId);
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/pools", method = RequestMethod.POST)
    @ResponseBody
    public PoolConfigurationModel createAccountPool(@PathVariable("accountId") Long accountId, @RequestBody String poolSettingJson) {
        Long poolId = poolDao.createPool(accountId, poolSettingJson);
        return poolDao.readPoolById(poolId);
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/pools/{poolId}", method = RequestMethod.POST)
    @ResponseBody
    public boolean updateAccountPool(@PathVariable("accountId") Long accountId, @PathVariable("poolId") Long poolConfigurationId, @RequestBody String newPoolSettingJson) {
        return poolDao.updatePool(poolConfigurationId, accountId, newPoolSettingJson);
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/pools/{poolId}/delete", method = RequestMethod.POST)
    @ResponseBody
    public boolean deleteAccountPool(@PathVariable("accountId") Long accountId, @PathVariable("poolId") Long poolConfigurationId) {
        return poolDao.deletePool(poolConfigurationId, accountId);
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/pools/{poolId}", method = RequestMethod.GET)
    @ResponseBody
    public PoolConfigurationModel getAccountPool(@PathVariable("accountId") Long accountId, @PathVariable("poolId") Long poolConfigurationId) {
        return poolDao.readPoolByIdAndAccountId(poolConfigurationId, accountId);
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/pools/{poolId}/status", method = RequestMethod.GET)
    @ResponseBody
    public PoolStatus getAccountPoolStatus(@PathVariable("accountId") Long accountId, @PathVariable("poolId") Long poolConfigurationId) {
        PoolConfigurationModel poolConfiguration = poolDao.readPoolByIdAndAccountId(poolConfigurationId, accountId);
        return _getPoolStatus(poolConfiguration);
    }

    /**
     * @return poolConfigurationId => poolStatus map with a single entry.
     */
    @RequestMapping(value = "/admin/pools/{poolId}/status", method = RequestMethod.GET)
    @ResponseBody
    public Map<Long, PoolStatus> getPoolStatus(@PathVariable("poolId") Long poolConfigurationId) {
        PoolConfigurationModel poolConfiguration = poolDao.readPoolById(poolConfigurationId);
        HashMap<Long, PoolStatus> resultMap = new HashMap<Long, PoolStatus>();
        resultMap.put(poolConfigurationId, _getPoolStatus(poolConfiguration));
        return resultMap;
    }

    /**
     * @return poolConfigurationId => poolStatus map.
     */
    @RequestMapping(value = "/admin/pools/status", method = RequestMethod.GET)
    @ResponseBody
    public Map<Long, PoolStatus> getPoolsStatus() {
        Map<Long /* poolConfigurationId */, PoolStatus> resultMap = new HashMap<Long, PoolStatus>();
        // get pool status for all pools
        Collection<PoolStatus> poolStatuses = poolManagerApi.listStatuses();
        // map every status found to its pool configuration
        List<PoolConfigurationModel> poolConfigurationModels = poolDao.readPools();
        for (PoolConfigurationModel poolConfiguration : poolConfigurationModels) {
            Long poolConfigurationId = poolConfiguration.getId();

            for (PoolStatus poolStatus : poolStatuses) {
                if (poolStatus.getPoolId().equals(poolConfiguration.getPoolSettings().getUuid())) {
                    resultMap.put(poolConfigurationId, poolStatus);
                }
            }
        }

        return resultMap;
    }

    @RequestMapping(value = "/admin/pools/{poolId}/errors", method = RequestMethod.GET)
    @ResponseBody
    public List<ErrorModel> getPoolErrors(@PathVariable("poolId") Long poolConfigurationId) {
        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        return poolManagerApi.listTaskErrors(poolSettings);
    }

    @RequestMapping(value = "/admin/pools/{poolId}/tasks", method = RequestMethod.GET)
    @ResponseBody
    public List<TaskModel> getPoolTasks(@PathVariable("poolId") Long poolConfigurationId) {
        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        return poolManagerApi.listRunningTasks(poolSettings);
    }

    @RequestMapping(value = "/admin/pools/{poolId}/tasks/{taskId}/delete", method = RequestMethod.POST)
    public void deletePoolTask(@PathVariable("poolId") Long poolConfigurationId, @PathVariable("taskId") Long taskId) {
        // task IDs are currently unique across pools, no need to check for pool id
//        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        poolManagerApi.removeRunningTask(/*poolSettings,*/ taskId);
    }


    @RequestMapping(value = "/admin/accounts/{accountId}/pools/{poolId}/nodes", method = RequestMethod.POST)
    @ResponseBody
    public String addMachine(@PathVariable("accountId") Long accountId, @PathVariable("poolId") Long poolConfigurationId) {
        throw new UnsupportedOperationException("not supported yet!");
//        NodeModel nodeModel = new NodeModel();
//            nodeModel.setPoolUuid(  );
//            poolManagerApi.createNode(  );
    }





    @RequestMapping(value = "/admin/pools/{poolId}/nodes", method = RequestMethod.GET)
    @ResponseBody
    public List<NodeModel> getMachines(@PathVariable("poolId") Long poolConfigurationId) {
        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        return poolManagerApi.listNodes(poolSettings);
    }

    @RequestMapping(value = "/admin/pools/{poolId}/nodes", method = RequestMethod.POST)
    public void addMachine(@PathVariable("poolId") Long poolConfigurationId) {
        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        poolManagerApi.createNode(poolSettings, null);
    }

    @RequestMapping(value = "/admin/pools/{poolId}/nodes/{nodeId}/bootstrap", method = RequestMethod.POST)
    public void nodeBootstrap(@PathVariable("poolId") Long poolConfigurationId, @PathVariable("nodeId") Long nodeId) {
        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        poolManagerApi.bootstrapNode(poolSettings, nodeId, null);
    }

    @RequestMapping(value = "/admin/pools/{poolId}/nodes/{nodeId}/delete", method = RequestMethod.POST)
    public void nodeDelete(@PathVariable("poolId") Long poolConfigurationId, @PathVariable("nodeId") Long nodeId) {
        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        poolManagerApi.deleteNode(poolSettings, nodeId, null);
    }


    @RequestMapping(value = "/admin/accounts/{accountId}/pools/{poolId}/nodes/{nodeId}/bootstrap", method = RequestMethod.POST)
    public void nodeBootstrap(@PathVariable("accountId") Long accountId,
                                @PathVariable("poolId") Long poolConfigurationId, @PathVariable("nodeId") Long nodeId) {
        PoolSettings poolSettings = poolDao.readPoolByIdAndAccountId(poolConfigurationId, accountId).getPoolSettings();
        poolManagerApi.bootstrapNode(poolSettings, nodeId, null);
    }

    @RequestMapping(value = "/admin/accounts/{accountId}/pools/{poolId}/nodes/{nodeId}/delete", method = RequestMethod.POST)
    public void nodeDelete(@ModelAttribute("poolSettings") PoolSettings poolSettings, @PathVariable("nodeId") Long nodeId) {
        poolManagerApi.deleteNode(poolSettings, nodeId, null);
    }



    @RequestMapping(value = "/admin/pools/{poolId}/cloud/nodes", method = RequestMethod.GET)
    @ResponseBody
    public List<NodeMappings> getCloudNodes(@PathVariable("poolId") Long poolConfigurationId) {
        PoolSettings poolSettings = poolDao.readPoolById(poolConfigurationId).getPoolSettings();
        return poolManagerApi.listCloudNodes(poolSettings);
    }





    @ModelAttribute("account")
    public AccountModel getUser(HttpServletRequest request) {
        return (AccountModel) request.getAttribute("account");
    }

    @ModelAttribute("poolSettings")
    public PoolSettings getPoolSettings( /*@PathVariable("accountId") Long accountId,
                                         @PathVariable("poolId") Long poolId */ HttpServletRequest request) {
//        return getAccountPool( accountId, poolId ).getPoolSettings();
        Map pathVariables = (Map) request.getAttribute("org.springframework.web.servlet.HandlerMapping.uriTemplateVariables");
        if (pathVariables.containsKey("accountId") && pathVariables.containsKey("poolId")) {
            long accountId = Long.parseLong((String) pathVariables.get("accountId"));
            long poolId = Long.parseLong((String) pathVariables.get("poolId"));
            return poolDao.readPoolByIdAndAccountId(poolId, accountId).getPoolSettings();
        } else {
            return null;
        }
    }


    private PoolStatus _getPoolStatus(PoolConfigurationModel poolConfiguration) {
        PoolStatus retValue = null;
        if (poolConfiguration != null) {
            PoolSettings poolSettings = poolConfiguration.getPoolSettings();
            if (poolSettings != null) {
                retValue = poolManagerApi.getStatus(poolSettings);
            }
        }
        return retValue;
    }
}