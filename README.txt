
 +----------+
 | Building |
 +----------+

# Helpful to build & test...

mvn "$@"
rm -rf gshell-*-SNAPSHOT
gunzip -c ./gshell-assembly/target/gshell-*-bin.tar.gz | tar xf -

./gshell-*/bin/gsh "$@"


 +-----------+
 | Releasing |
 +-----------+

Make sure that settings.xml contains details for these servers:

 * gshell-releases
 * gshell-site

Then run:

  mvn -Denv=release -Dusername=USER release:prepare

And then:

  mvn -Denv=release -Dusername=USER release:perform

USER must be be replaced by your SVN username

