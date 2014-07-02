source /etc/sysconfig/widget-pool-manager

cd $INSTALL_LOCATION/build
VM_ARGS="-Dspring.profiles.active=manager-app"
VM_ARGS="$VM_ARGS -DwebsiteMeContext=file://$INSTALL_LOCATION/build/me-context.xml"

java $VM_ARGS  -classpath "../website-1.0.0.jar:../lib/*"  cloudify.widget.website.initializer.EmbeddedJetty

echo $! > /var/run/poolmanager.pid