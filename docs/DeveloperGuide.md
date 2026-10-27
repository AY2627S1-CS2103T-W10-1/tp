---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**
This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

* [SE-EDU initiative](https://se-education.org)
* He Qianyi used OpenAI Codex to assist with feature 4 (partial-contact addition): parser/options,
  omitted-field representation, duplicate identity, storage/UI integration, tests and related UG/DG documentation.
* He Qianyi used OpenAI Codex to assist with the feature 3 `view CONTACT_ID` implementation, including the
  command/parser, details window, command-result integration, tests, and related
  User Guide/Developer Guide documentation.

OpenAI Codex assisted He Qianyi with the Feature 11 CSV export implementation, tests and documentation.
He Qianyi is responsible for reviewing and understanding these changes.
He Qianyi used OpenAI Codex to assist with updating and verifying the UI class diagram and the UML sequence diagrams for contact viewing, partial-contact addition and CSV export.

He Qianyi used OpenAI Codex to assist with diagnostic logging for contact viewing and CSV export,
including review fixes and regression tests to keep private input and feedback out of application logs.
He Qianyi used OpenAI Codex to assist with iterative email validation, long-input regression tests,
contact-workflow glossary updates and alpha-review documentation corrections.
He Qianyi used OpenAI Codex to assist with alpha-bug fixes for read-only contact viewing, specific CSV
filename feedback, return-to-prompt focus after popup dismissal, deterministic detail-value selection,
regression tests and matching documentation.
He Qianyi used OpenAI Codex to assist with the Week 11 UG/DG review, contact-workflow use cases,
reproducible manual testing instructions, documentation links, diagrams and current product screenshots.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `DepartmentPanel`, `PersonListPanel`, `DetailsPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

`MainWindow` lays its parts out as numbered panes, in the style of a terminal dashboard: \[1\] `DepartmentPanel`
(each registered department with its number of contacts), \[2\] `PersonListPanel` (one table row per contact, which
grows taller when its tags wrap), \[3\] a docked `DetailsPanel`, and \[4\] the `ResultDisplay` above the `CommandBox`
prompt. Alt+1 to Alt+4 (or Esc for the prompt) move the keyboard focus between them, and the pane with the focus
is outlined. `DepartmentPanel` and `StatusBarFooter` read the unfiltered `Logic.getPersonList()` and
`Logic.getDepartmentList()`, so they stay up to date with every change.

Contact details appear in two places. The docked \[3\] pane follows the selection in \[2\]: selecting a row with a
click or j/k formats that contact with `ViewCommand.formatDetails(Person)` and shows it at once, without running a
command, so glancing through contacts stays instant. To compare contacts side by side, details can also pop out
into windows managed by `ContactDetailsWindows`. A successful `view` command returns the
viewed `Person` and a details snapshot through `CommandResult`. `ContactDetailsWindows.show(Person, String)` refreshes
the open `ContactDetailsWindow` (a `UiPart<Stage>`) whose contact `isSamePerson` as the viewed one, or opens a new
popup cascaded from the main window. An edit can change every field `isSamePerson` compares, so
`ContactDetailsWindows` also listens to `Logic.getPersonList()`: when a contact is replaced in place, its popup
switches to the edited `Person`, and viewing a renamed contact still refreshes its popup. Like the docked pane, each
popup hosts a `DetailsPanel`, which turns every `Field: value` line of the snapshot into a grey field name beside a
read-only value that can be selected with j/k or Tab and copied. Several contacts can therefore be compared side by
side, while viewing the same contact again never duplicates its popup. Popups are owned by the main stage, so they
stay in front of it, and the keyboard focus stays at the prompt after `view`. Pressing Enter on a row in \[2\], or
double-clicking it, runs `view` with that row's contact ID. Repeated Alt+3 presses step through the docked pane and
then each popup, `q` closes the focused popup, and every popup is hidden when the application exits.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. `LogicManager` checks `Command.getAddressBookReplacement()` before execution. If present, it saves the
   proposed replacement before executing the command, and skips the normal save afterward. Confirmed
   `ClearCommand` uses this path; a save failure prevents execution and preserves the live model.
   Otherwise, `LogicManager` saves the canonical address book after execution if `Command.requiresStorageSave()`
   returns true.
   Read-only `ViewCommand` and display-only `SortCommand` return false, as do `ExportCommand`, `HelpCommand`,
   department listing and unconfirmed `ClearCommand`.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* maintains an observable display-order projection separate from the canonical person list. A `FilteredList` over this projection applies the current filter and preserves display-only sorting. It exposes an unmodifiable `ObservableList<Person>` shared by the UI and index-based commands.
* records session-local deletion history alongside the contact data.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-W10-1/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### CSV export (Feature 11 / UC9)

The sequence below shows a successful CSV export. Parsing is summarized in a reference frame; the command
reads all stored contacts, writes the CSV through `CsvContactExporter`, and skips the normal JSON save.

![Successful CSV export sequence](images/ExportCsvSequenceDiagram.png)

`AddressBookParser` dispatches `export` to `ExportCommandParser`. The parser requires `-c` or `--csv`, accepts
one optional basename (with matching quotes for spaces), and defaults to `contacts.csv`.
`CsvContactExporter.getFileNameError` returns a correction for the first violated rule: a missing stem,
surrounding whitespace, a folder path, reserved/control characters, a non-lowercase `.csv` extension or a
reserved Windows device name. `isValidFileName` delegates to the same validation, so parsing, command
construction and file writing keep one filename policy on all supported platforms. The filename policy follows
[Windows filename restrictions](https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file).

`ExportCommand` snapshots `model.getAddressBook().getPersonList()` rather than the filtered list.
It rejects an empty snapshot, then delegates file writing to the storage helper `CsvContactExporter`.
The command leaves the model and filter unchanged. A successful command returns the row count and filename
through the existing feedback display. Its `requiresStorageSave()` override skips the unrelated JSON save:
once the CSV exists, a JSON save failure must not turn a successful export into an error.

The exporter writes UTF-8 with a fixed six-column header and CRLF record separators. Values containing
commas, double quotes, CR or LF are quoted, and inner double quotes are doubled, following
[RFC 4180](https://www.rfc-editor.org/rfc/rfc4180). It writes raw field values, maps an absent department
to an empty cell, sorts tags and joins them with `; `. It preserves input order and repeated records.
Raw empty values also allow partial contacts to be exported when the partial-add feature is integrated.

The exporter writes and closes a temporary file in the destination directory before moving it to the
requested name. It checks for an existing destination and moves without `REPLACE_EXISTING`, so a file
created during export is also protected. The temporary file is cleaned up on completion or failure.
`FileAlreadyExistsException` produces a choose-another-filename message; other `IOException`s produce
a file creation error instead of a success result. The default output directory is the process working
directory; an injectable exporter isolates tests and simulates permission or interrupted-write failures.

Export uses validated in-memory contacts. Invalid JSON/contact data is handled by the existing startup
loader; export does not re-read a JSON file edited externally during the session. It adds no recovery path
for corrupted persistent data. Parser, model integration, real file, cleanup and JavaFX command-box tests
cover the export flow, including a filtered list and an existing destination.

### Adding contacts with partial information

The sequence below shows the successful addition of a partial contact with a name and phone number.
Parsing creates missing-value objects for omitted email and address fields; the command checks for duplicates,
adds the contact, and follows the normal JSON save.

![Successful partial-contact addition sequence](images/AddPartialContactSequenceDiagram.png)

`AddCommandParser` and `EditCommandParser` share `ArgumentTokenizer`. Each `Prefix` in `CliSyntax` has a
short and a long form (e.g. `-n`/`--name`), and both forms map to the same `ArgumentMultimap` key, so they can
be mixed and repeats in either form are detected as duplicates. The tokenizer splits input into words, treats a
quoted word as literal text (never a flag), rejects unknown `--` options, and stores each word after `-t`/`--tag`
as a separate tag. Name and at least one phone number or email address are required. Both the parser and
`AddCommand` reject contacts without either means of contact. Omitted phone, email and address use immutable
`notProvided()` value objects whose
stored values are empty strings and whose display strings are `Not provided`. Their public constructors and
parser validators still reject empty supplied values. Department remains optional. The department
catalogue and assignment rules are specified below under Department management.

Unrecognised single-hyphen words within a value are literal text rather than unsupported options:
`add --name Alice --phone 91234567 --address Road -x typo` stores `Road -x typo` as the address.
Recognised short flags still delimit fields unless quoted. This distinction also applies to `edit`.
`Name` currently accepts only ASCII letters, digits and ordinary spaces, starting with a letter or digit;
accented or Chinese characters, apostrophes and hyphens are outside this version's supported name format.

`Email.isValidEmail` scans characters and separator boundaries iteratively, preserving the existing format
rules without recursive regex matching. Validation takes linear time and constant auxiliary space, including
long local parts and domains. This avoids a `StackOverflowError` on repeated email segments and retains
previously accepted stored emails. Invalid email input still produces the normal `ParseException` through
`ParserUtil`; there is no new email-length restriction.

`JsonAdaptedPerson` maps null, omitted or empty phone/email/address fields to the missing value objects;
nonempty invalid data still causes a loading error. Existing fully populated data retains its fields.
`add` and `edit` both parse user input through `ParserUtil`, which validates phone numbers as 7–15 digits and
tags as 1–30 characters with the team-specified hyphen/underscore support and leading `#`/`/` normalization.
Keeping these rules in `ParserUtil`, rather than in one command's parser, keeps the two commands consistent.
Storage retains compatibility with previously valid short phone numbers (`Phone.isValidPhone` still accepts
3 or more digits) and longer tags through `Tag.fromStoredValue`; normal `add` and `edit` validation uses the
shared 1–30 character limit. Tag search accepts those longer stored values so legacy tags remain discoverable.
This avoids a migration of old contact data.
Tag search and matching reuse the teammate's implementation.

`Person.isSamePerson` requires the same name and at least one matching provided phone number or email
address. Missing fields are not matches. Address, Department and tags do not contribute to identity.
Names and emails are compared case-sensitively; phone numbers use exact string equality.
This duplicate relation is not transitive: a proposed edit can match its original contact by phone and another
contact by email. Therefore both `EditCommand` and `UniquePersonList.setPerson` check every other contact,
excluding the edited target, even when the original and edited versions match. `edit` can complete partial
contacts while preserving their other fields. Assigned departments must already exist in the department catalogue.
Editing a legacy contact without phone and email requires supplying at least one of them; storage loading
retains compatibility with old missing fields.

Loading is restoration rather than a new addition. `JsonSerializableAddressBook` validates all supplied
fields and department references, then restores the complete ordered list through `AddressBook.setPersons`.
Bulk restoration rejects only repeated name/phone/email combinations, as the previous storage rule did.
It therefore preserves every previously valid contact, including same-name contacts sharing either phone or
email, instead of rejecting the file and starting with empty data. The same path preserves these records when
`ModelManager` copies the address book and department operations replace contact objects without changing
their identities. Department undo also restores assignments in bulk and updates history references.
`addPerson`, interactive `setPerson` and contact undo continue to enforce `Person.isSamePerson` strictly;
there is no global switch that disables duplicate checks. Existing unresolved conflicts are retained on disk,
and a contact edit must produce an identity that does not conflict with another record. Contact undo also
requires a phone number or email address; attempting to restore a legacy name-only contact leaves both the
file and deletion history unchanged and reports the missing-information error.

`LegacyContactCompatibilityTest` loads previously valid JSON through the real `MainApp` startup path, with
and without a department catalogue. It verifies every contact field and ordering after a saving command,
an unrelated addition and repeated restarts, plus strict add/edit/contact-undo rejection and department
rename/delete/undo on the restored data. Rejected operations retain stored bytes and contact undo history.

`AddCommand` returns full labelled feedback, sorted tags and missing-field placeholders. In the contact
table, `PersonCard` shows a dimmed `—` for missing fields and for no tags, while the \[3\] Details pane and
popups show the same `Not provided` and `None` placeholders as `AddCommand`. `LogicManager` retains the
existing save failure behaviour: it reports the error instead of returning an addition-success result.
The model change and filter/sort reset occur before saving. If saving fails, the contact addition or edit
remains in memory and is not rolled back, so repeating an `add` may fail the duplicate check. After resolving
the storage error, `list` retries the normal save. The UG describes this recovery before closing the app.

### Department management: design specification

The department catalogue is stored alongside contacts in the address book. `depart` manages a shared
catalogue of department names, while `edit` assigns a catalogue entry to an individual contact.
The catalogue includes departments with no assigned contacts and is independent of the current contact filter.

| Command | Behaviour |
| --- | --- |
| `depart --delete DEPARTMENT` | Remove a catalogue entry and clear matching assignments in contacts and deletion history. |
| `depart --list` | List all departments in the catalogue, including unused departments. Report an empty catalogue when none exist. |
| `depart --add DEPARTMENT` | Add a new department to the catalogue without assigning it to any contact. |
| `depart --edit OLD_DEPARTMENT NEW_DEPARTMENT` | Rename an existing catalogue entry and update every contact assigned to it, including contacts hidden by a filter. |
| `edit CONTACT_ID --department DEPARTMENT` | Assign an existing catalogue department to the contact at the specified displayed-list position. The short option `-d` is also supported. |

Examples: `depart --add Engineering`, `depart --list`, `depart --edit Engineering "Research & Development"`,
and `edit 1 --department "Research & Development"`. Quote names containing spaces to distinguish the two
arguments of `depart --edit`. The command word is `depart` for all four catalogue operations.
The previous `depart CONTACT_ID --set DEPARTMENT` form is replaced by `edit CONTACT_ID --department DEPARTMENT`.

Department names are case-sensitive and must contain 2-50 characters using only letters, numbers, spaces, `-`, or `&`.
All department inputs are trimmed before format validation and catalogue lookup; spaces inside quoted names are preserved, while leading and trailing whitespace is discarded. `Department` stores the trimmed name, including when loading existing catalogue and contact data.
Reject empty or invalid names, duplicate catalogue additions, an unknown rename source, and a rename target
that already belongs to another catalogue entry. Reject missing arguments, extra arguments, and combined
catalogue operations without modifying the catalogue or contacts.

Renaming changes the catalogue entry and all its contact assignments as one logical operation; other contact
fields remain unchanged. Persist the catalogue (including unused entries) and updated assignments across restarts.
Listing departments does not modify contacts, the catalogue, filtering, or sorting.

`edit` validates both the displayed contact ID and membership of the requested name in the catalogue.
An unknown department is rejected without changing any supplied contact fields; the user must first register it
with `depart --add`. Omitting `--department` preserves the contact's existing department. A successful assignment
replaces only that contact's department, along with any other explicitly supplied edit fields; it does not rename
a catalogue entry or affect other contacts. Contacts may still have no department.
`add --department` applies the same catalogue membership check before adding a contact.

`AddressBook` owns the observable department catalogue and exposes an unmodifiable view through
`ReadOnlyAddressBook.getDepartmentList()`. In the Model class diagram, `AddressBook` is associated with
zero or more registered `Department` values, while each `Person` has zero or one assigned department.
Department equality is based on the case-sensitive name; assignments do not require the same object instance
as the catalogue entry.

`AddressBookParser` routes `depart` to `DepartCommandParser`. The parser uses the shared quote-aware word
lexer to enforce one operation and its exact argument count, validates names with `Department`, and creates
`DepartCommand.list()`, `DepartCommand.add(...)`, `DepartCommand.delete(...)`, or `DepartCommand.rename(...)`.
`DepartCommand.execute` checks catalogue membership through `Model.hasDepartment` before a mutation.
Additions delegate to `ModelManager.addDepartment`; renames delegate to `ModelManager.renameDepartment`.
`AddressBook.renameDepartment` replaces matching contacts with copies produced by
`Person.withRenamedDepartment`, preserving their other fields and leaving unrelated contacts unchanged.

`depart --list` skips the storage save and preserves display state. Adding an unused catalogue entry also
preserves contact filtering and sorting. `EditCommandParser` stores `-d`/`--department` in
`EditPersonDescriptor`; `EditCommand` checks the displayed index and catalogue membership before updating
any fields. A successful contact edit displays all contacts in stored order.
`ModelManager.renameDepartment` updates both stored contacts and deletion history, so `undo` cannot restore
an obsolete department name. A rename retains the active filter and resets display sorting.
`DepartCommand.delete(...)` validates catalogue membership before calling `ModelManager.deleteDepartment`.
`AddressBook.deleteDepartment` removes the entry and clears matching assignments, including hidden contacts.
`ModelManager` records the department and affected contact objects in a deletion entry, alongside markers for
contact deletions in a shared chronological history. Matching assignments in contact deletion history are
cleared too. The active filter is retained and sorting resets to stored order.
`UndoCommand` checks the latest deletion type. For a department deletion, it rejects an existing catalogue name
before changing state; otherwise it restores the department and assignments on tracked contacts still without
a department, including contacts awaiting restoration. `setPerson` updates tracked references so subsequent
name or phone edits survive undo. Explicit reassignment to another department is preserved. Missing contacts
are not recreated. The history entry is consumed only on success, and all contacts are displayed in stored order.
Department restoration reports `Restored deleted department: [NAME]`; a name conflict reports
`Cannot restore the deleted department because it already exists: [NAME]` and preserves data and history.
History is session-only. Successful deletion and restoration use the normal storage-save flow.

Renaming an entry to its own name is allowed. `clear` resets the catalogue together with contacts.

`JsonSerializableAddressBook` persists names in a `departments` array, including unused entries.
Legacy files with an absent or null `departments` array derive the catalogue from their contact assignments. Files containing an explicit
catalogue reject exact duplicate or invalid names and contact assignments absent from the catalogue.
Legacy catalogue entries differing only in surrounding whitespace are merged in first-occurrence order;
contact assignments are normalized to the same names. Normalized data is persisted on the next successful save.
`LogicManager` saves successful catalogue additions, renames, deletions, and contact assignments through the normal
storage flow. A save failure is reported instead of success; the in-memory change is not rolled back.
`DepartmentIntegrationTest` covers persistence, rejected edits, listing without writes, and rename followed by undo.
`DepartCommandParserTest`, `DepartCommandTest`, `AddressBookTest`, and `JsonSerializableAddressBookTest`
cover invalid input, catalogue constraints, unaffected contacts, and legacy-data loading.

#### Department command formats and feedback

| Operation | Format | Required name arguments |
| --- | --- | --- |
| List | `depart --list` | None |
| Add | `depart --add DEPARTMENT` | Exactly one |
| Delete | `depart --delete DEPARTMENT` | Exactly one |
| Rename | `depart --edit OLD_NAME NEW_NAME` | Exactly two, source first |

The command word `depart` is case-insensitive. Options are lowercase, case-sensitive, unquoted,
and have no short forms. Choose exactly one operation; extra arguments and repeated or combined operations
are rejected. Use ordinary ASCII hyphens and separate the option and arguments with whitespace.
The old `depart CONTACT_ID --set DEPARTMENT` form is not supported.

Names are case-sensitive and contain 2-50 characters from ASCII letters (`A-Z`, `a-z`), digits (`0-9`),
spaces, `-`, and `&`; names consisting only of whitespace are invalid. Names containing spaces must be one quoted argument, using matching single or double quotes, e.g. `depart --edit Engineering "Customer Support"`.
Unlike `add` and `edit`, an unquoted multi-word department name is treated as multiple arguments and is rejected.
Leading and trailing whitespace is trimmed after quotes are removed; internal spaces are preserved and count towards the length limit.
A closing quote must be followed by whitespace or the end of input. An unquoted name starting with `--`
is treated as an invalid option-like argument. To use such a name, quote it.
Renaming to the source's exact current name is allowed; renaming to another existing entry is not.

**Validation order:** Command structure (including quotes, option and argument count) is checked first.
Names are then validated left to right, checking blank values before character and length constraints.
Only after all names are valid does the system check whether a deletion target exists, an addition is a duplicate,
or, for a rename,
whether the source exists followed by whether the target belongs to another entry. The first failure is reported;
no catalogue or contact changes occur on validation failure. For example, `depart --edit Unknown !`
reports the invalid target name before checking whether Unknown exists.

In the messages below, bracketed placeholders are replaced by the supplied names without surrounding quote
characters. Code blocks show actual line breaks.

| Condition | Feedback |
| --- | --- |
| List a nonempty catalogue | `Departments:` followed by each registered name on a separate line. |
| Add a department | `Added department: [NAME]` |
| Rename a department | `Renamed department: [OLD_NAME] -> [NEW_NAME]` |
| Invalid command structure, including a missing name argument | The format error shown below. |
| Supplied name is empty or whitespace-only, e.g. `depart --add ""` | `Department name cannot be empty.` |
| Nonblank name violates character or length constraints | `Invalid Department: Must be 2-50 characters using only letters, numbers, spaces, '-', or '&'.` |
| Addition duplicates an existing entry | `Department already exists: [NAME]` |
| Delete target does not exist | `Department does not exist: [NAME]. Use depart --list or depart --add DEPARTMENT.` |
| Delete a department | `Deleted department: [NAME]. Associated contacts now have no department.` |
| Rename source does not exist | `Department does not exist: [OLD_NAME]. Use depart --list or depart --add DEPARTMENT.` |
| Rename target belongs to another entry | `Department already exists: [NEW_NAME]` |
| Catalogue is empty | `No departments have been registered.` |

Invalid command structure includes missing, unknown or quoted operations; missing or extra arguments;
repeated or combined operations; unmatched quotes or text immediately following a closing quote; and
unquoted names beginning with `--`. The exact feedback is:

```text
Invalid command format!
depart --list | depart --add DEPARTMENT | depart --edit OLD_NAME NEW_NAME | depart --delete DEPARTMENT
```

### Searching names with quotes

Name search uses `ArgumentTokenizer.splitWords` to remove matching outer quotes before splitting
the resulting text into whole-word OR keywords. Single and double quotes behave identically;
quotes inside unquoted words remain literal. Empty input, unmatched quotes and text immediately
after a closing quote are rejected.

### Searching contacts by tags (UC6)

The tag-search command is `find --tag TAG [TAG ...] [--tag TAG ...]`. The option is lowercase and may be
repeated; as with `add` and `edit`, several tags may follow one option. The command word is case-insensitive.
Tag matching is case-insensitive; tags contain ASCII letters, digits, hyphens, or underscores. New tags are
limited to 30 characters, while search accepts longer legacy tags retained during storage loading.
`ParserUtil.parseTagForSearch` trims surrounding whitespace and removes leading `#` or `/` characters. Matching quotes
are removed by the shared tokenizer. A tag that looks like an option must be quoted, so
`find --tag "--tag"` searches for the literal tag `--tag` rather than introducing another option.

#### Parsing and validation

`AddressBookParser` delegates `find` to `FindCommandParser`. The parser uses the shared quote-aware
`ArgumentTokenizer`, with each word after `--tag` treated as a tag value. It rejects text before the first
`--tag`, preventing a combined name-and-tag search. Values are validated from left to right through
`ParserUtil.parseTagForSearch`, keeping normalization and character validation aligned with `add` and `edit`
while allowing the longer values accepted by `Tag.fromStoredValue`.
Normalized duplicate query tags are removed before constructing the command.

| Condition | Feedback |
| --- | --- |
| Missing or whitespace-only value after `--tag` | `Tag cannot be empty: Please provide a valid tag after '--tag'.` |
| Tag violates the shared character constraints | `Tags must use only letters, numbers, hyphens (-), and underscores (_), with no spaces.` |
| No standalone `--tag`, but an option-like token is supplied | `Invalid find option: '[INPUT]'. Use find --tag TAG [TAG ...] [--tag TAG ...] or find KEYWORD [MORE_KEYWORDS].` |
| No arguments, or text before the first standalone `--tag` | `Invalid command format!` followed by a newline and `FindCommand.MESSAGE_USAGE`. |

Without a standalone `--tag`, a token beginning with `-`, an en dash, or an em dash is treated as an
unsupported option; otherwise parsing follows the existing name-search path. After a valid `--tag`, an
unquoted word beginning with `--` is also treated as an option and rejected if unknown. Quoting makes it a
tag value instead. Empty quoted text and an option without a value produce the missing-tag message.
Parsing failures leave the current filter, display order, and contacts unchanged.

Tag search uses `String.equalsIgnoreCase` so users need not remember tag capitalization,
consistent with name search and tag sorting. Stored tag spelling and tag equality are unchanged;
case variants may coexist in storage but are equivalent for searching.

#### Matching and execution

For valid tag input, the parser constructs `FindCommand(new TagsMatchPredicate(tags))`.
`TagsMatchPredicate` keeps an immutable copy of the query and uses `String.equalsIgnoreCase` to compare complete
stored tag names. A contact matches if any stored tag equals any requested tag (OR matching); substring
matches do not match; case-only differences do match. Contacts with no tags do not match.

`FindCommand.execute` passes the predicate to `Model.updateFilteredPersonList`. `ModelManager` restores
the display projection to stored order and applies the new predicate across all stored contacts, replacing
any previous filter and resetting sorting. Filtering selects entries from the stored contact list, so repeated
query tags or multiple matching tags do not duplicate contacts in the results.

A successful search reports `[COUNT] person(s) listed!`. If no contacts match, it displays an empty list and
`No contacts found with the specified tag(s).`; this is a valid search result, not a validation error.
Contact fields remain unchanged. `FindCommand.requiresStorageSave()` returns false because searching changes
only the displayed filter, so `LogicManager` returns the result without rewriting the address book data file.

`FindCommandParserTest` covers repeated options, whitespace, tag length boundaries, invalid tags,
unsupported options, and mixed name/tag input. `TagsMatchPredicateTest` verifies OR matching, case-insensitive comparisons,
whole-tag matching, and defensive copying. `FindCommandTest` verifies filtered results and feedback.

### Listing all contacts: design specification

The list feature lets users review every stored contact after applying a search or filter.
It accepts `list` or `ls`, with no parameters. Command words are case-insensitive;
`LIST`, `List`, and `LS` are accepted. Leading and trailing spaces are ignored.
Both spellings clear the current filter and display sorting, showing contacts in stored order.

Any extra input produces exactly:
`The list command does not accept any parameters. Usage: list | ls`
Rejecting extra input prevents users from assuming unsupported filtering or sorting options were applied.

`AddressBookParser` routes both spellings to `ListCommandParser`, which rejects nonblank arguments
before creating the shared `ListCommand`. Its execution uses `PREDICATE_SHOW_ALL_PERSONS` through
`Model.updateFilteredPersonList` to restore the full contact list and stored order.

### Contact display sorting

The sequence diagram below shows a successful `sort --by tags` command with a nonempty displayed list.
It focuses on parsing and execution within Logic, treating Model as a black box.

![Sequence diagram for the sort --by tags command](images/SortSequenceDiagram.png)

`LogicManager` passes the command text to `AddressBookParser`, which delegates the arguments to
`SortCommandParser`. The parser validates the option and field, normalizes `tags` to `SortField.TAGS`, and
creates a `SortCommand`. `LogicManager` then executes the command, which calls `Model.sortFilteredPersonList`
to reorder the displayed contacts while preserving the active filter and canonical contact order.
The command returns a `CommandResult` with the displayed contact count and selected field.

The diagram's final note summarizes the storage behavior: `SortCommand.requiresStorageSave()` returns false,
so `LogicManager` returns the result without saving. Routine list reads, result construction, and this storage
check are omitted from the diagram for clarity. Existing commands retain their previous save behavior.
Parse-error precedence is descending option, unsupported argument structure, missing option/value, then invalid
field; the user guide lists the exact messages.

`ModelManager` maintains an observable display projection separate from `AddressBook`. `FilteredList` is backed by
this projection, so the UI and index-based commands share the same displayed order. Sorting only rearranges visible
positions and preserves the filter. Successful canonical contact changes refresh the projection and reset sorting;
`list` and `find` also reset sorting through `updateFilteredPersonList`. `view` and `help` preserve the order.
Undo appends the restored contact in canonical order and shows the full list, preserving its department and tags.
The address book listener keeps direct changes through the existing read-only list's observable backing synchronized.

`ContactSorter` precomputes keys, then stably sorts a copy using case-insensitive comparison and missing values last.
It keeps duplicate occurrences and never mutates input. Stability uses the current display order, including when
switching fields. No contact-name tie-breaker is applied. Sorting a canonical-source `SortedList` without preserving current tie order would violate this rule.

`SortField.DEPARTMENT` reads the optional stored value from `Person.getDepartment()`. Department and tag
constructors validate their immutable values; sorting rejects null tag elements before changing the display.
Missing departments sort last.
Tag keys use the case-insensitive minimum of the unordered tag set; empty tag sets are absent keys.
Tag sorting does not group a contact by all of its tags, and does not change the stored tag set.
`ContactSorterTest` verifies real department values, case-insensitive ordering, missing values, and stable ties.
`view`, `edit`, and `delete` resolve indices against the same sorted display; undo preserves department data.

`MainApp` logs startup loading failures and initializes an empty address book when loading fails.
Sorting uses the current in-memory contacts, so valid contacts added afterward can be sorted without restarting.
An empty displayed list produces `No contacts found to sort.` Sorting still rejects null tag elements before
publishing a new order. A missing data file retains the existing sample-data behavior.

Regression coverage includes parser boundaries, aliases and errors; stable department and tag ordering; observable
display/filter behavior; editing and deleting sorted indices; unchanged storage; and recovery after startup loading
failures, including sorting contacts after successful additions and saves in the same session.
Run `./gradlew check coverage` (Windows: `.\gradlew.bat check coverage`) with Java 25.

### Clearing contacts with confirmation

`AddressBookParser` delegates `clear` to `ClearCommandParser`. After trimming surrounding whitespace,
the parser accepts only blank arguments or the exact `--confirm` flag. All other arguments produce
`MESSAGE_INVALID_COMMAND_FORMAT` with `ClearCommand.MESSAGE_USAGE` before any command executes.

`ClearCommand` stores an immutable confirmation flag. Without confirmation, it counts the canonical
address book's contacts and returns a warning that all contacts and registered departments will be deleted.
The count refers only to contacts; the department warning also appears when there are no contacts.
Contacts, the department catalogue, display filtering and sorting, and deletion history remain unchanged.
Its `requiresStorageSave()` override returns false in this case.
With confirmation, `getAddressBookReplacement()` returns an `AddressBookReplacement` containing an empty
address book and clear-specific recovery instructions. `LogicManager.executeReplacement` saves that
replacement first and invokes `ClearCommand.execute` only after saving succeeds. The command applies the
same empty book through `Model.setAddressBook`; no second save occurs. The command has no storage dependency,
and `LogicManager` selects this path through the command contract rather than checking its concrete type.

Both execution paths use one `LogicManager.saveAddressBook` helper to map permission and general I/O errors.
A failed replacement save adds the command's recovery instructions while retaining the original I/O cause.
No live data has been changed at that point: contacts, the catalogue, filtering, sorting, and deletion history
remain available. The user can resolve the storage error and retry clearing, or use `list` to save the retained
data. Other commands retain their existing execution-before-save behavior.

The JSON writer still writes directly to its destination. A denied write leaves the existing file intact,
but a mid-write failure can leave incomplete disk data. The retained in-memory book can be saved with `list`
after the storage issue is resolved. This flow does not provide atomic file replacement or disk rollback.
Clearing does not add contacts to individual-deletion history, so it cannot be undone.
Earlier individual deletions remain available to `undo`.

Regression tests cover strict parsing, warning counts with hidden contacts, unchanged display and undo
history, skipped saves for unconfirmed or invalid input, and reloading the empty saved address book
after confirmed deletion.

`ClearSaveFailureIntegrationTest` covers permission and general I/O failures, a real temporary read-only JSON
file where the platform enforces its permissions, department-only books, preservation of filter/sort and
mixed deletion history, recovery with `list`, and successful retry with one save. It records live model state
during saving to verify that clearing has not happened yet. An interrupted-write case verifies that retained
live data can repair the JSON file. `CommandErrorUiTest` checks that the failure retains the input and data
and displays recovery instructions rather than clear-success feedback.

`ClearCommand.HELP_PAGE` describes the removal of all contacts and registered departments, including
unused catalogue entries. `HelpCatalog` summarizes the same scope in the command overview. The warning,
usage feedback, and `help clear` all identify departments as part of the data being cleared.

### Viewing contact details

The sequence below shows a successful `view 1` request for a nonempty displayed list. It resolves the
displayed index and returns the snapshot to the contact's details popup without saving JSON contact data.

![Successful contact-details sequence](images/ViewContactSequenceDiagram.png)

`AddressBookParser` routes `view CONTACT_ID` to `ViewCommandParser`, which validates the contact ID using
`ParserUtil.parseIndex`. `ViewCommand` resolves that contact ID against `Model.getFilteredPersonList()` so that
filtering and sorting determine the selected row. `CONTACT_ID` is the same one-based displayed-list
position used by `edit`, represented internally by `Index`. It reports an empty list or the valid contact ID range
before accessing the contact.

The command formats all currently supported fields as labelled, untruncated lines, sorts tags for stable output,
and returns a `CommandResult` with separate short feedback, the viewed `Person`, and an optional `contactDetails`
payload. `MainWindow` displays only the short feedback in the main result box and forwards the person and payload to
`ContactDetailsWindows`, which shows them in that contact's owner-linked `ContactDetailsWindow` popup containing a
read-only, wrapping field grid with selectable values inside a scroll pane. Each successful `view` updates
the contact's popup with a snapshot, or
opens a popup if the contact has none; later model changes do not silently change a displayed snapshot.

`ViewCommand.requiresStorageSave()` returns false. `LogicManager` returns the details result without calling
`Storage.saveAddressBook`, preserving contact data, the current filter and display order. Viewing therefore
succeeds even when the JSON destination cannot be written, and cannot overwrite an externally changed file.
Contacts are validated and loaded into the model at startup, so `view` does not re-read contact data from disk.

Keyboard handling in the popup:

* Only a plain `q` closes the popup. `q` with Shift, Ctrl, Alt or Meta goes to the focused value instead.
* Closing a popup with `q` returns focus to the command box, even if another pane had focus before.
  `MainWindow` passes this action to each popup through `ContactDetailsWindows`. Other popups stay open.
* Moving to a value with j/k, Up/Down, Tab or Alt+3 selects its whole text, so Ctrl+C copies it. `DetailsPanel`
  selects the text itself instead of waiting for a focus change, because no focus change happens when the value
  already had focus or the popup is not yet the active window.
* Each value handles j/k itself. A popup opened by `view` has no value focused until the user moves into it, so
  `DetailsPanel` also handles a plain j or k on its scroll pane by moving to the first value.

Department display reads `Person.getDepartment()` from the Department implementation tracked by issue #65.
The optional value is shown in full, or as `Department: Not provided` when absent. The `view` command uses
the same displayed-list position as `edit`, so setting a department and then viewing that contact remains
consistent after filtering.

### Showing command help

The sequence below shows a successful `help list` request, from command parsing to displaying the
`list` manual in the reusable help window.

![Successful help-command sequence](images/HelpCommandSequenceDiagram.png)

`AddressBookParser` creates a `HelpCommandParser`, which validates the optional topic and resolves its
`HelpPage` through `HelpCatalog.findPage`. For this request, the topic is `list`. The parser creates a
`HelpCommand` carrying that page. The diagram omits the static catalogue lookup and result-factory calls,
and summarizes their effects in notes.

`HelpCommand.execute(model)` returns a `CommandResult` containing the selected page, feedback and
`showHelp = true`. It does not change the model. Its `requiresStorageSave()` method returns false, so
`LogicManager` returns the result without saving the address book. `MainWindow` displays the feedback
and forwards the page to the existing `HelpWindow`. The window updates its heading and text, resets
scrolling, and shows or restores itself with focus on the help text.

With no topic, `help` or `?` selects the overview instead. Topic lookup is case-insensitive and accepts
aliases such as `ls`; unknown topics or multiple arguments produce a `ParseException` before command
execution. The Help menu and F1 call `MainWindow.handleHelp()` directly to display the overview,
bypassing command parsing. These alternative paths are omitted from the successful sequence above.

### Undoing a deletion

`undo` restores the most recent deletion in the current session: a contact removed with `delete`, or a department
removed with `depart --delete`. Only these two commands are recorded, so `undo` does not revert `add`, `edit`,
`depart --add`, `depart --edit`, `clear` or any other command, and there is no `redo`.

`ModelManager` keeps the deletion history in memory only. It is not written to the data file, so it is empty every
time the app starts. The history uses two lists, each used as a stack:

* `deletedPersons` holds every contact removed with `delete`.
* `deletionOrder` records every deletion in order, so contact and department deletions share one history. A contact
  deletion is recorded as `null`. A department deletion is recorded as a `DepartmentDeletion`, which holds the
  department and the contacts whose assignment it cleared.

`Model` exposes the history through these operations:

* `Model#deletePerson(Person)` — Removes the person and pushes it onto both lists.
* `Model#hasDeletedPerson()` — Returns true if `deletedPersons` is not empty.
* `Model#getLastDeletedPerson()` — Returns the most recent contact deletion without removing it from the history.
* `Model#restoreLastDeletedPerson()` — Adds the most recent contact deletion back and pops it from both lists.
* `Model#deleteDepartment(Department)` — Removes the department, clears its assignments and pushes a
  `DepartmentDeletion` onto `deletionOrder`.
* `Model#getLastDeletedDepartment()` — Returns the department if the most recent deletion was a department deletion,
  or `null` otherwise.
* `Model#restoreLastDeletedDepartment()` — Restores the most recent department deletion and pops it.

`DeleteCommand` is the only caller of `deletePerson`, and `DepartCommand` is the only caller of `deleteDepartment`,
so both record deletions without any extra code. The success message of `delete` ends with `Use 'undo' to restore.`

Given below is an example usage scenario.

Step 1. The user executes `delete 2`, which removes a contact in the Engineering department. `ModelManager` pushes
that contact onto `deletedPersons` and records the deletion in `deletionOrder`.

Step 2. The user executes `depart --delete Engineering`. `ModelManager` clears Engineering from every contact that has
it, including the contact deleted in step 1, and pushes a `DepartmentDeletion` onto `deletionOrder`.

Step 3. The user executes `undo`. The most recent deletion is the department, so `undo` restores Engineering and its
assignments, including the one on the contact deleted in step 1. A second `undo` restores that contact with its
Engineering assignment. A third `undo` fails with `There are no deletions to undo in this session.`

The following sequence diagram shows how `undo` restores a deleted contact through the `Logic` and `Model`
components. It leaves out the `Model#getLastDeletedDepartment()` call that runs first, which returns `null` when the
most recent deletion is a contact:

![Interactions Inside the Logic and Model Components for the `undo` Command](images/UndoDeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How `undo` works:

1. `AddressBookParser` passes the rest of the input to `UndoCommandParser`. If there is any argument (e.g. `undo 2`),
   the parser throws a `ParseException` with the usage message.
1. `UndoCommand#execute` calls `Model#getLastDeletedDepartment()`. If the most recent deletion was a department
   deletion, it checks `Model#hasDepartment(...)`. If that name has been registered again, it throws a
   `CommandException` and leaves the history unchanged. Otherwise, `Model#restoreLastDeletedDepartment()` adds the
   department back to the catalogue and reassigns it to the contacts it was cleared from that still have no
   department, including those in `deletedPersons`. It then pops the deletion and shows the full list.
   [Department management](#department-management-design-specification) explains how later edits to those contacts
   are tracked.
1. Otherwise, the most recent deletion is a contact deletion, or there is none. `UndoCommand#execute` checks
   `Model#hasDeletedPerson()`. If the history is empty, it throws a `CommandException` saying there is nothing to
   undo.
1. It reads the most recent contact deletion with `Model#getLastDeletedPerson()`. If that contact has neither a phone
   number nor an email address, which only happens with legacy data, it throws a `CommandException` and leaves the
   history unchanged.
1. It checks `Model#hasPerson(...)` for a [duplicate contact](#glossary) already in the list: one with the same name
   and a matching provided phone number or email address (`Person#isSamePerson`). If one exists, it throws a
   `CommandException` that names it. One matching field is enough, so a contact with a different email, or no email,
   still blocks `undo` if its phone matches. The deletion stays in the history, so the user can edit every
   conflicting identity field, or change the existing contact's name, and retry. Deleting a conflicting contact
   creates a newer history entry, so the next `undo` would target that newer deletion instead.
1. Otherwise, `Model#restoreLastDeletedPerson()` calls `Model#addPerson`. This appends the `Person` held in
   `deletedPersons` to the end of the stored list, so its tags and other fields are unchanged. Its department reflects
   any `depart --edit` or `depart --delete` run since the deletion. `addPerson` also shows the full
   list, which clears any `find` filter and `sort` order. The deletion is popped only after the add succeeds, so a
   failed add does not lose it.
1. `LogicManager` saves the address book, as it does after other commands that change it.

`clear` replaces the address book through `Model#setAddressBook`, which does not touch the deletion history. After
`clear`, `undo` can still restore contacts and departments that were deleted individually before it, but not the
contacts that `clear` removed.

#### Design considerations:

**Aspect: What `undo` records:**

* **Alternative 1 (current choice):** Only what each `delete` or `depart --delete` removed: the contact, or the
  department and the contacts it was cleared from.
  * Pros: Uses little memory. `undo` always brings back a deleted contact or department, even if other commands ran
    after the deletion.
  * Cons: Only these two commands can be undone. Each command that should become undoable needs its own reverse
    logic, as `depart --delete` did.

* **Alternative 2:** A copy of the entire address book after every command that changes it, as in AB3's proposed
  `VersionedAddressBook`.
  * Pros: Any command can be undone, and `redo` is straightforward to add.
  * Cons: Uses more memory. `undo` reverts the most recent change, which may be an `add` or `edit` rather than the
    deletion the user wants back.

**Aspect: Where the deletion history is kept:**

* **Alternative 1 (current choice):** In `ModelManager`.
  * Pros: Each `Model` has its own history, so tests do not share state. `ModelManager#equals` compares the history,
    so `CommandTestUtil.assertCommandSuccess` checks it too.
  * Cons: `Model` gains five methods, which every `Model` stub in the tests must implement.

* **Alternative 2:** In a static field of `DeleteCommand` or `UndoCommand`.
  * Pros: `Model` does not change.
  * Cons: A new command object is created for every input, so the history would have to be static. It would then be
    shared by every `Model`, including those in different tests.

### Command history and error feedback

`CommandBox` owns a session-local `CommandHistory`, separate from the model and JSON storage.
On submission it records the exact nonblank input before execution, including failed commands and duplicates.
It clears the command field after success and retains it after a parse or execution error.

Unmodified Up/Down keys are handled before the text field's default behavior. `previous(currentInput)` saves
the draft when leaving the newest position; `next()` restores that draft after the newest stored command.
Navigation stops at the ends. Editing a recalled command does not change the stored entry, and browsing
never executes commands. History is discarded when the app closes.

`MainWindow.executeCommand` passes errors to `CommandErrorFormatter.format`, which retains the original
error and appends up to two examples from the matching `HelpPage` when examples are not already present.
Unknown commands direct the user to `help`.
The command box receives its error style while preserving the submitted input for correction.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a secretary of a department head at a tech firm
* needs to be able to store contact information offline
* has a need to manage a significant number of contacts
* prefers desktop apps over other types of applications
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: sudoContact helps secretaries maintain professional contacts using short commands,
a shared department catalogue, tag searches and full contact details. It stores contacts locally and exports
all stored contacts to CSV for sharing; filtering the displayed list does not limit an export.


### User stories

The stories for hiding private details and sorting by name are future requirements; they are not available
in the current product. CSV export always includes all stored contacts.

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​                                    | I want to …​                                  | So that I can…​                                                               |
| -------- |-------------------------------------------|----------------------------------------------|------------------------------------------------------------------------------|
| `* * *`  | new user / forgetful user                 | see usage instructions                       | refer to instructions when I do not know/forget how to use the App           |
| `* * *`  | forgetful user                            | see usage instructions                       | refer to instructions when I do not know/forget how to use the App           |
| `* * *`  | user                                      | add a new person                             |                                                                              |
| `* * *`  | user                                      | delete a person                              | remove entries that I no longer need                                         |
| `* * *`  | user                                      | find a person by name                        | locate details of persons without having to go through the entire list       |
| `* * *`  | user                                      | record which department a contact belongs to | categorise employees for easier filtering                                    |
| `* * *`  | user                                      | search across all tags                       | find someone when I only remember where they work/fragmented info            |
| `* * *`  | user                                      | view one contact's full details              | see all the fields of a person that may be cut off in the list view          |
| `* * *`  | user                                      | add a contact with only partial information  | capture an person's incomplete information if I do not have all their info   |
| `* * *`  | user                                      | undo a delete                                | recover from a mistaken delete                                               |
| `* * *`  | user with many contacts                   | List all contacts                            | quickly review the contacts I have stored                                    |
| `* * *`  | user with many contacts                   | sort contacts by department/tags             | quickly review contact list of people with the same tag                      |
| `* * *`  | user                                      | use unix syntax                              | type faster coming from a unix background                                    |
| `* *`  | user                                      | generate a .csv of the contact list          | Easily copy and paste their email address to quickly send out an email blast |
| `* *`    | user                                      | hide private contact details                 | minimize chance of someone else seeing them by accident                      |
| `*`      | user with many persons in the address book | sort persons by name                         | locate a person easily                                                       |

### Use cases

(For all use cases below, the **System** is `sudoContact` and the **Actor** is the `user`, unless specified otherwise)

**Use Case: UC1 - List all contacts**

**MSS**

1. User requests to list all contacts
2. sudoContact shows a list of contacts

   use case ends

**Extensions**

* 2a. The list is empty.

  * 2a1. sudoContact shows an empty contact list and the normal `Listed all persons.` feedback.

  Use case ends.

**Use Case: UC2 - User sorts contacts by department/tag**

**MSS**

1. User displays contacts, either by <u>listing all contacts (UC1)</u> or filtering the list.
2. User requests ascending sorting using `sort -b FIELD` or `sort --by FIELD`.
3. sudoContact validates the option and field, then sorts only the currently displayed contacts.
4. sudoContact shows the sorted list and numbered feedback. The active filter, contact data, and saved order are unchanged.

   Use case ends.

**Extensions**

* 2a. The option or field is missing, invalid, repeated, or accompanied by unsupported arguments.
  * 2a1. sudoContact reports the corresponding parsing error; the current display order is unchanged.
  * Use case ends.
* 2b. The request contains an explicit `--descending` token.
  * 2b1. sudoContact reports that only ascending sorting is supported.
  * Use case ends.
* 3a. The displayed list is empty, including after contact loading failed at startup.
  * 3a1. sudoContact displays `No contacts found to sort.`
  * Use case ends.
* 3b. A displayed contact contains invalid department or tag data.
  * 3b1. sudoContact reports the invalid-data error without changing display order or writing the file.
  * Use case ends.
* 3c. Contact loading failed at startup and valid contacts were subsequently added in the same session.
  * 3c1. sudoContact sorts the current in-memory contacts normally; restarting is not required.
  * Use case ends.

**Use case: UC3 - Delete a contact**

**MSS**

1. User <u>lists all contacts (UC1)</u>.
2. User requests to delete a contact using its index in the currently
   displayed list.
3. sudoContact deletes the selected contact.
4. sudoContact displays the deleted contact's details and informs the
   user that the deletion can be undone.

   Use case ends.

**Extensions**

* 1a. User filters the contacts or <u>sorts contacts by
  department/tag (UC2)</u>.

  Use case resumes at step 2 using the resulting displayed list.

* 1b. The displayed list is empty.

  Use case ends.

* 2a. The deletion request has an invalid format or the index is
  missing, invalid, or outside the displayed list's range.

  * 2a1. sudoContact shows an error message and indicates the
    required format or valid index range.

  Use case resumes at step 2.

**Use case: UC4 - Undo a delete**

**MSS**

1. User requests to undo a deletion.
2. sudoContact restores the most recently deleted contact or department
   that has not yet been restored in the current session. A restored
   contact keeps its details; a restored department is reassigned to
   the contacts it was removed from that still have no department.
3. sudoContact displays the restored contact's details or the restored
   department's name.

   Use case ends.

**Extensions**

* 1a. The undo request includes additional arguments.

  * 1a1. sudoContact shows an error message explaining that undo
    does not accept arguments.

  Use case resumes at step 1.

* 1b. There are no deletions available to undo in the current session.

  * 1b1. sudoContact informs the user that there are no deletions
    left to undo.

  Use case ends.

* 2a. A duplicate contact already exists.

  * 2a1. sudoContact rejects the restoration and identifies the
    existing contact that prevents it.
  * 2a2. sudoContact retains the deleted contact for a later
    undo attempt.

  Use case ends.

* 2b. A department with the same name has been registered again.

  * 2b1. sudoContact rejects the restoration and names the department.
  * 2b2. sudoContact retains the deleted department for a later
    undo attempt.

  Use case ends.


**Use case: UC5a - List departments**

**MSS**

1. User enters `depart --list`.
2. sudoContact validates the command format.
3. sudoContact displays every registered department, including unused departments.

   Use case ends.

**Extensions**

* 2a. The command format is invalid.

  * 2a1. sudoContact displays the command format error.

  Use case ends.

* 3a. The catalogue is empty.

  * 3a1. sudoContact informs the user that no departments have been registered.

  Use case ends.


**Use case: UC5b - Add a department**

**MSS**

1. User enters `depart --add DEPARTMENT`.
2. sudoContact validates the name and checks that it is not already registered.
3. sudoContact adds the department, saves the data and confirms the addition.

   Use case ends.

**Extensions**

* 2a. The command format or department name is invalid.

  * 2a1. sudoContact displays an error and does not modify the catalogue.

  Use case ends.

* 2b. The department already exists.

  * 2b1. sudoContact informs the user that it is already registered.

  Use case ends.

* 3a. sudoContact cannot save the updated address book.

  * 3a1. sudoContact reports the save error instead of confirming the addition.
  * 3a2. The new department remains in memory because the change is not rolled back.

  Use case ends.


**Use case: UC5c - Rename a department**

**MSS**

1. User enters `depart --edit OLD_NAME NEW_NAME`.
2. sudoContact validates both names and checks the source and target catalogue entries.
3. sudoContact renames the department and updates all associated contacts, including hidden contacts.
4. sudoContact saves the data and confirms the rename.

   Use case ends.

**Extensions**

* 2a. The command format or either name is invalid.

  * 2a1. sudoContact displays an error and does not modify the catalogue or contacts.

  Use case ends.

* 2b. The source does not exist or the target name belongs to another registered department.

  * 2b1. sudoContact displays the corresponding error and leaves the data unchanged.

  Use case ends.

* 4a. sudoContact cannot save the updated address book.

  * 4a1. sudoContact reports the save error instead of confirming the rename.
  * 4a2. The renamed department and updated contact assignments remain in memory because the change is not
    rolled back.

  Use case ends.


**Use case: UC5d - Delete a department**

**MSS**

1. User enters `depart --delete DEPARTMENT`.
2. sudoContact validates the name and checks that the department exists.
3. sudoContact removes the department and clears its assignments from current contacts and deletion history.
4. sudoContact records the deletion for `undo`, saves the data and confirms the deletion.

   Use case ends.

**Extensions**

* 2a. The command format, name or existence check fails.

  * 2a1. sudoContact displays an error and leaves the catalogue, contacts and deletion history unchanged.

  Use case ends.

* 4a. sudoContact cannot save the updated address book.

  * 4a1. sudoContact reports the save error instead of confirming the deletion.
  * 4a2. The deleted catalogue entry, cleared assignments and undo record remain changed in memory because the
    operation is not rolled back.

  Use case ends.


**Use case: UC5e - Assign a department to a contact**

**MSS**

1. User enters `edit CONTACT_ID --department DEPARTMENT`.
2. sudoContact validates the displayed contact ID and checks that the department is registered.
3. sudoContact updates the selected contact, saves the data and confirms the edit.

   Use case ends.

**Extensions**

* 2a. The contact ID, department name or membership is invalid.

  * 2a1. sudoContact displays an error and leaves the contact unchanged.

  Use case ends.

* 3a. sudoContact cannot save the updated address book.

  * 3a1. sudoContact reports the save error instead of confirming the edit.
  * 3a2. The contact's updated department remains in memory because the change is not rolled back.

  Use case ends.


**Use case:** UC6 - Search contacts by tags

**MSS:**

1.  User requests to find contacts using one or more tags.
2.  sudoContact displays each contact having any of the specified tags once, using case-insensitive,
    whole-tag matching, and shows the number of matching contacts.

    Use case ends.

**Extensions:**

* 1a. A specified tag contains invalid characters.

  * 1a1. sudoContact displays `Tags must use only letters, numbers, hyphens (-), and underscores (_), with no spaces.`

  Use case ends.

* 1b. A tag value is missing.

  * 1b1. sudoContact displays `Tag cannot be empty: Please provide a valid tag after '--tag'.`

  Use case ends.

* 1c. An unsupported option is supplied, either instead of or after a valid `--tag`
  (e.g. `find --tags friends`, `find --TAG friends`, or `find --tag friends --tags family`).

  * 1c1. sudoContact displays `Invalid find option: '[INPUT]'. Use find --tag TAG [TAG ...] [--tag TAG ...] or find KEYWORD [MORE_KEYWORDS].`

  Use case ends.

* 1d. The command has no arguments or arguments precede the first valid `--tag` option
  (e.g. `find Alice --tag friends`).

  * 1d1. sudoContact displays an invalid command format error and the `find` usage instructions.

  Use case ends.

* 1e. The command contains an unmatched quote or text immediately after a closing quote.

  * 1e1. sudoContact displays the corresponding quotation error.

  Use case ends.

* 2a. No contacts match the specified tag(s).

  * 2a1. sudoContact displays `No contacts found with the specified tag(s).`

  Use case ends.

**Use case: UC7 - View one contact's full details**

**MSS**

1. User displays a list of contacts (UC1), which may be filtered or sorted.
2. User enters `view CONTACT_ID`, using the one-based row number in that displayed list.
3. sudoContact identifies the contact from the current in-memory list.
4. sudoContact opens or refreshes that contact's popup with its full details and shows a short confirmation.

   Use case ends.

**Extensions**

* 2a. The command format or contact ID is invalid.

  * 2a1. sudoContact reports the required format and positive-integer constraint without changing data.

  Use case resumes at step 2.

* 3a. The displayed list is empty.

  * 3a1. sudoContact reports that there is no contact to view.

  Use case ends.

* 3b. The contact ID exceeds the displayed list's size.

  * 3b1. sudoContact reports the valid contact ID range without opening or updating a popup.

  Use case resumes at step 2.

Viewing preserves contacts, filtering and sorting and skips the JSON save. A disk read/write failure is
not an extension of this use case; startup loading and mutating-command saves handle those failures.

**Use case: UC8 - Add a contact with partial information**

**MSS**

1. User enters `add --name NAME` with at least a phone number or email address,
   including any other information they know, such as department or tags.
2. sudoContact validates the supplied information and checks for a contact
   with the same name and a matching provided phone number or email address. A matching name
   alone does not prevent the contact from being added; omitted fields do not match.
3. sudoContact saves the new contact with the supplied information, leaving
   omitted optional fields unprovided.
4. sudoContact confirms that the contact was added and displays the full contact list in stored order.
   Missing values appear as `—` in the table; feedback and full details use `Not provided` or, for tags, `None`.

   Use case ends.

**Extensions**

* 2a. The name is missing or empty, or both phone and email are omitted.

  * 2a1. sudoContact displays an error and does not add the contact.

  Use case resumes at step 1.

* 2b. A supplied field is invalid, an option is unknown, or the command
  format is invalid.

  * 2b1. sudoContact displays an error and does not add the contact.

  Use case resumes at step 1.

* 2c. A duplicate contact already exists.

  * 2c1. sudoContact informs the user that the contact is a duplicate
    and does not add it.

  Use case ends.

* 3a. sudoContact cannot save the new contact.

  * 3a1. sudoContact reports the save failure without confirming persistence. The contact remains in
    memory; the change is not rolled back. After resolving the storage problem, the user runs `list` to
    retry saving before exiting.

  Use case ends.

**Use case: UC9 - Export contacts to a CSV file**

**MSS**

1. User enters `export -c|--csv [FILENAME]`.
2. sudoContact validates the supplied filename, or selects `contacts.csv`
   if no filename was supplied.
3. sudoContact snapshots all contacts from the current in-memory address book, including hidden contacts.
4. sudoContact creates a UTF-8 CSV file with a header row and a separate
   row for each contact. It escapes values containing commas or quotation
   marks.
5. sudoContact confirms the number of contacts exported and the filename.

   Use case ends.

**Extensions**

* 2a. The supplied filename is invalid.

  * 2a1. sudoContact displays an error and does not export the contacts.

  Use case resumes at step 1.

* 3a. There are no contacts to export.

  * 3a1. sudoContact informs the user that the contact list is empty.

  Use case ends.

* 4a. A file or directory already exists at the chosen destination.

  * 4a1. sudoContact asks for a different filename and preserves the existing destination.

  Use case resumes at step 1.

* 4b. The CSV file cannot be created or written.

  * 4b1. sudoContact reports the file error without confirming a successful export and removes its
    temporary file. Contacts, display state and the JSON data file remain unchanged.

  Use case ends.

**Use Case: UC10 - View available commands and their usage**

**MSS**

1. User requests the command overview with `help` or `?`.
2. sudoContact opens the Help window with command names, aliases and short descriptions.
3. User requests detailed help with `help COMMAND` or `? COMMAND`.
4. sudoContact shows that command's syntax, options, examples and behavior notes in the same window.

   Use case ends.

**Extensions**

* 1a. User requests a specific command directly instead of the overview.

  Use case proceeds at step 4.

* 2a. User only needs the overview.

  Use case ends.

* 3a. The requested command is unknown or too many arguments are supplied.

  * 3a1. sudoContact reports the error in the main window and leaves any open help page unchanged.

  Use case resumes at step 3.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ with Java `25` installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  The system should maintain responsiveness with command execution times of under 1 second when handling a dataset of up to 1,000 records under typical usage conditions.
4.  The system should provide smooth scrolling when displaying a dataset of up to 1,000 records under typical usage conditions.
5.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
6.  Contact data must be stored locally in a human-editable text file (e.g., JSON). If the file is missing, malformed, or corrupted at startup, the application must show a clear diagnostic message and either recover safely or exit without an unhandled exception.
7.  After a successful add, edit, or delete command, the updated contact list must be restored when the application is closed and reopened.
8.  If saving contact data fails, the application must report the failure and leave the last successfully saved data file intact.

### Glossary

* **Contact**: A stored record for a person or organisation, containing a name and any available contact details, department, and tags.
* **Contact ID**: A positive one-based position in the currently displayed list, used by `view`, `edit`, and `delete`. Filtering or sorting can change which contact occupies a position.
* **Department**: An optional organisational unit associated with a contact. Under this specification, assignments must use a name from the shared department catalogue; renaming an entry updates all associated contacts.
* **Department catalogue**: The complete set of registered department names, including unused entries, managed with `depart --list`, `depart --add`, `depart --edit`, and `depart --delete`.
* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Matching contact**: For a tag search, a contact that has at least one of the tags specified in the command (OR matching). A matching contact is displayed once even when it has more than one specified tag.
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Currently displayed list**: The contacts shown after filtering or sorting. The one-based `CONTACT_ID` used by `view`, `edit` and `delete` refers to a position in this list, which can differ from stored order.
* **Stored contact list**: All contacts in the address book's canonical in-memory order, including contacts hidden by a filter. A normal JSON save persists this list. CSV export uses the entire list; display sorting does not reorder it.
* **Partial contact**: A contact with some optional details omitted. New contacts require a name and at least one phone number or email address; older saved name-only contacts remain loadable.
* **Omitted contact detail**: An unknown optional field not supplied when adding a contact. Absent phone, email, address and department show `Not provided`; no tags shows `None`. Explicitly supplied empty phone/email/address values are rejected.
* **Duplicate contact**: An existing contact with the same name and a matching provided phone number or email address as a contact being added, edited, or restored. Missing fields do not match. Comparisons are case-sensitive.
* **CSV export**: A UTF-8 comma-separated values file containing a header row and one row for each stored contact.
* **Tag**: An optional label attached to a contact to support categorisation and searching. Tag matching is case-insensitive.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Use a separate writable folder and a copy of the JAR for each independent test section. Do not use a
folder containing contacts you want to keep. Run `java -version` to verify Java 25, then launch with
`java -jar "sudoContact.jar"` from that folder (substitute the actual downloaded filename).
Unless a section gives another fixture, a fresh folder starts with the built-in sample contacts.

The examples below complement the UG; vary the inputs during exploratory testing. Where a test checks
file contents, close the app before editing files and preserve a backup before simulating corruption.

### Listing and searching

Use a fresh disposable folder with the built-in sample contacts.

1. Enter `find Alex`. Expected: only Alex Yeoh is displayed. Enter `ls`.
   Expected: all six sample contacts reappear in stored order.
1. Enter `sort --by tags`, then `list`. Expected: `list` restores stored order, and indices match that order.
1. Enter `find --tag friends --tag colleagues`. Expected: Alex Yeoh, Bernice Yu and Roy Balakrishnan appear;
   Bernice appears once even though more than one requested tag matches.
1. Compare `find --tag friends` and `find --tag FRIENDS`. Expected: both display Alex Yeoh and Bernice Yu;
   tag matching ignores case. Enter `find --tag NoSuchTag`. Expected: the no-matches message and an empty list.
1. Try `find --tag`, `find --TAG friends` and `list extra`. Expected: specific validation/format errors;
   no contacts are changed. Enter `list` to restore the display.

### Command help and history

1. Enter `help`. Expected: an overview of command names, aliases and short descriptions appears.
   Enter `help export`. Expected: the same window shows export syntax, options and examples.
1. Enter `help ls` and `? sort`. Expected: the corresponding manuals appear. Enter `help missing` or
   `help list extra`. Expected: an error in the main window, while the last valid help page remains.
1. Focus Help and press unmodified `q`. Expected: Help closes. Open it through the Help menu or F1.
   Expected: the overview reopens, rather than the previous command-specific page.
1. With the command prompt focused, submit `list`, then the invalid command `help missing`.
   Press Up twice. Expected: the failed command and then `list` are recalled without being executed.
1. Press Down until the newest draft is restored. Type `find Alex` without submitting, press Up and then Down.
   Expected: the exact draft `find Alex` is restored. Browsing stops at each end; it does not wrap.
1. Exit and relaunch from the same folder. Press Up in the prompt.
   Expected: the previous session's history is unavailable; contacts and saved preferences remain.

### Clearing contacts with confirmation

Use disposable data, record the full stored contact count, and register at least one unused department before testing.

1. Enter `clear`. Expected: the warning reports the full stored contact count, states that all registered
   departments will also be deleted, and explains `clear --confirm`. All contacts and departments remain.
   Restart and check that both stored collections are unchanged.
1. Filter with `find`, then enter `clear`. Expected: the warning counts all stored contacts,
   including hidden contacts; the displayed search result remains unchanged.
1. Enter `clear 1`, `clear --CONFIRM`, `clear --confirm extra`, and `clear --confirm --confirm`.
   Expected: each produces an invalid command format error; no contacts are deleted.
1. Enter `clear --confirm`. Expected: all contacts and registered departments disappear, including hidden
   contacts and unused department entries. Restart and verify both collections are empty.
1. With no earlier individual deletions in the session, enter `undo` after a confirmed clear.
   Expected: there are no deletions to undo. Separately, delete one contact before clearing and
   verify that `undo` restores only that earlier individual deletion.
1. In an address book with no contacts, register a department with `depart --add Engineering`, then enter
   `clear`. Expected: the warning has a contact count of 0 and still states that all registered departments
   will be deleted. No data changes.
1. Enter `help clear`. Expected: the manual states that confirmed clearing deletes all contacts and registered
   departments, including unused entries. The help overview also mentions contacts and departments.

1. In disposable data, register Engineering and an unused department, then add Alice to Engineering.
   Apply a filter and sort, and record the saved JSON contents. Deny writes to the configured JSON file
   using permissions enforced by the current account, then run `clear --confirm`.
   Expected: a save error states that clear was not applied and explains how to recover. Contacts, departments,
   the displayed filter/order, and earlier deletion history remain unchanged. A permission-denied file retains
   its original bytes. Restore write permission and run `list`; the original contacts and catalogue remain
   in the saved JSON. Retry `clear --confirm`; only a successful save clears the live data.

### Exporting contacts as CSV

Use a fresh disposable launch folder with the built-in sample contacts and no CSV files.

1. Enter `find Alex`, then `export --csv team.csv`. Expected: feedback reports the full stored contact
   count; the displayed search stays unchanged. Open `team.csv` as UTF-8 text and check the six-column
   header, all stored contacts, quoted addresses containing commas, empty departments and tag cells.
1. Enter `export --csv team.csv` again. Expected: an existing-file error; the original CSV is unchanged.
1. Enter `export --csv`. Expected: `contacts.csv` is created with the same contents (if the name is unused).
1. Enter `export --csv "my contacts.csv"`. Expected: the file with spaces in its name is created.
1. Enter `export --csv ../contacts.csv`, `export --csv NUL.csv`, `export --csv ''`, `export --csv a.txt`,
   `export --json` and `export --csv a.csv b.csv`. Expected: an error and no new export file.
1. Enter `add --name "Csv Probe" --email csv@example.com --address '北京, "总部"'`, then export to an unused
   filename. Expected: the Unicode address is intact, surrounded by quotes, with each inner quote doubled.
1. In a disposable address book, enter `clear --confirm`, then export to an unused filename. Expected:
   `No contacts found to export.`; no file is created. An empty *search result* with stored contacts should
   still export those contacts successfully.
1. Launch from a folder without write permission using a non-administrator account, keeping JSON data
   in a writable location through the app configuration. Export to an unused filename. Expected: a file
   creation error; no success message or partially written destination. Restore permissions afterward.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Copy the JAR to an empty writable folder, open a terminal there and check `java -version` reports Java 25.
   Launch with `java -jar "sudoContact.jar"`. Expected: the four-pane sudoContact window opens with sample contacts.
1. Resize and move the main window, then enter `exit`. Relaunch with the same terminal command from the same
   folder. Expected: the saved window size and location are restored.
1. Open Help and two contact-details popups, then exit through the main window.
   Expected: every application window closes.
1. Start a second disposable instance from another folder. Expected: it uses that folder's separate data
   and preferences, rather than the first folder's contacts.

### Department catalogue and assignments

Deletion check: assign a department to two contacts and delete one contact. Filter out the other contact,
then run `depart --delete DEPARTMENT`. Verify the entry disappears and the hidden contact has no department.
The first `undo` restores the department and its assignments; the second restores the deleted contact with its
department. Check the reverse operation order too. Verify later name/phone edits survive restoration, explicit
reassignment is preserved, and a re-registered department blocks undo without consuming history.
Restart after successful restoration and verify saved contacts and catalogue. History itself does not survive restart.

For the department assignment examples below, register the required names first with `depart --add`
(Engineering, "R&D Engineering", Sales, and Support as applicable).

1. With an empty catalogue, run `depart --list`. Expected: an explicit empty-catalogue result.
2. Run `depart --add Engineering` and `depart --add "Customer Support"`, then `depart --list`.
   Expected: both names appear even when no contacts use them. Restart and confirm both remain.
3. Assign Engineering to two contacts with `edit CONTACT_ID --department Engineering` and filter to show only one.
   Run `depart --edit Engineering "Research & Development"`.
   Expected: the catalogue and both contacts use the new name; unrelated fields are unchanged.
   Restart and confirm the rename persists. The old name is no longer available for assignment.
4. Run `edit 1 --department "Customer Support"`. Expected: only the selected contact's assignment changes.
   Edit its phone without a department option. Expected: its department is preserved.
5. Run `edit 1 --department Unregistered --phone 91234567` with Unregistered absent from the catalogue.
   Expected: the entire edit fails; neither department nor phone changes.
6. Try adding an existing name, renaming an unknown source, renaming to another existing entry,
   empty or invalid names, missing or extra arguments, and combined `depart` operations.
   Expected: errors with no catalogue or contact changes.
7. Apply a contact filter and sort, then run `depart --list`.
   Expected: every registered department is listed; the filter and displayed contact order remain unchanged.
8. Sort contacts and use `edit 1 -d "Customer Support"`.
   Expected: the first displayed contact is updated. Try invalid and out-of-range contact IDs;
   expect an error with no data changes.

### Adding and completing a partial contact

Start in a separate disposable folder with no contact named Dana Lim, Chen or Bob Lee.
Register Engineering using `depart --add Engineering` before the assignment steps below.

1. Try `add --name "Dana Lim"` and `add --name "Dana Lim" --address Main Road`.
   Expected: both fail with `A phone number or email address must be provided.`, without changing contacts or storage.
1. Add `add --name "Dana Lim" --email dana@example.com` to an address book that has no Dana Lim contact.
   Expected: the table shows `—` for missing fields. Addition feedback and `view` show `Not provided`
   for phone/address/department and `None` for tags; email is present.
1. Restart the app. Expected: Dana Lim is still present with the same omitted fields.
1. Run `find Dana`, then `edit 1 -p 91234567 -e dana@example.com -a Main Road --department Engineering`.
   Expected: the contact has the supplied details; restart preserves all fields.
1. Run `find Dana`, then `edit 1 -p 123`, `edit 1 -p 1234567890123456` and `edit 1 -t #intern`.
   Expected: both phone edits are rejected with `Invalid phone number: Must contain 7-15 digits.`, as in `add`;
   the tag edit gives Dana Lim the tag `intern`, without the `#`.
1. Add another `Dana Lim` with both a different phone and a different email. Expected: the same name is accepted.
   Reuse either contact's phone or email with the same name, even while changing the other field.
   Expected: duplicate error, no new entry. Try the same collisions through `edit` and `undo` too.
1. Try `add --name ""`, `add --name Alice --phone 123456`, `add --name Alice --email broken`,
   `add --name Alice --department !`, `add --name Alice --tag "two words"`, and
   `add --name Alice --unknown value`. Expected: errors; existing contacts and the saved file stay unchanged.
1. Try an unmatched quote and repeated `--name`, `--phone`, `--email`, `--address` or `--department`.
   Expected: format or duplicate-option error. Multiple `--tag` flags are accepted.
1. Run `add --name Chen --phone 91234567 --tag #intern new-client team_intern --tag intern`.
   Expected: three distinct tags, displayed without the leading `#` and preserved after restart.
1. Run the equivalent short-flag form, for example `add -n Bob Lee -p 91234567 -d Engineering`.
   Expected: equivalent partial-contact support; omitted fields remain unprovided.

### Viewing a contact's details

In a separate disposable folder, prepare the following fixture (one command per line):

```text
clear --confirm
depart --add "R&D Engineering"
depart --add Sales
add --name "Betsy Crowe" --phone 91234567 --email betsy@example.com
add --name "Benson Meier" --phone 92345678 --email benson@example.com --tag vendor client
```

1. Displayed-list contact IDs

   1. Run `list`, then `view 2`. Expected: the second contact's supported fields appear in a details popup.
   1. Run `find Betsy`, then `view 1`. Expected: Betsy's details appear, and the filtered list remains unchanged.
   1. Run `view 2` when only one search result is displayed. Expected: an error gives the range `1-1`.
   1. Run `find NoSuchContact`, then `view 1`. Expected: an error says there is no contact to view.
   1. Try `view`, `view 0`, `view -1`, `view 1.5`, `view 1 2`, and `view 2147483648`.
      Expected: each reports that the contact ID must be a positive integer and shows the usage.

1. Details popups

   Run `list` before this group to restore the contacts hidden by the previous empty search.

   1. View a contact with a long name/address and multiple tags. Resize the details popup and scroll.
      Expected: all values remain readable without truncation; tags are separated by commas.
      The main result box shows only `Showing details of contact CONTACT_ID: NAME`.
   1. View a contact without tags. Expected: the Tags field shows a dimmed `None`.
   1. View a contact without a department. Expected: the Department field shows a dimmed `Not provided`.
   1. Press Alt+3 until a popup has the focus, then press `j` and `Ctrl+C` (`Cmd+C` on macOS).
      Expected: the phone number is highlighted and copied to the clipboard.
   1. Close all contact popups, run `view 1`, click the new popup's title bar, then press `j` twice.
      Expected: the name is highlighted first, then the phone number.
   1. Run `find Benson`, then `edit 1 --department "R&D Engineering"`. Run `find Benson` again, then `view 1`.
      Expected: Benson's details include `Department: R&D Engineering`; `view` preserves the current filter.
   1. Change the selected contact's department with `edit 1 --department Sales`.
      Expected: the details snapshot stays unchanged. Run `find Benson` again, then `view 1`;
      the refreshed snapshot displays `Department: Sales`.
   1. With Benson's popup open, run `edit 1 --name Benson Renamed`, `find Benson`, then `view 1`.
      Expected: the existing popup refreshes and is titled `Contact details: Benson Renamed`; no second popup opens.
   1. Close all contact popups, then run `list`, `view 1` and `view 2`. Expected: exactly two popups are open,
      offset from each other. Both stay in front of the main window, and the prompt keeps the keyboard focus.
   1. Run `view 1` again. Expected: its existing popup updates rather than a third popup opening.
   1. Press Alt+3 repeatedly. Expected: the focus moves to \[3\] Details, then through each open popup,
      then back to \[3\] Details. Press Esc.
      Expected: the focus returns to the prompt.
   1. Focus \[2\] Contacts, move into a popup with Alt+3 and press unmodified `q`.
      Expected: only that popup closes and the command prompt receives focus, ready for another command.
      Shift/Ctrl/Alt/Meta+q does not close the popup.
   1. Close a contact's popup, then view that contact again. Expected: a popup reopens with the current details.
   1. Click a row in \[2\], then move with `j`/`k`.
      Expected: \[3\] Details shows each selected contact at once, and no popup opens.
   1. Select a row in \[2\] with Alt+2 and `j`/`k`, then press Enter. Separately, double-click another row.
      Expected: each gives the same result as running `view` with that row's contact ID.
   1. Edit the viewed contact. Expected: the snapshot stays unchanged until another `view` command is run.
   1. Exit the application while several popups are open. Expected: every popup closes with the main window.

1. Data and persistence

   Relaunch from the same folder after the previous group's exit step.

   1. Filter contacts, then run a valid `view` command. Compare contact values before and after.
      Expected: contact data, the filter, display order and JSON file remain unchanged.
   1. Run `view` when the save location cannot be written (for example, configure a directory as the JSON path).
      Expected: the details popup opens or refreshes normally, without a save error or a write attempt.
   1. With contacts already loaded, change the JSON file externally and run `view`.
      Expected: the current in-memory contact is displayed; the external file is neither read nor overwritten.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Saving data

1. Missing data file: in a fresh disposable folder, launch without `data/addressbook.json`.
   Expected: sample contacts appear. Run a valid `add` and restart; the saved contacts are restored.
1. Invalid data file: close the app, back up the JSON file and replace its contents with `{`.
   Restart. Expected: an empty address book and a loading warning in the terminal/log; the invalid file is
   still present. `view 1` reports an empty list, and `sort --by tags` reports no contacts without replacing it.
1. Restore the valid backup while the app is closed, then relaunch.
   Expected: every contact and department loads from the restored file.
1. Save failure: in a separate disposable folder, replace the configured JSON file with a directory of the
   same name while the app is closed, then launch and run `add --name "Save Probe" --email probe@example.com`.
   Expected: a save error, but the new contact remains in the current in-memory list. Exit without assuming
   persistence. Restore a writable file destination and repeat to check successful save/reload.
1. With valid contacts loaded, make the JSON destination unwritable and run `view 1`.
   Expected: viewing still succeeds because it is read-only. After restoring write access, run `list`
   to persist the current model and restart to verify the data.

### Sorting contacts

1. Add contacts with multiple tags, including mixed-case tags and contacts without tags.
2. Run `sort --by tags`; confirm ordering by the smallest tag, missing tags last, and updated displayed indices.
3. Register departments with `depart --add DEPARTMENT` and assign them using `edit CONTACT_ID --department DEPARTMENT`.
   Run `sort -b dept`; confirm alphabetical order and absent departments last.
4. Run `find NAME`, then sort; confirm hidden contacts remain hidden. Run `list` to restore the normal order.
5. Sort again and edit or delete index 1; confirm the first displayed contact is affected and the remaining display resets.
6. Try a repeated option, an invalid field, and `--descending`; confirm exact errors without changing display order.
7. Compare the contact data file before and after sorting; its contents should be unchanged.
8. Give two contacts departments that differ only by case. Sort by tags, then by department; confirm their
   relative order from the tag sort is retained. Repeat with missing fields to confirm those contacts appear last.
9. After sorting, run `view 1`, then `edit 1 --department Support`; confirm both target the displayed first contact.
   Check the new indices after assignment resets sorting. Delete a sorted contact and run `undo`; confirm its
   department and tags are restored and the full list returns to canonical order.
10. Using a disposable copy of the application, start with malformed contact JSON; confirm the list is empty and
    sorting reports `No contacts found to sort.` without overwriting the file. Add two valid contacts with different
    tags and confirm they are saved. Sort by tags in the same session; confirm sorting succeeds and the saved file
    remains unchanged. Repeat after a startup file-read failure once the file is writable again.
