export CLASSPATH=../lib/pollo.jar:../lib/endorsed/dom3-xercesImpl.jar:../lib/endorsed/dom3-xmlParserAPIs.jar:../build

java -Djava.endorsed.dirs=../lib/endorsed org.outerj.pollo.displayspeceditor.DisplaySpecificationEditor $@
