# Tutorial 2: adding a count command

This is a late Tutorial 2 submission on the required `tutorial-adding-command`
branch. The course permits a different command from the tutorial's `remark`
example. This exercise implements `count [--all]` against the current team code.
It is submitted for the tutorial and is not intended for the product release.

## Command behavior

- `count` reports the number of contacts in the displayed list.
- `count --all` reports every stored contact, including hidden contacts.
- An empty list returns zero. Unexpected arguments or repeated flags are rejected.
- Counting preserves contacts, filtering and display order, and skips JSON saving.
- `help count` explains both scopes with working examples.

## Execution path

1. `AddressBookParser` recognizes `count`, ignoring command-word case.
2. `CountCommandParser` validates the optional `--all` flag and constructs a command.
3. `CountCommand.execute` reads the appropriate model list and returns feedback.
4. `LogicManager` displays the feedback without calling storage, because the
   command returns `false` from `requiresStorageSave`.

The command and parser are separate, as in the official adding-a-command
tutorial. The help catalogue supplies both the overview entry and detailed help.
Automated checks exercise routing, malformed inputs, empty data, filtered data,
state preservation and help integration.

## Manual verification steps

1. Start the app with sample contacts and run `count --all`.
2. Run `find Alex`, then `count`: only displayed matches should be counted.
3. Run `count --all` again: the stored total should remain unchanged.
4. Run `count --all extra`: an input-format error should appear.
5. Run `help count`: the detailed instructions should open.

## Acknowledgement

OpenAI Codex assisted He Qianyi with the command, parser, tests and this document.
This document describes the implementation and verification steps; it does not
claim that the student has personally completed a debugger or GUI walkthrough.
