############################################################
# Dockerfile to build LCM container images
# Based on Ubuntu
############################################################
FROM       ubuntu:latest
MAINTAINER lcm

# Set key and repo for mongo
ENV DEBIAN_FRONTEND=noninteractive
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections


RUN apt-get update -y && \
    apt-get upgrade -y && \
    apt-get install -y software-properties-common && \ 
    add-apt-repository -y ppa:webupd8team/java && \
    apt-get update -y && \
    apt-get install -y oracle-java8-set-default

RUN mkdir /root/workspace
WORKDIR /root/workspace

ADD lcm-packaging/target/lcm-complete-*.tar.gz /root/workspace/
RUN ln -s lcm-complete-*-SNAPSHOT/ lcm-complete

ADD docker/config/application.properties lcm-complete/config/

COPY docker/entrypoint.sh .

EXPOSE 8081 8080
ENTRYPOINT ["./entrypoint.sh"]

##################### INSTALLATION END #####################

