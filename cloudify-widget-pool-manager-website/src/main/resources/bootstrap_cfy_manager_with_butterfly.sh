#!/bin/bash
set -e

sudo wget "https://s3.amazonaws.com/cloudify-widget-bootstrap/ops+provision+script/bootstrap_script.sh"

source bootstrap_script.sh

wget "https://github.com/cloudify-cosmo/cloudify-nodecellar-example/archive/3.1-build.zip" -O blueprint.zip

echo "#################### update and clean source ######################"
sudo apt-get update
sudo apt-get clean
echo "#################### force installation ######################"
sudo apt-get -f -y install

echo "#################### dpkg something ######################"
sudo dpkg --configure -a
sudo apt-get -f -y install

echo "#################### installing zip unzip ######################"
sudo apt-get -y install zip unzip
echo "#################### running unzip ######################"
unzip -n blueprint.zip

cd cloudify-nodecellar-example-3.1-build
echo "#################### uploading nodecellar blueprint ######################"
cfy blueprints upload -b nodecellar1 -p singlehost-blueprint.yaml -v

echo "#################### Starting butterfly ######################"
sudo pip install https://github.com/LironHazan/butterfly/archive/master.zip

nohup butterfly.server.py --host="0.0.0.0" --port=8080 --prompt_login=False --unsecure &

sleep 10

echo successfully
