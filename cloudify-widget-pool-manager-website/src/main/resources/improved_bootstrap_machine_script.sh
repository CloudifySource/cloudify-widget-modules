#! /bin/bash

#########  IMPORTANT  ########  IMPORTANT  #########  IMPORTANT  ##############
#
#  ***  THIS IS NOT TO BE USED AS THE DEFAULT BOOTSTRAP SCRIPT  ***
#
#  THIS IS ONLY A VARIATION OF THE DEFAULT SCRIPT THAT ALSO INSTALLS XAP
#  THE DEFAULT SCRIPT IS "bootstrap_machine.sh"
#
#########  IMPORTANT  ########  IMPORTANT  #########  IMPORTANT  ##############


########################################
#  This script uses placeholders in the format ##placeholder## that are injected from widget configuration
#
#  The available placeholders are:
#      publicip - the cloudify's public ip. if not given, cloudify will show 127.0.0.1 as public ip as it is localcloud
#      privateip - cloudify's private ip. if not given, cloudify will show 127.0.0.1 as private ip as it is localcloud
#      installNode - if "true" then we install node
#      cloudifyUrl - the URL download cloudify from
#      prebootstrapScript - a string of code to run before bootstrap
#      recipeUrl - a url to a zip file containing a recipe to be installed on bootstrap. skipped if not present.
#      recipeRelativePath - a relative URL inside the zip file to reach the recipe
#      recipeDownloadMethod - wget (default) or s3 (requires more data) to download the recipe's zip file
#
#
###########################################

CLOUDIFY_HOMEDIR_CONF=/etc/cloudify/homedir
CLOUDIFY_FOLDER=`cat $CLOUDIFY_HOMEDIR_CONF`

echo "cloudify folder is $CLOUDIFY_FOLDER"

DOWNLOAD_RECIPE_ROOT="/tmp/download_recipe"
RECIPE_URL="https://github.com/CloudifySource/cloudify-recipes/archive/master.zip"
RECIPE_RELATIVE_PATH="cloudify-recipes-master/apps/xap9x-tiny/"

init(){
    echo "Open firewall ports"
    # iptables -A INPUT -i eth0 -p tcp -m multiport --dports 22,80,443,8080,9000,8100,8099 -m state --state NEW,ESTABLISHED -j ACCEPT

    # iptables -A OUTPUT -o eth0 -p tcp -m multiport --sports 80,443,8080,9000,8100,8099 -m state --state ESTABLISHED -j ACCEPT

    service iptables save
    /etc/init.d/iptables restart

    echo add hostname to /etc/hosts
    echo "127.0.0.1 `hostname`" >> /etc/hosts

    echo Setting sudo privileged mode
    sudo sed -i 's/^Defaults.*requiretty/#&/g' /etc/sudoers
}

install_java(){    
    echo "JAVA_HOME is $JAVA_HOME"
    CLOUDIFY_AGENT_ENV_PUBLIC_IP=##publicip##
    CLOUDIFY_AGENT_ENV_PRIVATE_IP=##privateip##
    # http://repository.cloudifysource.org/org/cloudifysource/2.7.0-5985-M3/gigaspaces-cloudify-2.7.0-M3-b5985.zip
    JAVA_64_URL="http://repository.cloudifysource.org/com/oracle/java/1.6.0_32/jdk-6u32-linux-x64.bin"
    if [ ! -z "$JAVA_HOME" ]; then
       echo "Java file already exists. not installing java"
       echo "JAVA_HOME is $JAVA_HOME"
    else
        rm -rf jdk1.6.0_32/
        echo Downloading JDK from $JAVA_64_URL
        wget -q -O ~/java.bin $JAVA_64_URL
        chmod +x ~/java.bin
        echo -e "\n" > ~/input.txt

        echo Installing JDK
        ./java.bin < ~/input.txt > /dev/null
        rm -f ~/input.txt
        rm -f ~/java.bin

        echo Exporing JAVA_HOME
        # export JAVA_HOME="`pwd`/jdk1.6.0_32"
        # export PATH=$PATH:$JAVA_HOME/bin
        echo "export JAVA_HOME=`pwd`/jdk1.6.0_32" >> ~/.bashrc
        echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
        source ~/.bashrc
    fi

}


install_node(){
    if [ -f /usr/bin/node ]; then
      echo "node already installed"
    else
        NAVE=/opt/nave/nave.sh
        yum install -y dos2unix

        if [ ! -f $NAVE ]; then
            echo "downloading nave"
            mkdir -p /opt/nave
            wget -O $NAVE "https://raw.github.com/isaacs/nave/master/nave.sh"
            dos2unix $NAVE
            chmod +x $NAVE
        fi
        chmod 755 $NAVE
        $NAVE install 0.10.18
        ln -Tfs ~/.nave/installed/0.10.18/bin/node /usr/bin/node
        ln -Tfs ~/.nave/installed/0.10.18/bin/npm /usr/bin/npm
    fi
}

install_cloudify(){
    echo "installing cloudify"
    CLOUDIFY_URL="https://s3-eu-west-1.amazonaws.com/gigaspaces-repository-eu/org/cloudifysource/2.7.1-6300-RELEASE/gigaspaces-cloudify-2.7.1-ga-b6300.zip"

    mkdir -p /etc/cloudify
    echo "saving url to file"
    echo $CLOUDIFY_URL > /etc/cloudify/url
    if [ -f $CLOUDIFY_HOMEDIR_CONF ];then
        echo "cloudify already downloaded and is at `cat $CLOUDIFY_HOMEDIR_CONF`"
    else
        echo Downloading cloudify installation from $CLOUDIFY_URL

        wget --no-check-certificate "$CLOUDIFY_URL" -O ~/cloudify.zip
        if [ $? -ne 0 ]; then
            echo "Failed downloading cloudify installation"
            exit 1
        fi

        unzip ~/cloudify.zip > /dev/null


        CLOUDIFY_FOLDER="`pwd`/`ls ~ | grep giga`"
        echo "saving cloudify home [$CLOUDIFY_FOLDER] to file"
        echo $CLOUDIFY_FOLDER > $CLOUDIFY_HOMEDIR_CONF


        rm -f ~/cloudify.zip

        echo "run prebootstrap script"
        wget --no-check-certificate "https://raw.github.com/CloudifySource/cloudify-widget/master/conf/cloudify/webui-context.xml"   -O ${CLOUDIFY_FOLDER}/config/cloudify-webui-context-override.xml
        echo Starting Cloudify bootstrap-localcloud `hostname -I`

        # -nic-address `hostname -I`"
    fi
}

bootstrap_localcloud(){
    source ~/.bashrc
    echo "installing cloudify"

    CLOUDIFY_FOLDER=`cat $CLOUDIFY_HOMEDIR_CONF`

    echo "killing all java and starting cloudify"
    killall -9 java

    ${CLOUDIFY_FOLDER}/bin/cloudify.sh "bootstrap-localcloud"
}


download_recipe_wget(){
     mkdir -p $DOWNLOAD_RECIPE_ROOT
    if [ ! -f $DOWNLOAD_RECIPE_ROOT/recipe.zip ]; then
        echo "download recipe using wget method"
        wget -q -O $DOWNLOAD_RECIPE_ROOT/recipe.zip "$RECIPE_URL"

    else
        echo "recipe file already exists, nothing to do"
    fi
}




 install_recipe(){
    source ~/.bashrc
    echo "RECIPE_RELATIVE_PATH is ($RECIPE_RELATIVE_PATH)"

    if [ -f $DOWNLOAD_RECIPE_ROOT/recipe.zip ];then
        echo "found recipe.zip file, installing it"

        cd "$DOWNLOAD_RECIPE_ROOT"
        unzip -o recipe.zip


        echo "going into $RECIPE_RELATIVE_PATH to install"
        cd "$RECIPE_RELATIVE_PATH"
        echo "I am at `pwd` and I am invoking install command on localhost"
        ${CLOUDIFY_FOLDER}/bin/cloudify.sh "connect http://localhost:8100; install-application  -disableSelfHealing --verbose -timeout 200 ."

    else
        echo "not installing recipe [$INSTALL_RECIPE]"
    fi
}

init

install_java &
RUN_INSTALL_JAVA=$!

install_node &
RUN_INSTALL_NODE=$!

install_cloudify &
RUN_INSTALL_CLOUDIFY=$!

download_recipe_wget &
RUN_DOWNLOAD_RECIPE=$!

wait $RUN_INSTALL_JAVA
wait $RUN_INSTALL_NODE
wait $RUN_INSTALL_CLOUDIFY
wait $RUN_DOWNLOAD_RECIPE

echo ""
echo ""
echo ""
echo "finished waiting for all, will start to bootstrap localcloud"
echo ""
echo ""
echo ""


bootstrap_localcloud
install_recipe


# cat nohup.out
exit 0
