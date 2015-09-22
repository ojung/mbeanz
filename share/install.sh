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

cp $dir/mbeanz.plist /System/Library/LaunchDaemons
