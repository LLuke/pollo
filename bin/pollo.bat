set CLASSPATH=..\lib\pollo.jar;..\lib\pollohelp.jar;..\lib\endorsed\dom3-xmlParserAPIs.jar;..\lib\endorsed\dom3-xercesImpl.jar;..\lib\commons-digester.jar;..\lib\commons-beanutils.jar;..\lib\commons-collections.jar;..\lib\log4j-core.jar;..\lib\jaxen-core.jar;..\lib\jaxen-dom.jar;..\lib\saxpath.jar;..\lib\msv.jar;..\lib\xsdlib.jar;..\lib\relaxngDatatype.jar;..\lib\isorelax.jar;..\lib\jhbasic.jar;..\conf;..\res;..\build

java -Djava.endorsed.dirs=..\lib\endorsed org.outerj.pollo.Pollo %1
