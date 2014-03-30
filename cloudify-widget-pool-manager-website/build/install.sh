# use this script by running
# yum -y install dos2unix && wget --no-cache --no-check-certificate -O - http://get.gsdev.info/cloudify-widget-pool-manager-website/1.0.0/install.sh | dos2unix | bash

install_main(){

    SYSCONFIG_FILE=/etc/sysconfig/widget-pool-manager
    if [ ! -f $SYSCONFIG_FILE ]; then
        echo "missing file $SYSCONFIG_FILE"
        exit 1
    fi

    CURRENT_DIRECTORY=`pwd`
    cd "$(dirname "$0")"

    eval "`wget --no-cache --no-check-certificate -O - http://get.gsdev.info/gsat/1.0.0/install_gsat.sh | dos2unix`"


    echo "reading sysconfig"
    SYSCONFIG_FILE=widget-pool-manager read_sysconfig
    check_exists INSTALL_LOCATION

    install_nginx

    install_java

    install_mysql

    upgrade_main

    cd $CURRENT_DIRECTORY

    service widget-pool
}

download_pool_manager(){
    echo "downloading pool manager"
    URL=http://get.gsdev.info/cloudify-widget-pool-manager-website/1.0.0/cloudify-widget-pool-manager-website-1.0.0.tar

    TAR_FILENAME=cloudify-widget-pool-manager-website-1.0.0.tar
    wget --no-check-certificate "$URL" -O $TAR_FILENAME


    mkdir -p $INSTALL_LOCATION
    tar -xf $TAR_FILENAME -C $INSTALL_LOCATION

    chmod +x $INSTALL_LOCATION/**/*.sh
    dos2unix $INSTALL_LOCATION/**/*.sh


}



upgrade_main(){
    echo "installing gsat"
    eval "`wget --no-cache --no-check-certificate -O - http://get.gsdev.info/gsat/1.0.0/install_gsat.sh | dos2unix`"
    SYSCONFIG_FILE=widget-pool-manager read_sysconfig

    echo "installing the JAR file"
    download_pool_manager

    # create the DB and upgrade it. if already exists it will just upgrade it.

    DB_USER=$DB_ADMIN
    DB_PASSWORD=$DB_ADMIN_PASSWORD


    echo "creating DB"
    UPGRADE_TO=create

    BASEDIR=$INSTALL_LOCATION/manager-schema
    migrate_db

    BASEDIR=$INSTALL_LOCATION/website-schema
    migrate_db


    echo "migrating dbs"
     UPGRADE_TO=latest
     BASEDIR=$INSTALL_LOCATION/manager-schema
     migrate_db

     BASEDIR=$INSTALL_LOCATION/website-schema
     migrate_db


    dos2unix $INSTALL_LOCATION/build/nginx.conf
    source $INSTALL_LOCATION/build/nginx.conf | dos2unix > /etc/nginx/sites-enabled/widget-pool-manager.conf

    echo "installing service script under widget-pool"
    SERVICE_NAME=widget-pool SERVICE_FILE=$INSTALL_LOCATION/build/service.sh install_initd_script

    if [ -z $SYSCONFIG_UPGRADE_URL ]; then
        echo "no SYSCONFIG_UPGRADE_URL defined. skipping"
    else
        echo "upgrading sysconfig file using url $SYSCONFIG_UPGRADE_URL"
        run_wget -O $SYSCONFIG_FILE  $SYSCONFIG_UPGRADE_URL
        dos2unix $SYSCONFIG_FILE
        chmod +x $SYSCONFIG_FILE
    fi

}


set -e
if [ "$1" = "upgrade" ];then
    echo "running upgrade"
    upgrade_main $*
else
    echo "running install"
    install_main $*
fi
set +e

