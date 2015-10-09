Load Testing Scenarios
======================

The target and scenarios are currently all hard-coded and have to be changed in the code
in `MarathonSimulation`. So first adjust that to your needs.

Afterwards you can run the load tests in sbt with:

```
project load-testing
test
```

You can look at the report with:

```
lastReport
```

The project uses the [sbt-gatling-plugin](http://gatling.io/docs/2.1.7/extensions/sbt_plugin.html).