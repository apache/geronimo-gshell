
 +----------+
 | Building |
 +----------+
 
# Helpful to build & test...

mvn "$@"
rm -rf gshell-1.0.0-SNAPSHOT
gunzip -c gshell-assemblies/gshell-assembly/target/gshell-1.0.0-SNAPSHOT-bin.tar.gz | tar xf -

./gshell-1.0.0-SNAPSHOT/bin/gsh "$@"


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

