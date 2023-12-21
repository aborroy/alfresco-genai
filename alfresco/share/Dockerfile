ARG SHARE_TAG
FROM alfresco/alfresco-share:${SHARE_TAG}

ARG TOMCAT_DIR=/usr/local/tomcat

# Server data
ARG SERVER_NAME

# Install modules and addons
RUN mkdir -p $TOMCAT_DIR/amps
COPY modules/amps $TOMCAT_DIR/amps
COPY modules/jars $TOMCAT_DIR/webapps/share/WEB-INF/lib

RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
    $TOMCAT_DIR/amps $TOMCAT_DIR/webapps/share -directory -nobackup -force

# Increase default cacheMaxSize
RUN sed -i "s|<Context>|<Context>\n<Resources cacheMaxSize=\"51200\"/> |g" $TOMCAT_DIR/conf/context.xml