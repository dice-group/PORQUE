FROM maven:3.6.3-jdk-11 AS MAVEN_BUILD
WORKDIR /build
COPY backend/pom.xml backend-server/pom.xml
COPY backend/src backend-server/src

WORKDIR /build/backend-server
RUN mvn package

FROM tomcat:8.5.37-jre11
EXPOSE 8080
COPY --from=MAVEN_BUILD /build/backend-server/target/PORQUE.war /usr/local/tomcat/webapps
RUN /usr/local/tomcat/bin/catalina.sh start
