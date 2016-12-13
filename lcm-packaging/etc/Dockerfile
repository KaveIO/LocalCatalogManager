############################################################
# Dockerfile to build LCM container images
# Based on Ubuntu
############################################################
FROM       ubuntu:latest
MAINTAINER Docker

# Set key and repo for mongo
ENV DEBIAN_FRONTEND=noninteractive
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections


RUN apt-get update && apt-get install -y git software-properties-common maven 
RUN apt-get update -y && apt-get upgrade -y

# Set key and repo for java 8
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get update
RUN apt-get install -y oracle-java8-set-default

RUN mkdir $HOME/workspace
WORKDIR $HOME/workspace

ARG username
ARG password
ARG branch=master

RUN git clone -b $branch https://$username:$password@github.com/DataAnalyticsOrganization/LocalCatalogManager.git
#RUN git clone git@github.com:DataAnalyticsOrganization/LocalCatalogManager.git
WORKDIR $HOME/workspace/LocalCatalogManager
RUN mvn install -DskipTests=true

WORKDIR /tmp
RUN echo "Header0,Header1,Header2,Header3,Header4" >>  temp.csv && echo "00,01,02,03,04" >> temp.csv && echo "10,11,12,13,14" >> temp.csv && echo "20,21,22,23,24" >> temp.csv && echo "30,31,32,33,34" >> temp.csv && echo "40,41,42,43,44" >> temp.csv && echo "50,51,52,53,54" >> temp.csv && echo "60,61,62,63,64" >> temp.csv && echo "70,71,72,73,74" >> temp.csv && echo "80,81,82,83,84" >> temp.csv && echo "90,91,92,93,94" >> temp.csv


##################### INSTALLATION END #####################

WORKDIR $HOME/workspace/LocalCatalogManager/lcm-server/
CMD git pull && sed -ie "s/^lcm.server.securePort=.*/lcm.server.securePort=$SSL_PORT/" src/main/resources/application.properties && sed -ie "s/^lcm.server.port=.*/lcm.server.port=$PORT/" src/main/resources/application.properties && sed -ie "s/^lcm.server.storage.mongo.port=.*/lcm.server.storage.mongo.port=$LCM_MONGO_PORT/" src/main/resources/application.properties && sed -ie "s/^lcm.server.storage.mongo.host=.*/lcm.server.storage.mongo.host=$LCM_MONGO/" src/main/resources/application.properties && mvn install -DskipTests=true && mvn "-Dexec.args=-classpath %classpath nl.kpmg.lcm.server.Main" -Dexec.executable=java org.codehaus.mojo:exec-maven-plugin:1.2.1:exec >> $HOME/lcm-server.log 2>&1



# Just to keep the container running 
#CMD tail -f /dev/null 

