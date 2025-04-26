FROM tomcat:9.0-jdk14-openjdk-oracle

# Eliminar la aplicación por defecto de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el archivo WAR generado al directorio webapps de Tomcat
COPY target/prueba-1.0.war /usr/local/tomcat/webapps/ROOT.war

# Puerto expuesto
EXPOSE 8080

# Comando para iniciar Tomcat
CMD ["catalina.sh", "run"]
