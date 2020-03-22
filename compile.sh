
rm -f $(find . -name "*.class")
gradlew shadowJar
cp build/libs/CA4006_assignment1-all.jar ./factory.jar

echo -e "USAGE:\n\tjava -jar factory.jar --help"
