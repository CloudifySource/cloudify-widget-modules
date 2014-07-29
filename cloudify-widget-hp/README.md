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

* ```f8c11f9a-436f-4d65-ac4f-8638984c5f25``` = ```CentOS 5.8 Server 64-bit 20120828 (b) (291.3 MB)```  
* ```202e7659-f7c6-444a-8b32-872fe2ed080c``` = ```CentOS 6.3 Server 64-bit 20130116 (310.0 MB)```  
* ```72618630-16f1-4c22-a227-8ed012e63c3f``` = ```Debian Wheezy 7.4 Server 64-bit 20140411 (233.9 MB)```  
* ```f31f02b8-afa2-4991-a17a-422c68847ad7``` = ```SUSE Linux Enterprise Server 11 SP3 20130820 (b) (417.8 MB)```  
* ```37da5135-8814-46ed-b7f3-3064e5c0772d``` = ```Ubuntu Lucid 10.04 LTS Server 64-bit 20130909.hp2 (b) (233.6 MB)```  
* ```1c556fd2-2b05-42e2-8745-73f261b876fe``` = ```Ubuntu Precise 12.04.4 LTS Server 64-bit 20140414 (Rescue Image) (249.9 MB)```  
* ```1294610e-fdc4-579b-829b-d0c9f5c0a612``` = ```Windows Server 2008 Enterprise SP2 x64 Volume License 20140415 (b) (6.5 GB)```  
* ```8a398ff7-f4f7-5beb-bab0-300c47345080``` = ```Windows Server 2008 Enterprise SP2 x86 Volume License 20140415 (b) (4.7 GB)```  
* ```e08ecab2-4acd-540d-b16a-60284769aceb``` = ```Windows Server 2008 R2 Enterprise SP1 x64 Volume License 20140415 (b) (5.2 GB)```  
* ```d782c03d-9666-4639-a62f-53b376880120``` = ```ActiveState Stackato v3.2.1 (5.3 GB)```  
* ```0c8a7e05-7c50-52eb-9c13-2a9135a9890c``` = ```ActiveState Stackato-v3.0.1 - Partner Image (4.9 GB)```  
* ```9c0bac77-9764-4b56-a8e9-426dea720688``` = ```CohesiveFT VNS3 3.0.4 Free Edition - PAYG (1000.1 MB)```  
* ```f39dc898-d26a-4168-a65c-abbff3007ed2``` = ```CohesiveFT VNS3 3.0.4 UL - BYOL (1000.0 MB)```  
* ```f05e0321-a420-4766-9b25-13b31e7b08e2``` = ```CohesiveFT VNS3 3.5 Free Edition - PAYG (2.1 GB)```  
* ```218b1158-8ca4-4660-85fa-e733188b375a``` = ```Debian Wheezy 7.6 64-bit 20140720 - Partner Image (415.9 MB)```  
* ```46fc6417-55fc-459e-8104-871442f7f8e2``` = ```Fedora 19 Server 64-bit 20140407 - Partner Image (228.4 MB)```  
* ```831fa6a5-1ca5-42ea-bd41-4cbebf01085a``` = ```Fedora 20 Server 64-bit 20140407 - Partner Image (201.1 MB)```  
* ```4f5277ae-2167-5173-bef9-cc66d9d4c37b``` = ```RightImage_CentOS_6.5_x64_v13.5.2 - Partner Image (2.2 GB)```  
* ```3593085c-1be1-5c1a-8dcc-0e4f2af98e41``` = ```RightImage_Ubuntu_12.04_x64_v13.5.2 - Partner Image (1.9 GB)```  
* ```82e3efcd-50b2-4527-bee4-08f234d8df0c``` = ```SOASTA CloudTest - Maestro or Results Service, v2 (2.6 GB)```  
* ```0f0a56d9-6d5f-4caa-8b85-6feb55756b4a``` = ```SOASTA TestResultService 1.0 - Partner Image (1.0 GB)```  
* ```c5607a33-2f36-47c1-b667-3d98ddbbf8fa``` = ```Ubuntu Server 12.04 LTS (amd64 20140606) - Partner Image (248.7 MB)```  
* ```261844b3-479c-5446-a2c4-1ea95d53b668``` = ```Ubuntu Server 12.04.2 LTS (amd64 20130318) - Partner Image (239.9 MB)```  
* ```7f7f3b70-316d-4f49-ac7c-c62d6abc35f9``` = ```Ubuntu Server 13.10 (amd64 20131030) - Partner Image (231.4 MB)```  
* ```9d7d22d0-7d43-481f-a7eb-d93ea2791409``` = ```Ubuntu Server 13.10 (amd64 20140409.1) - Partner Image (236.6 MB)```  
* ```17ae0d65-e69a-46d3-a15f-da7aed3aefd2``` = ```Ubuntu Server 14.04.1 LTS (amd64 20140724) - Partner Image (252.0 MB)```  
* ```32b40af5-7a66-47e0-894f-51c5883ea3f5``` = ```BLU AGE Modernization Cobol2SpringMVC (16.8 GB)```  
* ```5e7b4352-46f2-4add-8f5a-758eeb1ed062``` = ```Leostream Cloud Desktops 2.0 - Windows Server 2008 R2 x64 - Partner Image (15.3 GB)```  
* ```18a78e14-0f4f-4af8-85fd-245ea407d3a0``` = ```desktopsites Konect Elite Version 8.6.2.100 (20140211) (11.9 GB)```  

**provider.machineOptions.networkUuid**

Your network UUID.  
```Project``` -> ```Network``` -> ```Networks``` -> network details for your network -> ```network overview```  
The ID property is the value of this property.

**provider.machineOptions.keyPairName**

Your Key Pair name that matches the ```provider.connectDetails.sshPrivateKey```. 
```Project``` -> ```Compute``` -> ```Access & Security``` -> ```Key Pair Name```  
