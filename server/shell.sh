#!/bin/bash

export MIRTH_HOME=`dirname $0`
cd $MIRTH_HOME

address=https://127.0.0.1:8443 
username=admin
password=admin
version=0.0.0

while getopts "a:u:p:v:" opt
do
	case $opt in
	a) address=$OPTARG ;;
	u) username=$OPTARG ;;
	p) password=$OPTARG ;;
	v) version=$OPTARG ;;
	esac
done

java \
	-jar $MIRTH_HOME/shell-launcher.jar \
	$MIRTH_HOME/shell-launcher.xml \
	-a $address \
	-u $username \
	-p $password \
	-v $version