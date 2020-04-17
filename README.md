# Use this artifact

## Include n-able local repository in your pom.xml

```xml
<repository>
	<uniqueVersion>false</uniqueVersion>
	<id>local</id>
	<name>nable Repository</name>
	<url>http://192.168.50.18:8081/repository/java-artifacts/</url>
	<layout>default</layout>
</repository>
```


## Add dependency 

```xml
<dependency>
	<groupId>biz.nable.sb.cor.common.utils</groupId>
	<artifactId>SB_COR_Common-Utils</artifactId>
	<version>0.0.3-GA</version>
</dependency>
```
