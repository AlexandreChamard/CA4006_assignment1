
rm -f $(find . -name "*.class")
gradlew shadowJar
cp build/libs/CA4006_assignment1-all.jar ./main.jar

echo -e "USAGE:\n\tjava -jar main.jar --help"
