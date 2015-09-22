#!/bin/bash

dir=$(cd "$(dirname "${BASH_SOURCE[0]}" )" && pwd)

install_dir=/opt/mbeanz
config_dir=/etc/mbeanz

mkdir -p $install_dir
cp $dir/start-mbeanz-api $install_dir
cp $dir/mbeanz.jar $install_dir

mkdir -p $config_dir
cp $dir/config $config_dir
chmod +x $install_dir/start-mbeanz-api

launchd_dir=/System/Library/LaunchDaemons
cp $dir/mbeanz.plist $launchd_dir
launchctl unload $launchd_dir/mbeanz.plist
launchctl load $launchd_dir/mbeanz.plist

pip install -r $dir/requirements.txt
cp $dir/../mbeanz/mbeanz /usr/local/bin
