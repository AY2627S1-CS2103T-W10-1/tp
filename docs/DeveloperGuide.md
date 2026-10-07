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
* He Qianyi used OpenAI Codex to assist with the feature 3 `view INDEX` implementation, including the
  command/parser, details window, command-result and read-only persistence integration, tests, and related
  User Guide/Developer Guide documentation.

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

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
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

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

### Viewing contact details

`AddressBookParser` routes `view INDEX` to `ViewCommandParser`, which validates the index using
`ParserUtil.parseIndex`. `ViewCommand` resolves that index against `Model.getFilteredPersonList()` so that
filtering (and future sorting) determines the selected row. It reports an empty list or the valid index range
before accessing the contact.

The command formats all currently supported fields as labelled, untruncated lines, sorts tags for stable output,
and returns a `CommandResult` with `showDetails` set. `MainWindow` forwards the details to a reusable,
owner-linked `ContactDetailsWindow` containing a read-only, wrapping, scrollable text area. Each successful
`view` updates the window with a snapshot; later model changes do not silently change the displayed snapshot.

`ViewCommand.isReadOnly()` returns true, allowing `LogicManager` to return its result without writing storage.
Other commands retain their existing persistence behaviour. Contacts are already validated and loaded into the
model at startup, so `view` does not re-read potentially modified or corrupted files on disk.

Department display will be added when the Department model implementation tracked by issue #65 is available.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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

**Value proposition**: sudoContact is a lightweight address book that strips away clunky GUIs. It allows you to search, update and route VIP contact data instantly using standard Unix syntax. SudoContact can instantly pipe a filtered list of investors directly into a csv for quick sharing.


### User stories

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

*{More to be added}*

### Use cases

(For all use cases below, the **System** is the `AddressBook` and the **Actor** is the `user`, unless specified otherwise)

**Use Case: UC1 - List all contacts**

**MSS**

1. User requests to list all contacts
2. AddressBook shows a list of contacts

   use case ends

**Extensions**

* 1a. The list is empty.
*    1a1. shows a message indicating that there are no contacts.

  Use case ends.

**Use Case: UC2 - User sorts contacts by department/tag**

**MSS**

1. User <u>lists all contacts (UC1)</u>
2. User sorts the contact by either the department or tag field.
3. AddressBook shows a sorted list of contacts according to field selected.

   use case ends

**Use case: UC3 - Delete a contact**

**MSS**

1. User <u>lists all contacts (UC1)</u>.
2. User requests to delete a contact using its index in the currently
   displayed list.
3. AddressBook deletes the selected contact.
4. AddressBook displays the deleted contact's details and informs the
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

  * 2a1. AddressBook shows an error message and indicates the
    required format or valid index range.

  Use case resumes at step 2.

**Use case: UC4 - Undo a delete**

**MSS**

1. User requests to undo a deletion.
2. AddressBook restores the most recently deleted contact that has
   not yet been restored in the current session, including its details.
3. AddressBook displays the restored contact's details.

   Use case ends.

**Extensions**

* 1a. The undo request includes additional arguments.

  * 1a1. AddressBook shows an error message explaining that undo
    does not accept arguments.

  Use case resumes at step 1.

* 1b. There are no deletions available to undo in the current session.

  * 1b1. AddressBook informs the user that there are no deletions
    left to undo.

  Use case ends.

* 2a. An existing contact has both the same phone number and
  email address as the contact being restored.

  * 2a1. AddressBook rejects the restoration and identifies the
    existing contact that prevents it.
  * 2a2. AddressBook retains the deleted contact for a later
    undo attempt.

  Use case ends.

**System:** sudoContact

**Use case:** UC5 - Record which department a contact belongs to

**Actor:** User

**MSS:**

1.  User requests to assign a department to a contact.
2.  User enters `depart CONTACT_ID --set DEPARTMENT`.
3.  sudoContact validates the contact ID and department.
4.  sudoContact assigns the specified department to the contact.
5.  sudoContact displays a confirmation showing the contact and its updated department.

    Use case ends.

**Extensions:**

* 3a. The contact ID format is invalid.

    * 3a1. sudoContact displays `Invalid Contact ID: Must be a positive integer.`

      Use case ends.

* 3b. The specified contact does not exist.

    * 3b1. sudoContact displays `Contact non-existent: No contact found with ID '[INPUT]'.`

      Use case ends.

* 3c. The department value is missing.

    * 3c1. sudoContact displays `Department name cannot be empty.`

      Use case ends.

* 3d. The department value does not satisfy the required format or length.

    * 3d1. sudoContact displays `Invalid Department: Must be 2-50 characters using only letters, numbers, spaces, '-', or '&'.`

      Use case ends.

* 4a. The contact already has a department.

    * 4a1. sudoContact overwrites the existing department with the newly specified department.

      Use case resumes at step 5.

**System:** sudoContact

**Use case:** UC6 - Search contacts by tags

**Actor:** User

**MSS:**

1.  User requests to search for contacts with one or more tags.
2.  User enters a `find` command with one or more `--tag` values.
3.  sudoContact validates all specified tags.
4.  sudoContact searches the contacts for those matching the specified tags.
5.  sudoContact displays each matching contact once.
6.  sudoContact displays the matching contacts to the user.

    Use case ends.

**Extensions:**

* 3a. A specified tag contains invalid characters or spaces.

    * 3a1. sudoContact displays `Invalid Tag: '[INPUT]' must be 1-30 characters using only letters, numbers, hyphens (-), and underscores (_), with no spaces.`

      Use case ends.

* 3b. A tag value is missing.

    * 3b1. sudoContact displays `Tag cannot be empty: Please provide a valid tag after '--tag'.`

      Use case ends.

* 4a. No contacts match the specified tag(s).

    * 4a1. sudoContact displays `No contacts found with the specified tag(s).`

      Use case ends.

* 4b. Multiple tags are specified and a contact matches more than one of them.

    * 4b1. sudoContact displays the matching contact only once.

      Use case resumes at step 5.


**Use case: UC8 - View one contact's full details**

**System:** sudoContact

**Actor:** User

**MSS**

1. User displays a list of contacts (UC1), which may be filtered or sorted.
2. User enters `view INDEX`, using an index in the currently displayed list.
3. sudoContact identifies the contact at that index.
4. sudoContact displays all available details of that contact, including
   fields not shown in the list.

   Use case ends.

**Extensions**

* 1a. The displayed list is empty.

  * 1a1. sudoContact informs the user that there is no contact to view.

  Use case ends.

* 2a. The command format or index is invalid, or the index is outside
  the currently displayed list.

  * 2a1. sudoContact displays an error and indicates the required format
    or valid index range.

  Use case resumes at step 2.

* 3a. The selected contact's stored data cannot be read.

  * 3a1. sudoContact reports that the contact details cannot be displayed.

  Use case ends.

**Use case: UC9 - Add a contact with partial information**

**System:** sudoContact

**Actor:** User

**MSS**

1. User enters `add --name NAME`, including any optional information they
   know, such as a phone number, email address, department, or tags.
2. sudoContact validates the supplied information and checks for a contact
   with the same name, phone number, and email address. A matching name
   alone does not prevent the contact from being added.
3. sudoContact saves the new contact with the supplied information, leaving
   omitted optional fields unprovided.
4. sudoContact confirms that the contact was added and shows omitted fields
   as `Not provided` or, for tags, `None`.

   Use case ends.

**Extensions**

* 2a. The name is missing or empty.

  * 2a1. sudoContact displays an error and does not add the contact.

  Use case resumes at step 1.

* 2b. A supplied field is invalid, an option is unknown, or the command
  format is invalid.

  * 2b1. sudoContact displays an error and does not add the contact.

  Use case resumes at step 1.

* 2c. A contact with the same name, phone number, and email address
  already exists.

  * 2c1. sudoContact informs the user that the contact is a duplicate
    and does not add it.

  Use case ends.

* 3a. sudoContact cannot save the new contact.

  * 3a1. sudoContact reports the failure without confirming that the
    contact was added.

  Use case ends.

**Use case: UC10 - Export contacts to a CSV file**

**System:** sudoContact

**Actor:** User

**MSS**

1. User enters `export --csv [FILENAME]`.
2. sudoContact validates the supplied filename, or selects `contacts.csv`
   if no filename was supplied.
3. sudoContact retrieves all stored contacts.
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

* 3b. Contact data cannot be read.

  * 3b1. sudoContact reports that the export could not be completed.

  Use case ends.

* 4a. The CSV file cannot be created or written.

  * 4a1. sudoContact reports the file error without confirming a
    successful export.

  Use case ends.

**Use Case: UC11 - View available commands and their usage**
 
  **Actor:** User
 
  **MSS**
 
  1. User enters `help`.
  2. sudoContact displays the available commands and their usage.
  3. User enters `help COMMAND` to learn more about a specific command.
  4. sudoContact displays the usage of that command.
 
     Use case ends.
 
  **Extensions**
 
* 1a. User enters `help COMMAND` directly.
    * 1a1. Use case resumes at step 4.

* 3a. User enters `help COMMAND` wrongly/for a command that does not exist
    * 3a1. Inform user it is an unrecognized command

      Use case ends
 
* 4a. The specified command does not exist.
    * 4a1. sudoContact displays `Command not found`.
 
      Use case ends

*{More to be added}*

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  The system should maintain responsiveness with command execution times of under 1 second when handling a dataset of up to 1,000 records under typical usage conditions.
4.  The system should provide smooth scrolling when displaying a dataset of up to 1,000 records under typical usage conditions.
5.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
6.  Contact data must be stored locally in a human-editable text file (e.g., JSON). If the file is missing, malformed, or corrupted at startup, the application must show a clear diagnostic message and either recover safely or exit without an unhandled exception.
7.  After a successful add, edit, or delete command, the updated contact list must be restored when the application is closed and reopened.
8.  If saving contact data fails, the application must report the failure and leave the last successfully saved data file intact.

*{More to be added}*

### Glossary

* **Contact**: A stored record for a person or organisation, containing a name and any available contact details, department, and tags.
* **Contact ID**: A positive integer that identifies the contact to be updated by the `depart` command.
* **Department**: An optional organisational unit associated with a contact. Setting a new department replaces that contact's existing department.
* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Matching contact**: For a tag search, a contact that has every tag specified in the command. A matching contact is displayed once even when it has more than one specified tag.
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Currently displayed list**: The contacts shown after any filtering or sorting. The index used to view a contact refers to its position in this list.
* **Partial contact**: A contact with a name but without some optional details, such as a phone number, email address, department, or tags.
* **Duplicate contact**: An existing contact with the same name, phone number, and email address as a contact being added.
* **CSV export**: A UTF-8 comma-separated values file containing a header row and one row for each stored contact.
* **Tag**: An optional label attached to a contact to support categorisation and searching. Tag matching is case-insensitive.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Viewing a contact's details

1. Displayed-list indexing

   1. Run `list`, then `view 2`. Expected: the second contact's supported fields appear in the details window.
   1. Run `find Betsy`, then `view 1`. Expected: Betsy's details appear, and the filtered list remains unchanged.
   1. Run `view 2` when only one search result is displayed. Expected: an error gives the range `1-1`.
   1. Run `find NoSuchContact`, then `view 1`. Expected: an error says there is no contact to view.
   1. Try `view`, `view 0`, `view -1`, `view 1.5`, `view 1 2`, and `view 2147483648`.
      Expected: each reports that the index must be a positive integer and shows the usage.

1. Details window

   1. View a contact with a long name/address and multiple tags. Resize the details window and scroll.
      Expected: all values remain readable without truncation; tags are separated by commas.
   1. View a contact without tags. Expected: `Tags: None`.
   1. View another contact. Expected: the existing window updates rather than creating another window.
   1. Close the details window, then run `view 1`. Expected: the window reopens with the current details.
   1. Edit the viewed contact. Expected: the snapshot stays unchanged until another `view` command is run.
   1. Exit the application while details are visible. Expected: the details window closes with the main window.

1. Read-only behaviour

   1. Compare the data file contents and modification time before and after a valid `view` command.
      Expected: neither changes. The command still works if the data file cannot be written.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
