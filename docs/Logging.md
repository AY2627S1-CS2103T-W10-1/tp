---
layout: page
title: Logging guide
---

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

Command diagnostics must not include raw command text, arguments, command objects, user-facing feedback,
or exception messages/stack traces that can contain contact fields or CSV filenames. Log only trusted command
types, event descriptions, counts, displayed indices and exception types. This applies at every logging level,
including `FINE` and `FINER`. Model initialization logs the contact count instead of the address book contents.
Success and error feedback in the app still identifies the relevant contact or CSV filename for the user.

Regression tests capture formatted application logs at `Level.ALL` through the real parser, logic and command box.
They cover CSV export success, existing-file rejection, invalid filenames, unknown commands, contact viewing,
contact addition and model initialization. Export command tests also check write failures whose exception messages
contain filenames. The tests assert that private values are absent while useful event diagnostics remain.
