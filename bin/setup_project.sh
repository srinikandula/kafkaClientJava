#!/bin/bash
#
KAFKA_HOME=~/kafka_2.11-1.1.0

if [ -d "$KAFKA_HOME" ]; then
  echo "Looks like kafka is already installed"
else
    if [ -e "$HOME/kafka_2.11-1.1.0.tgz" ]; then
        echo "Kafka is already downloaded"
    else
        echo "downloaing kafka..."
        wget http://www-us.apache.org/dist/kafka/1.1.0/kafka_2.11-1.1.0.tgz --directory-prefix=$HOME/
    fi
  tar xvf ~/kafka_2.11-1.1.0.tgz -C $HOME/
  export KAFKA_HOME=$HOME/kafka_2.11-1.1.0
fi



#Install Oracle JDK

sudo add-apt-repository ppa:webupd8team/java

sudo apt update; sudo apt install oracle-java8-installer


#Install maven
wget http://mirrors.wuchna.com/apachemirror/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.zip
sudo apt install unzip
unzip apache-maven-3.5.4-bin.zip


cd kafkaClient
#modify application.properties to set mongodb IP
mvn package


# Create a file in '/etc/systemd/system'

#!/bin/bash
#
[Unit]
Description=Java Service

[Service]
User=nobody
# The configuration file application.properties should be here:
WorkingDirectory=/home/ubuntu/kafkaClientJava
ExecStart=/usr/bin/java -jar target/kafkaClientJava-0.1.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5
StandardOutput=syslog               # Output to syslog
StandardError=syslog                # Output to syslog
SyslogIdentifier=kafkaClientJava

[Install]
WantedBy=multi-user.target


#Run the commands in startZookeeper.sh

sudo systemctl stop kafkaClientJava.service
sudo systemctl start kafkaClientJava.service
