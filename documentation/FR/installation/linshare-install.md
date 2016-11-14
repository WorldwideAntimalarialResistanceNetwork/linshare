##Sommaire

###INSTALLATION LINSHARE

#### 1. [Installation minimale de Linshare](#install-min)
   * [Téléchargement de LinShare](#dlLinshare)
   * [Déploiement de l'archive et des fichiers de configuration](#instalFile)
   * [Installation de OpenJDK Java JRE](#instalOpenJdk) 
   * [Base de données (Installation de PostgreSQL)](#bdd)
   * [Conteneur de servlet (Installation de Tomcat 8)](#tomcat)
     1. [Configuration vhost ui-user](#ui-user)
     2. [Configuration vhost ui-admin](#ui-admin)
   * [Configuration et lancement de Linshare](#linconf)
   * [Premier accès](#firstAccess)

#### 2. [Installation du composant UploadRequest (optionnel)](#instalUR)
   1. [Installation du composant Upload-Request 1.X (vhost dedié)](#ur10)
     * [Téléchargement du module](#dlmoduleUR)
     * [Déploiement de l'archive](#deployUR)
     * [Configuration Apache](#apacheUR)
     * [Premier accès](#firstAccessUR)
   2. [Installation du composant Upload-Request 1.1.X et supérieur (dans un répertoire)](#ur11)
     * [Téléchargement du module](#dlmoduleUR1)
     * [Déploiement de l'archive](#deployUR1)
     * [Configuration Apache](#apacheUR1)
     * [Premier accès](#firstAccessUR1)

#### 3. [Installation du composant UplaodProposition (optionel)](#instalUP)
   * [Téléchargement du module](#dlmoduleUP)
   * [Déploiement de l'archive](#deployUP)
   * [Configuration Apache via vhost dédié](#apacheUP)
   * [Premier accès](#firstAccessUP)
   * [Configuration Apache via directory](#apacheUP1)
   * [Premier accès](#firstAccessUP1)

###INSTALLATION LINSHARE

> Note:<br/>
    Dans ce guide d'installation, nous avons choisi les composants que nous recommandons à savoir Mongo DB - PostgresSql - Apache tomcat - Apache httpd - JAVA.
   Vous pouvez néanmoins adapter en fonction des composants compatibles que vous aurez retenus.


####Installation minimale de __LinShare__
<a name="install-min"></a>


####Téléchargement de __LinShare__
<a name="dlLinshare"></a>

__LinShare__ est en libre téléchargement à l’adresse suivante (la dernière version) :

  * [http://download.linshare.org/versions/latest](http://download.linshare.org/versions/latest)

Pour cette installation, téléchargez les fichiers suivants :

  * __linshare-ui-admin-{VERSION}.tar.bz2__

<a name="instalFile">
####Déploiement de l'archive et des fichiers de configurations
</a>
Afin de manipuler les archives, il est nécessaire d’utiliser les outils Unzip et Bzip :

`[root@localhost ~]$ aptitude install unzip bzip2 vim`

Créez le répertoire de configuration de __LinShare__ et copiez les fichiers de configuration :

```
[root@localhost ~]$ mv linshare-core-{VERSION}.war linshare.war
[root@localhost ~]$ mkdir -p /etc/linshare
[root@localhost ~]$ unzip -j -d /etc/linshare/ linshare.war WEB-INF/classes/{linshare,log4j}.*
```
###Environnement d’exécution Java (JVM)

__LinShare__ fonctionne avec OpenJDK et Sun/Oracle Java version 8. Ce guide porte sur OpenJDK Java 8.

<a name="instalOpenJdk">
####Installation de OpenJDK Java JRE
</a>
Installez Java Runtime Environment (JRE) de OpenJDK depuis les dépôts :

 > Note :

   - Si vous êtes sur Debian jessie, vous devez ajoutez les bakcports pour pouvoir installer JAVA 8.
   Pour ce faire vous devez ajouter cette ligne : `deb http://ftp.debian.org/debian jessie-backports mai` au fichier `/etc/apt/sources.list`.
   Puis vous pouvez installer le package désiré comme suit : `aptitude update; aptitude -t jessie-backports install votre_package`.

```
[root@localhost ~]$ aptitude install openjdk-8-jre
[root@localhost ~]$ update-alternatives --config java
```

> Remarque:<br/>
    * les éventuelles erreurs relatives au plugin Java peuvent être ignorées.

<a name="bdd">
###Base de données
</a>
__LinShare__ requière l’utilisation d’une base de données (PostgreSQL) 
pour ses fichiers et sa configuration. Ce guide présente une installation avec PostgreSQL.

Installation de PostgreSQL depuis les dépôts :

`[root@localhost ~]$ aptitude install postgresql`

Démarrez le service PostgreSQL :

`[root@localhost ~]$ service postgresql start`

####Création des accès sécurisés

Adaptez le fichier de gestion des accès de PostgreSQL :

```
[root@localhost ~]$ vim /etc/postgresql/<VERSION>/main/pg_hba.conf
 # TYPE  DATABASE                  USER          CIDR-ADDRESS         METHOD
 local   linshare,linshare_data    linshare                           md5
 host    linshare,linshare_data    linshare      127.0.0.1/32         md5
 host    linshare,linshare_data    linshare      ::1/128              md5
```

> Note :<br/>
   * Ces lignes se trouvent généralement à la fin du fichier de configuration.<br/>
   * Pour des raisons de sécurité, le service PostgreSQL n’écoute qu’en local.<br/>
   * Pensez à redémarrer/recharger le serveur PostgreSQL après vos modifications pour qu'elles soient prises en compte:<br/>
   `[root@localhost ~]$ service postgresql restart/reload`

Il convient également d'ajouter ces règles dans les premières. En effet, PostgreSQL utilise la premère règle valide qui correspond à la demande d'authentification.

Créez l’utilisateur « linshare » (mot de passe {PASSWORD}) :

```
[root@localhost ~]$ su - postgres
[postgres@localhost ~]$ psql

CREATE ROLE linshare
  ENCRYPTED PASSWORD '{PASSWORD}'
  NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN;

\q
```
Commandes : pour quitter, tapez « \q » ; pour obtenir de l’aide sous PSQL, tapez « \? ».

Créez et importez les schémas de base de données :

```
[root@localhost ~]$ su - postgres
[postgres@localhost ~]$ psql

CREATE DATABASE linshare
  WITH OWNER = linshare
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;

CREATE DATABASE linshare_data
  WITH OWNER = linshare
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE linshare TO linshare;
GRANT ALL ON DATABASE linshare_data TO linshare;

\q
```

__Important : si votre base de données est installée en langue française, remplacez toutes les occurrences de chaîne « en_US » par « fr_FR ».__

> Note :<br/>
    * Au besoin, vous disposez d'un script nommé createDatabase.sh sous `/WEB-INF/classes/sql/postgresql/` qui vous 
   fournit les commandes pour créer vos bases de données.

Importez les fichiers SQL « createSchema.sql » et « import-postgresql.sql » :

```
[root@localhost ~]$ unzip -c linshare.war WEB-INF/classes/sql/postgresql/createSchema.sql | psql -h localhost -U linshare -W -d linshare
Password for user linshare: {PASSWORD}
[root@localhost ~]$ unzip -c linshare.war WEB-INF/classes/sql/postgresql/import-postgresql.sql | psql -h localhost -U linshare -W -d linshare
Password for user linshare: {PASSWORD}
```

Éditez le fichier de configuration de __LinShare__ :

```
[root@localhost ~]$ vim /etc/linshare/linshare.properties
#******************** DATABASE
### PostgreSQL
linshare.db.username=linshare
linshare.db.password={PASSWORD}
linshare.db.driver.class=org.postgresql.Driver
linshare.db.url=jdbc:postgresql://localhost:5432/linshare
linshare.db.dialect=org.hibernate.dialect.PostgreSQLDialect
```
Pour l'installation de LinShare, vous aurez aussi besoin d'installer une base de données mongoDB. Vous pouvez le faire en suivant les commandes suivantes :

```
[root@localhost ~]$ aptitude install mongodb
```
<a name="tomcat">
###Conteneur de servlets
</a>
__LinShare__ étant une application Java compilée et empaquetée au format WAR (**W**eb **A**pplication a**R**chive), il lui faut donc un __conteneur de servlets Java__ (Tomcat ou Jetty) pour fonctionner.

Ce paragraphe présente l’installation et la configuration du serveur Tomcat.

###Installation de Tomcat 8

Installez Tomcat depuis les dépôts :

`[root@localhost ~]$ aptitude install tomcat8`

####Configuration de Tomcat 8

Pour spécifier l’emplacement de la __configuration__ de LinShare (fichier __linshare.properties__) ainsi que les 
options de démarrage par défaut nécessaire, récupérer les lignes commentées dans l'en-tête dans 
le fichier __linshare.properties__ et copier-coller les dans le fichier tomcat (__/etc/default/tomcat8__).

L’ensemble des options de démarrage par défaut nécessaires à __LinShare__ sont indiquées dans les en-têtes des fichiers de configuration suivants :

  * __/etc/linshare/linshare.properties__
  * __/etc/linshare/log4j.properties__

####Déploiement de l'archive

Déployez l’archive de l’application __LinShare__ dans le serveur Tomcat :

```
[root@localhost ~]$ cp linshare.war /var/lib/tomcat8/webapps/
[root@localhost ~]$ mkdir -p /var/lib/linshare
[root@localhost ~]$ chown -R tomcat8:tomcat8 /var/lib/linshare
```
<a name="apache">
###Serveur web
</a>
L’interface d’administration de __LinShare__ est une application s’appuyant sur les langages web HTML/CSS et JavaScript. Elle nécessite un simple serveur web de type Apache ou nginx.

Ce guide présente l’installation de Apache HTTP Server.

####Installation de Apache 2

Installez Apache 2 depuis les dépôts :

`[root@localhost ~]$ aptitude install apache2

####Configuration du vhost
<a name="ui-user">
####ui-user
</a>

Deployer l'archive de LinShare application UI User dans le repertoire approprié d'Appache 2 :

```
[root@localhost ~]$ cd /var/www/
[root@localhost ~]$ tar xjf /tmp/linshare_data/linshare-ui-user-<VERSION>.tar.bz2
[root@localhost ~]$ mv linshare-ui-user-<VERSION> /var/www/linshare-ui-user
```

Pour déployer l’application LinShare, il est nécessaire d’activer le module __mod_proxy__ sur Apache 2. De plus, il faut ajouter la configuration ci-après au fichier fourni par défaut par Debian :

```
[root@localhost ~]$ cd /etc/apache2/sites-available
[root@localhost ~]$ cp default linshare-user.conf
[root@localhost ~]$ a2dissite default
[root@localhost ~]$ a2ensite linshare-user.conf
[root@localhost ~]$ a2enmod proxy proxy_http
[root@localhost ~]$ vim linshare-user.conf

<VirtualHost *:80>
...
ServerName linshare-user.local
DocumentRoot /var/www/linshare-ui-user
<Location /linshare>
    ProxyPass http://127.0.0.1:8080/linshare
    ProxyPassReverse http://127.0.0.1:8080/linshare
    ProxyPassReverseCookiePath /linshare /

    # Workaround to remove httpOnly flag (could also be done with tomcat)
    Header edit Set-Cookie "(JSESSIONID=.*); Path.*" "$1; Path=/"
    # For https, you should add Secure flag.
    # Header edit Set-Cookie "(JSESSIONID=.*); Path.*" "$1; Path=/; Secure"

    #This header is added to avoid the  JSON cache issue on IE.
    Header set Cache-Control "max-age=0,no-cache,no-store"
</Location>

ErrorLog /var/log/apache2/linshare-user-error.log
CustomLog /var/log/apache2/linshare-user-access.log combined
...
</Virtualhost>
```
> Note:
   * Après toute modification d'un vhost, il faut recharger le server Apache 2 :<br/>
   `[root@localhost ~]$ service apache2 reload` <br/>
   * Dans les versions récentes de Apache, le fichier default peut se nommer default.conf.<br/>
   * Dans le cas où créez un document root, vous pourrez y créer un sous repertoire custom, 
   dans lequel vous pourrez déployer votre logo :<br/>
   `[root@localhost ~]$ mkdir -p linshare/custom`

<a name="ui-admin">
####ui-admin
</a>
Déployez l’archive de l’application __LinShare UI Admin__ dans le répertoire du serveur Apache 2 :

```
[root@localhost ~]$ cd /var/www/
[root@localhost ~]$ tar xjf linshare-ui-admin-{VERSION}.tar.bz2
[root@localhost ~]$ mv linshare-ui-admin-{VERSION} /var/www/linshare-ui-admin
```
Pour déployer l’interface d’administration de __LinShare__, il est nécessaire d’activer le module __mod_proxy__ sur 
Apache2. De plus, il faut ajouter la configuration ci-après au fichier fourni par défaut par Debian :

```
[root@localhost ~]$ cd /etc/apache2/sites-available
[root@localhost ~]$ cp default linshare-admin.conf
[root@localhost ~]$ a2dissite default
[root@localhost ~]$ a2ensite linshare-admin.conf
[root@localhost ~]$ a2enmod proxy proxy_http headers
[root@localhost ~]$ vim linshare-admin.conf
<VirtualHost *:80>
...
ServerName linshare-admin.local
DocumentRoot /var/www/linshare-ui-admin
<Location /linshare>
    ProxyPass http://127.0.0.1:8080/linshare/webservice/rest/admin
    ProxyPassReverse http://127.0.0.1:8080/linshare/webservice/rest/admin
    ProxyPassReverseCookiePath /linshare /

    # Workaround to remove httpOnly flag (could also be done with tomcat)
    Header edit Set-Cookie "(JSESSIONID=.*); Path.*" "$1; Path=/"
    # For https, you should add Secure flag.
    # Header edit Set-Cookie "(JSESSIONID=.*); Path.*" "$1; Path=/; Secure"

    #Cette entête est rajoutée pour éviter à IE de faire du cache sur le JSON
    Header set Cache-Control "max-age=0,no-cache,no-store"
</Location>

<Directory /var/www/linshare-ui-admin>
	   Options -Indexes
	   AllowOverride None
	   Order Allow,Deny
	   Allow from all
</Directory>

ErrorLog /var/log/apache2/linshare-admin-error.log
CustomLog /var/log/apache2/linshare-admin-access.log combined
...
</Virtualhost>
```
> Note:
   * Après toute modification d'un vhost. il faut recharger le server Apache 2 :<br/>
    `[root@localhost ~]$ service apache2 reload`<br/>
   * Dans les version récentes de Apache, le fichier default peut se nommer default.conf.

<a name="linconf">
###Configuration & Lancement de LinShare
</a>
Configurez l’__emplacement de stockage des fichiers__ :

```
[root@localhost ~]$ vim /etc/linshare/linshare.properties
linshare.encipherment.tmp.dir=/var/lib/linshare/tmp
linshare.signature.tmp.dir=/var/lib/linshare/tmp/linSignDocuments
linshare.files.directory=/var/lib/linshare/repository
```
Configurez l’__accès à un service SMTP__, pour l’envoi des messages de notification :

```
mail.smtp.host=<smtp.yourdomain.com>
mail.smtp.port=25
mail.smtp.user=linshare
mail.smtp.password=<SMTP-PASSWORD>
mail.smtp.auth.needed=false
mail.smtp.charset=UTF-8

Sur LinShare, vous avez deux modes d'authentification possibles, le permier est celui par défaut et le second est une authification par sso. Pour démarrer LinShare vous devez au moins activer l'un des modes suivants :

* default : processus d'authentification par défaut.
* sso : permet l'injection d'entête pour le SSO. Ce profil inclue les ... du profil par défaut.

Le profil par défaut est jcloud pour le filesystem pour les tests.

Vous pouvez surcharger ces paramètres en utilisant -Dspring.profiles.active=xxx
Ou vous pouvez utiliser une variable d'environnement : SPRING_PROFILES_ACTIVE

Vous devez aussi activer au moins un des profiles de système de sockage de fichiers en dessous :

Available file data store profiles :

* jcloud : Utilisant jcloud comme système de stockage de fichier : Amazon S3, Swift, Ceph, filesystem (que pour les tests).

* gridfs : Using gridfs (mongodb) comme système de stockage de fichier.

* jackrabbit2 : Using jackrabbit comme système de stockage de fichier.

* jackrabbit-to-jcloud : Utilisant Jcloud comme nouveau système de stockage de fichier, jackrabbit comme fallback file data store.

* jackrabbit-to-gridfs : Utilisant GridFS comme nouveau système de stockage de fichier, jackrabbit comme fallback file data store.

Le profil recommandé est jcloud avec swift.

> Note

    - Nous utilisons uniquement JackRabbit pour la transition, car il est dépricié.

```
Pour __démarrer LinShare__, démarrez le service Tomcat :

`[root@localhost ~]$ service tomcat8 restart`

Afin de vérifier le fonctionnement de __LinShare__, consultez les fichiers des __journaux__ (logs) :

`[root@localhost ~]$ tail -f /var/log/tomcat8/catalina.out`

En fin d’un démarrage correct du service, vous devriez avoir les messages suivants :

```
[...]
org.apache.coyote.http11.Http11Protocol start
INFO: Démarrage de Coyote HTTP/1.1 sur http-8080
org.apache.catalina.startup.Catalina start
INFO: Server startup in 23151 ms
```
Puis redémarrez le service Apache 2 :

`[root@localhost ~]$ service apache2 restart`

Note :

> Vous disposez d'exemples de vhosts dans le repertoire : [utils/apache2/vhosts-sample/](../../../utils/apache2/vhosts-sample/)

<a name="firstAccess">
###Premier accès
</a>
Le __service LinShare__ est désormais accessible aux adresses suivantes.

Pour l’interface utilisateur :

  * __http://linshare-user.local/linshare__

Note :

  > Vous devez renseignez cette url dans les paramètres de votre domaine.<br/>
  Pour ce faire, choisissez la fonctionalité __Domaine__ dans la liste de vos fonctionalités<br/>
  et renseignez la champs __Url de base pour les notifications__ dans la sous fonctionalité __URL de base pour les notifications par courriel__.

Pour l’interface d’administration :

  * __http://linshare-admin.local/__

####Paramétrage de LinShare

Connectez vous à __LinShare__ en tant qu’__administrateur système__ de __LinShare__ :

  * Identifiant : __root@localhost.localdomain__

  * Mot de passe : __adminlinshare__

Ensuite, afin d’interconnecter __LinShare__ avec votre référentiel utilisateurs de type LDAP, créez un nouveau domaine depuis la rubrique « DOMAINES ». 
Pour plus d’informations, veuillez vous référer au __Guide de configuration et d’administration__ de __LinShare__ [__LINSHARE:CONF__].

<a name="instalUR">
##Installation du composant Upload-Request (optionnel)
</a>

<a name="ur10">
###Installation du composant Upload-Request 1.X (vhost dedié)
</a>

Ce module permet à un compte externe à l'application d'accéder à une interface de dépôt de fichier dont les options sont configurables dans l'interface d'administration.

Note :

 > Si vous activez la fonctionnalité de dépôt, depuis l'interface d'administration,  vous devrez déployer ce composant.

<a name="dlmoduleUR"> 
####Téléchargement du module
</a>

Ce module est en libre téléchargement à l'adresse suivante :

  * [http://download.linshare.org/versions/latest](http://download.linshare.org/versions/latest)

Pour cette installation, téléchargez le fichier suivant selon la version désirée :

  * linshare-ui-upload-request-{VERSION}.tar.bz2

<a name="deployUR">
####Déploiement de l'archive
</a>
Déployez l’archive de l’application __LinShare UI Upload Request__ dans le répertoire du serveur Apache :

```
[root@localhost ~]$ cd /var/www/
[root@localhost ~]$ tar xjf linshare-ui-upload-request-{VERSION}.tar.bz2
[root@localhost ~]$ mv linshare-ui-upload-request-{VERSION} /var/www/linshare-ui-upload-request
```
<a name="apacheUR">
####Configuration Apache via un Vhost dédié
</a>
Pour déployer l’interface Upload-Request de __LinShare__, il est nécessaire d’activer le module __mod_proxy__ sur Apache2. De plus, il faut ajouter la configuration ci-après au fichier fourni par défaut par Debian :

```
[root@localhost ~]$ cd /etc/apache2/sites-available
[root@localhost ~]$ cp default linshare-ui-upload-request.conf
[root@localhost ~]$ a2dissite default
[root@localhost ~]$ a2ensite linshare-ui-upload-request.conf
[root@localhost ~]$ a2enmod proxy proxy_http
[root@localhost ~]$ vim linshare-ui-upload-request.conf
```
#####Version 1.0.X:

```
<VirtualHost *:80>
...
ServerName linshare-ui-upload-request.local
DocumentRoot /var/www/linshare-ui-upload-request
<Location /linshare>
    ProxyPass http://127.0.0.1:8080/linshare/webservice/rest/uploadrequest
    ProxyPassReverse http://127.0.0.1:8080/linshare/webservice/rest/uploadrequest
    #Cette entête est rajoutée pour éviter à IE de faire du cache sur le JSON
    Header set Cache-Control "max-age=0,no-cache,no-store"
</Location>

<Directory /var/www/linshare-ui-upload-request>
	   Options -Indexes
	   AllowOverride None
	   Order Allow,Deny
	   Allow from all
</Directory>

...
</Virtualhost>
```
#####Version 1.1.X:

```
<VirtualHost *:80>
...
ServerName linshare-ui-upload-request.local
DocumentRoot /var/www/linshare-ui-upload-request
<Location /linshare>
    ProxyPass http://127.0.0.1:8080/linshare
    ProxyPassReverse http://127.0.0.1:8080/linshare
    #Cette entête est rajoutée pour éviter à IE de faire du cache sur le JSON
    Header set Cache-Control "max-age=0,no-cache,no-store"
</Location>

<Directory /var/www/linshare-ui-upload-request>
	   Options -Indexes
	   AllowOverride None
	   Order Allow,Deny
	   Allow from all
</Directory>

...
</Virtualhost>
```
Pour __accéder à LinShare Upload Request__, démarrez __LinShare Core__ avant, puis redémarrez le service Apache2 :

`[root@localhost ~]$ service apache2 restart`

<a name="firstAccessUR">
###Premier accès
</a>
Le __service LinShare__ est désormais accessible à l'adresse suivante.

Pour l’interface de dépôt de fichiers (module Upload-Request) : 

  * Installation avec un vhost dedié : __http://linshare-upload-request.local/{uuid}__

Note:

 > Vous avez besoin d'un ticket valable issue d'un courriel de __Linshare__ pour pouvoir utiliser ce portail.
   Si ce n'est le cas, vous serez dirigé vers une page 404, Les urls complètes vers ce portail seront envoyées 
   aux destinataires des invitations de dépôt.

 > Vous devez aussi renseiger cette url dans les paramètres de la fonctionalité __Invitation de dépôt__ dans votre interface d'administration.<br/>
   Pour ce faire, alle dans votre interface d'administration, choisissez la fonctionalité __Invitation de dépôt__ parmi les fonctionalités, et renseignez l'__Url de l'application invitation de dépôt __

<a name="ur11">
###Installation du composant Upload-Request 1.1.X et supérieur (dans un directory)
</a>

<a name="dlmoduleUR1">
####Téléchargement du module
</a>
Ce module est en libre téléchargement à l'adresse suivante :

  * [http://download.linshare.org/versions/latest](http://download.linshare.org/versions/latest)

Pour cette installation, téléchargez le fichier suivant selon la version désirée :

  * linshare-ui-upload-request-{VERSION}.tar.bz2

<a name="deployUR1">
####Déploiement de l'archive
</a>
Déployez l’archive de l’application __LinShare UI Upload Request__ dans le répertoire du serveur Apache :

```
[root@localhost ~]$ cd /var/www/linshare
[root@localhost ~]$ tar xjf linshare-ui-upload-request-{VERSION}.tar.bz2
[root@localhost ~]$ ln -s linshare-ui-upload-request-{VERSION} upload-request
```
<a name="apacheUR1">
####Configuration Apache via un directory
</a>
Pour déployer l’interface Upload-Request de __LinShare__, il est nécessaire d’activer le module __mod_proxy__ sur Apache2. De plus, il faut ouvrir le fichier virtualhost linshare-user.conf :

```
[root@localhost ~]$ cd /etc/apache2/sites-available
[root@localhost ~]$ vim linshare-user.conf
```
et ajouter les lignes suivantes à votre section virtualhost:

```
<Directory "upload-request">
	   Options -Indexes
	   AllowOverride None
	   Order Allow,Deny
	   Allow from all
</Directory>
```
Pour __accéder à LinShare Upload Request__, démarrez __LinShare Core__ avant, puis redémarrez le service Apache2 :

`[root@localhost ~]$ service apache2 restart`

<a name="firstAccessUR1">
###Premier accès
</a>
Le __service LinShare__ est désormais accessible à l'adresse suivante.

Pour l’interface de dépôt de fichiers (module Upload-Request) :

  * Installation dans un repertoire : __http://linshare-user.local/upload-request/{uuid}__

Note :
   > Vous devez aussi renseiger cette url dans les paramètres de la fonctionalité __Invitation de dépôt__ dans votre interface d'administration.<br/>
   Pour ce faire, allez dans votre interface d'administration, choisissez la fonctionalité __Invitation de dépôt__ parmi les fonctionalités, et renseignez l'__Url de l'application inviation de dépôt __

##Installation du composant Upload Proposition (optionnel)

Ce module permet à un utilisateur externe de pouvoir demander à un utilisateur interne de lui envoyer une invitation de dépôt (Upload Request).

<a name="dlmoduleUP">
####Téléchargement du module
</a>
Ce module est en libre téléchargement à l'adresse suivante :

  * [http://download.linshare.org/versions/latest](http://download.linshare.org/versions/latest)

Pour cette installation, téléchargez les fichiers suivants selon la version désirée :

  * linshare-ui-upload-proposition-{VERSION}.tar.bz2
  * linshare-upload-proposition-{VERSION}.jar
  * linshare-upload-proposition.sh
  * linshare-upload-proposition-production.yml

> Attention :<br/>
  * ce module requière l’installation du module Upload Request.

Effectuez les commandes suivantes :

```
[root@localhost ~]$ mv linshare-upload-proposition-{VERSION}.jar /usr/local/sbin/linshare-upload-proposition.jar
[root@localhost ~]$ cp linshare-upload-proposition-production.yml /etc/linshare/
[root@localhost ~]$ cp linshare-upload-proposition.sh /ect/init.d/
[root@localhost ~]$ update-rc.d linshare-upload-proposition.sh defaults
[root@localhost ~]$ chmod +x /etc/init.d/linshare-upload-proposition.sh
```
Vous disposez désormais d'un script « linshare-upload-proposition.sh » vouz permettant d'intérroger pour avoir des 
renseignements sur le statut de votre process, mais aussi de pouvoir l'arrêter et le redémarrer.

<a name="apacheUP">
####Configuration Apache via un vhost dedié
</a>

Déployez l’archive 
de l’application LinShare UI Upload-Proposition dans le répertoire du serveur Apache2 :

```
[root@localhost ~]$ cd /var/www/
[root@localhost ~]$ tar xjf linshare-ui-upload-proposition-{VERSION}.tar.bz2
[root@localhost ~]$ mv linshare-ui-upload-proposition-{VERSION} /var/www/linshare-ui-upload-proposition
```
Pour déployer l’interface Upload-Proposition de __LinShare__, il est nécessaire d’activer le module __mod_proxy__ sur 
Apache2. De plus, il faut ajouter la configuration ci-après au fichier fourni par défaut par Debian :

```
[root@localhost ~]$ cd /etc/apache2/sites-available
[root@localhost ~]$ cp default linshare-ui-upload-proposition.conf
[root@localhost ~]$ a2dissite default
[root@localhost ~]$ a2ensite linshare-ui-upload-proposition.conf
[root@localhost ~]$ a2enmod proxy proxy_http
[root@localhost ~]$ vim linshare-ui-upload-proposition.conf
<VirtualHost *:80>
...
ServerName linshare-ui-upload-proposition.local
DocumentRoot /var/www/linshare-ui-upload-proposition
<Location /linshare>
    ProxyPass http://127.0.0.1:9080/linshare/webservice/rest/uploadproposition
    ProxyPassReverse http://127.0.0.1:9080/linshare/webservice/rest/uploadproposition
    #Cette est rajoutée pour éviter à IE de faire du cache sur le JSON
    Header set Cache-Control "max-age=0,no-cache,no-store"
</Location>
<Directory /var/www/linshare-ui-upload-proposition>
	   Options -Indexes
	   AllowOverride None
	   Order Allow,Deny
	   Allow from all
</Directory>
...
</Virtualhost>
```
Pour __accéder à LinShare Upload-Proposition__, démarrez LinShare Core avant, puis redémarrez le service Apache2 :

`[root@localhost ~]$ service apache2 restart`

<a name="firstAccessUP">
###Premier accès
</a>
L’interface de demande de dépôt de fichiers (module Upload-Proposition) est désormais disponible à l'adresse suivante : 

  * __http://linshare-upload-proposition.local/__

<a name="apacheUP1">
###Configuration apache via a directory
</a>

Déployez l'interface __LinShare Upload-Proposition__ dans le repertoire linshare que vous avez créez dans la section user :

```
[root@localhost ~]$ cd /var/www/linshare
[root@localhost ~]$ tar xjf /tmp/linshare_data/linshare-ui-upload-proposition-<VERSION>.tar.bz2
[root@localhost ~]$ ln -s linshare-ui-upload-proposition-{VERSION} upload-proposition
```
Pour déployer l'interface __Linshare Upload-Proposition__, ouvrez le fichier de configuration de virtualhost linshare-user.conf :

```
[root@localhost ~]$ cd /etc/apache2/sites-available
[root@localhost ~]$ vim linshare-user.conf
```
et ajoutez y les lignes suivantes :

```
<Directory "upload-proposition">
	   Options -Indexes
	   AllowOverride None
	   Order Allow,Deny
	   Allow from all
</Directory>
```
Pour __accéder à LinShare Upload-Proposition__, démarrez LinShare Core avant, puis redémarrez le service Apache2 :

`[root@localhost ~]$ service apache2 restart`

<a name="firstAccessUP1">
###Premier accès
</a>

L’interface de demande de dépôt de fichiers (module Upload-Proposition) est désormais disponible à l'adresse suivante :

  * __http://linshare-user.local/upload-proposition/__
