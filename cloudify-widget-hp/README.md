```json
{
    "name": "--- hp grizzly ---", 
    "authKey": "---key---", 
    "maxNodes": 3, 
    "minNodes": 1, 
    "nodeManagement": { 
        "mode": "MANUAL", 
        "activeModules": [ 
            "CREATE", 
            "BOOTSTRAP", 
            "DELETE", 
            "DELETE_EXPIRED" 
        ], 
        "emailSettings": { 
        	"turnedOn": "false", 
        	"recipients": [ 
        		"me@myself.com" 
        	] 
        } 
    }, 
    "bootstrapProperties": { 
        "publicIp": "", 
        "privateIp": "", 
        "cloudifyUrl": "http://repository.cloudifysource.org/org/cloudifysource/community/gigaspaces-cloudify-2.7.0-ga-b5996.zip", 
        "preBootstrapScript": "", 
        "recipeUrl": "", 
        "recipeRelativePath": "" 
    }, 
    "provider": { 
        "name": "__name__", 
        "connectDetails": { 
            "project": "__project__", 
            "key": "__key__", 
            "secretKey": "__secretKey__", 
            "apiVersion": "__apiVersion__", 
            "region" : "__region__", 
            "identityEndpoint": "__identityEndpoint__", 
    	    "sshPrivateKey": "__sshPrivateKey__" 
        }, 
        "machineOptions": { 
            "mask": "test-hp", 
            "machinesCount": 1, 
            "hardwareId": "__hardwareId__", 
            "imageId": "__imageId__", 
            "description" : "", 
            "securityGroup": "default", 
            "networkUuid": "__networkUuid__", 
            "keyPairName": "__keyPairName__",
            "username" : "__image_default_username_for_ssh_purposes__"
        } 
    }
}
```

Options
---

All values are to be extracted from the HP Project Management Dashboard.

**provider.name**

The pool name.

**provider.connectDetails.project**

```Identity``` -> ```Projects``` -> ```Project ID``` for the relevant project.

**provider.connectDetails.key**

The HP cloud login user name, should be in the format ```[first-name]@hpcloud-paid```.

**provider.connectDetails.secretKey**

The HP cloud login password.

**provider.connectDetails.apiVersion**

Put in the value ```2```. 

**provider.connectDetails.region**

US WEST is ```region-a.geo-1```.  
US EAST is ```region-b.geo-1```.

**provider.connectDetails.identityEndpoint**

US WEST is ```https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3/auth```  
US EAST is ```https://region-b.geo-1.identity.hpcloudsvc.com:35357/v3/auth```

This can be also extracted from your ```User Roles and API Endpoints``` but note that the link might not contain ```/auth``` so append it if it is missing.

**provider.connectDetails.sshPrivateKey**

This should be created.  
Navigate to ```Project``` -> ```Compute``` -> ```Access & Security``` -> ```Key Pairs``` tab  
Click on ```Create Key Pair``` and give it a name and download the key pair ```pem``` file.  
Edit the file and replace all new lines with ```\n```.  
The one-line key string result is the value for this property. 

**provider.machineOptions.hardwareId**

* 100 = ```standard.xsmall```  
* 101 = ```standard.small```  
* 102 = ```standard.medium```  
* 103 = ```standard.large```  
* 104 = ```standard.xlarge```  
* 105 = ```standard.2xlarge```  
* 110 = ```standard.4xlarge```  
* 114 = ```standard.8xlarge```  
* 203 = ```highmem.large```  
* 204 = ```highmem.xlarge```  
* 205 = ```highmem.2xlarge```  

**provider.machineOptions.imageId**

Navigate to ```Project``` -> ```Compute``` -> ```Images``` -> Click on image.  
The ID is the value of this property.

**provider.machineOptions.networkUuid**

Your network UUID.  
```Project``` -> ```Network``` -> ```Networks``` -> network details for your network -> ```network overview```  
The ID property is the value of this property.

**provider.machineOptions.keyPairName**

Your Key Pair name that matches the ```provider.connectDetails.sshPrivateKey```. 
```Project``` -> ```Compute``` -> ```Access & Security``` -> ```Key Pair Name```  
