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
            "keyPairName": "__keyPairName__" 
        } 
    }
}
```
