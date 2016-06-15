#ipm-ws
Provides the functionality needed to identify files of interest based on metadata query 
and filtering and to download a manifest of files and directory locations. 
It also provides notification mechanisms based on <b>tcga-expedition</b> 
RDF graph or relational metadata store.

#System Requirements
Tomcat 7.x running on Java 7 or greater.

#Installation
1. Install <b>ipm</b> from https://github.com/TCGAExpedition/ipm
2. Put war/ipm-ws.war on your Tomcat web server.
3. Set <b><i>'useVitOrPostgre'</i></b> parameter in resources/jQueryPostgres.conf.
4. Set connction parameters in <i>jQueryPostgres.conf</i> or in <i>jQueryVrt.conf</i> based on your 
<b>tcga-expedition</b> data store selection. 
5. Copy <i>jQueryPostgres.conf</i> to $CATALINA_HOME/conf directory.
6. If you use Virtuoso, copy <i>jQueryVrt.conf</i> file must be in $CATALINA_HOME/conf directory as well.
7. Start the server


#Project Dependencies
 - [ipm](https://github.com/TCGAExpedition/ipm)
 - [tcga-expedition](https://github.com/TCGAExpedition/tcga-expedition)

