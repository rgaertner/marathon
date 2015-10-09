Load Testing Scenarios
======================

The target and scenarios are currently all hard-coded and have to be changed in the code
in `MarathonSimulation`. So first adjust that to your needs.

Use this only on a test Marathon instance and not in production!

Make sure that you start with an empty Marathon (using httpie):

```bash
http DELETE :8080/v2/groups/
```

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