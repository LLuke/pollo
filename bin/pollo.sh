export CLASSPATH=../lib/pollo.jar:../lib/pollohelp.jar:../lib/endorsed/dom3-xercesImpl.jar:../lib/endorsed/dom3-xmlParserAPIs.jar:../lib/avalon-configuration.jar:../lib/commons-beanutils.jar:../lib/commons-collections.jar:../lib/log4j-core.jar:../lib/jaxen-core.jar:../lib/jaxen-dom.jar:../lib/saxpath.jar:../lib/msv.jar:../lib/xsdlib.jar:../lib/relaxngDatatype.jar:../lib/isorelax.jar:../lib/xpp3-1.1.3.4-RC3_min.jar:../lib/commons-lang-exception-2.0.jar:../conf:../build

java -Djava.endorsed.dirs=../lib/endorsed org.outerj.pollo.Pollo $@
