FROM openjdk:7-alpine

# An MSc student at UQAC - Université du Quebec à Chicoutimi
MAINTAINER Demetrio Guilardi <demetrio_guilardi-silva1@uqac.ca>

#################################
########## ENVIRONMENT ##########
#################################

# Install jdk8
# The default sdk 7 will be overwritten by the version 8
# DaCapo needs both java versions, 7 and 8. Check benchmarks/dacapo.properties.
RUN apk add --no-cache openjdk8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH ${JAVA_HOME}:$PATH

# Install ant
ENV ANT_VERSION 1.10.7
RUN cd && \
    wget -q http://www.us.apache.org/dist//ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz && \
    tar -xzf apache-ant-${ANT_VERSION}-bin.tar.gz && \
    mv apache-ant-${ANT_VERSION} /opt/ant && \
    rm apache-ant-${ANT_VERSION}-bin.tar.gz
ENV ANT_HOME /opt/ant
ENV PATH ${PATH}:/opt/ant/bin



#########################################
########## DaCapo dependencies ##########
#########################################

# Install svn
RUN apk add --update subversion curl && rm -rf /var/cache/apk/*

# Install cvs and other basic tools for it to work
RUN apk add cvs openssl busybox-extras

# Install mercurial
RUN apk add --no-cache --update mercurial

# Install git
RUN apk add git

# Copy folders
COPY ./tools /usr/local/tools
COPY ./regression /usr/local/regression
COPY ./benchmarks /usr/local/benchmarks
COPY ./benchmarks/docker.properties /usr/local/benchmarks/local.properties

# Copy files
COPY ./LICENSE /usr/local/LICENSE
COPY ./README.md /usr/local/README.md
COPY ./.gitlab-ci.yml /usr/local/.gitlab-ci.yml
COPY ./Dockerfile /usr/local/Dockerfile
COPY ./.git /usr/local/.git
COPY ./.gitignore /usr/local/.gitignore
COPY ./.dockerignore /usr/local/.dockerignore


##################################################
########## Image X DaCapo compatibility ##########
##################################################

# Install bash
# DaCapo runs on /bin/bash while the image runs on /bin/sh
RUN apk add bash

# Update patch
# DaCapo runs the patch command with the "-l" param
# which is unsuported by the Alpine's BusyBox
# This will install the default GNU patch command
RUN apk add --update patch
