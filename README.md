# StackInfo Maven plugin

A very simple Maven plugin which only provides `prepare` mojo now.
This mojo stores pom.xml files in effective-like format for further processing. It works as an aggregator.


## Usage

Simply run Maven and specify plugin coordinates together with `prepare` mojo:
```
mvn io.github.stackinfo:stackinfo-maven-plugin:0.1:prepare
```

The results will be in `target/stackinfo/poms/`.
