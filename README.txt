
 +----------+
 | Building |
 +----------+

# Helpful to build & test...

mvn "$@"
rm -rf gshell-*-SNAPSHOT
gunzip -c ./gshell-assembly/target/gshell-*-bin.tar.gz | tar xf -

./gshell-*/bin/gsh "$@"

