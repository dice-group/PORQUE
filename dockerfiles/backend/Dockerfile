FROM maven:3.6.3-jdk-11 AS MAVEN_BUILD
WORKDIR /build
COPY backend/pom.xml porque-backend/pom.xml
COPY backend/src porque-backend/src

WORKDIR /build/porque-backend
RUN mvn package

FROM tomcat:8.5.37-jre11
EXPOSE 8080
COPY --from=MAVEN_BUILD /build/porque-backend/target/PORQUE.war /usr/local/tomcat/webapps
RUN /usr/local/tomcat/bin/catalina.sh start
