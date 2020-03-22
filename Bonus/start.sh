
rm -f $(find . -name "*.class")
java -jar factory.jar

echo -e "USAGE:\n\tjava -jar factory.jar --help"
