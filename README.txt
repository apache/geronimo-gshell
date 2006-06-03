# Helpful to build & test...

mvn "$@"
rm -rf gshell-1.0.0-SNAPSHOT
gunzip -c gshell-assemblies/gshell-assembly/target/gshell-1.0.0-SNAPSHOT-bin.tar.gz | tar xf -

./gshell-1.0.0-SNAPSHOT/bin/gsh "$@"
