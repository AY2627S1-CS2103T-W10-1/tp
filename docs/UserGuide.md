---
layout: page
title: User Guide
---

sudoContact is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, sudoContact can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the scrollable command list. Press `q` while the Help window is active to close it.<br>
   Some example commands you can try:

   * `list` or `ls` : Lists all contacts.

   * `sort --by tags` : Sorts displayed contacts by their alphabetically smallest tag.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Named fields for `add` and `edit` can be in any order. Other commands follow their documented order.<br>
  For example, `p/PHONE_NUMBER n/NAME` is also acceptable for `add`. For `sort`, the option must precede the field.

* Extraneous parameters for `help`, `exit`, and `clear` are ignored.<br>
  For example, `help 123` is interpreted as `help`. The `list` and `ls` commands reject extra parameters.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Opens a scrollable window listing every available command with a short description. You can also open it from the Help menu or with `F1`. Press `q` while the Help window is active to close it.

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all contacts: `list` or `ls`

Shows all contacts in their stored order, clearing the current filter and display sorting.

Format: `list` or `ls`

* Both commands take no parameters.
* Leading and trailing spaces are ignored.
* Command words are case-insensitive: `LIST`, `List`, and `LS` are also accepted.
* Extra parameters produce: `The list command does not accept any parameters. Usage: list | ls`

Examples:

* `list`
* `ls`
* `find Betsy` followed by `ls` restores the full contact list.

### Assigning a department: `depart`

Assigns or replaces the department of a contact in the currently displayed list.

Format: `depart CONTACT_ID --set DEPARTMENT`

* `CONTACT_ID` is the positive index currently shown next to the contact, including after filtering or sorting.
* `DEPARTMENT` must contain 2-50 characters using letters, numbers, spaces, `-`, or `&`.
* Departments are optional. Newly added contacts have no department until you assign one.
* A successful assignment is saved and replaces any existing department. Editing other fields preserves it.
* Assigning a department resets display sorting but retains an active filter. Check the displayed indices again
  before assigning another department, or run `list` first.

Examples:

* `depart 1 --set Engineering`
* `depart 2 --set R&D Engineering`
* `sort --by tags` followed by `depart 1 --set Marketing` assigns Marketing to the first sorted contact.

Use `sort --by department` after assignments to group contacts by their stored department.

### Sorting contacts by department or tags: `sort`

Sorts the currently displayed contacts in ascending order while preserving any active `find` filter.

Format: `sort -b FIELD` or `sort --by FIELD`

* `FIELD` must be `department`, `dept`, `tag`, or `tags`. The aliases select the same two sorting fields.
* Department and tag comparisons are alphabetical and case-insensitive.
* Tag sorting uses each contact's alphabetically smallest tag, not the order tags were entered.
  It does not group contacts by every tag they share; a contact appears once, using its smallest tag as the key.
* Contacts without the selected field appear last. Equal sorting keys retain their current relative order,
  including case-only differences and when switching sorting fields; names are not a tie-breaker.
* Department sorting uses the stored department assigned with `depart CONTACT_ID --set DEPARTMENT`.
  Contacts without departments appear last.
* Successful sorting reports the number of displayed contacts and the selected field in one line.
* Only the display order changes. Contacts and their saved order remain unchanged, and sorting does not save the file.
* Displayed indices are updated; `edit 1`, `delete 1`, `view 1`, and `depart 1 --set Engineering` target the first row.
* Another sort uses the current displayed order as its starting point. `list`, `find`, and successful contact
  mutations (`add`, `edit`, `delete`, `depart`, `clear`, or `undo`) reset sorting to stored order.
  `view` and `help` preserve the display order. Sort order is not retained after restarting.
* Options and field aliases are lowercase. Use ordinary hyphens, not Unicode dashes. Exactly one option and one
  field are required; quoted fields, `--by=tags`, combined options, extra arguments, and descending order are rejected.

Examples:

* `sort -b department`
* `sort -b dept`
* `sort --by tag`
* `sort --by tags`

For example, start with Alex Tan, Beatrice Lim, and Chen Wei in that stored order:

Contact | Department | Tag
--------|------------|----
Alex Tan | Engineering | mentor
Beatrice Lim | Marketing | client
Chen Wei | Engineering | intern

`sort --by tags` displays Beatrice, Chen, then Alex and returns:

```text
Sorted 3 contacts by tags.
```

Starting again from stored order, `sort -b dept` displays Alex, Chen, then Beatrice and returns:

```text
Sorted 3 contacts by department.
```

Alex and Chen retain their relative order because their department keys are equal.
The reported count includes only contacts in the currently displayed list.

For an empty displayed list, the feedback is `No contacts found to sort.`

Problem | Feedback
--------|---------
Missing option | `Missing sorting option. Usage: sort -b/--by <department\|dept\|tag\|tags>`
Missing field | `Missing sorting field. Usage: sort -b/--by <department\|dept\|tag\|tags>`
Unknown field | `Invalid sorting field. Use department, dept, tag, or tags.`
Unsupported or repeated argument | `Unsupported argument. Usage: sort -b/--by <department\|dept\|tag\|tags>`
`--descending` supplied | `Descending order is not supported. Contacts are sorted in ascending order.`
Invalid displayed contact data | `Unable to sort contacts because some stored contact data is invalid.`

When several input problems occur, an explicit `--descending` token takes precedence, followed by unsupported
argument structure, missing option/value, and an invalid field. Failed input or invalid sorting data leaves the
current display order unchanged. Sorting preserves separate contact occurrences rather than deduplicating them;
the application's existing rules still prevent duplicate contacts from being added or loaded.

Sorting uses the current in-memory contacts; it does not reread the file. If contacts fail to load at startup,
the application starts with an empty list, so sorting reports `No contacts found to sort.` You can add valid contacts
and sort them in the same session. To load contacts from a repaired file, restart the application.
A missing file follows the existing sample-data behavior.

### Viewing a contact's details: `view`

Shows the selected contact's name, phone, email, department, address and tags in a separate contact details window.
The main result box shows a brief confirmation, such as `Showing details of contact 1: Amy Bee`.
Long values in the details window wrap onto multiple lines; scroll to read them. Text can be selected and copied.
Contacts without tags show `Tags: None`.
Contacts without a department show `Department: Not provided`.

Format: `view CONTACT_ID`

* `CONTACT_ID` is the positive integer shown beside a contact in the **currently displayed list**.
* After filtering or sorting, use the current displayed `CONTACT_ID`, rather than its position in the saved address book.
* Surrounding spaces are ignored. Missing contact IDs, zero, negative numbers, decimals and extra arguments are rejected.
* A contact ID outside the displayed list produces an error showing the valid range.
* An empty displayed list produces `There is no contact to view in the currently displayed list.`
* Viewing leaves contact data and the current filter unchanged and uses the same automatic saving flow as other commands.
* The window shows a snapshot. Run `view CONTACT_ID` again after editing, deleting or filtering to refresh it.
  Viewing another contact updates the same window. Closing the window does not exit sudoContact.

Examples:

* `list` followed by `view 2` shows the second contact in the full list.
* `depart 1 --set Engineering` followed by `view 1` shows `Department: Engineering` for the first displayed contact.
* `find Betsy` followed by `view 1` shows the first contact in the search results.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating contacts by tags: `find --tag`

Format: `find --tag TAG [--tag TAG ...]`

* Command words are case-insensitive: `find`, `FIND`, and `FiNd` are equivalent.
* The option must be lowercase `--tag`; `--TAG` is invalid. Tag values are case-sensitive:
  `friends` does not match `Friends`.
* Matches contacts with **any** of the specified tags, respecting case and matching whole tags.
* Each matching contact is displayed once, even if several tags match or a tag is repeated.
* Tags must contain 1–30 letters, numbers, hyphens (`-`), or underscores (`_`), without spaces.
* A missing value displays `Tag cannot be empty: Please provide a valid tag after '--tag'.`
* An invalid value displays `Invalid Tag: '[INPUT]' must be 1-30 characters using only letters, numbers, hyphens (-), and underscores (_), with no spaces.`
* No matches displays `No contacts found with the specified tag(s).`

Example: `find --tag client --tag important`

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​
* You can restore a deleted person with `undo`.

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Undoing a deletion: `undo`

Restores the person most recently removed with `delete`.

Format: `undo`

* Only reverts `delete`. It does not undo any other command, such as `add` or `edit`.
* Run `undo` again to restore earlier deletions, most recent first.
* Deletions are remembered only until you close the app.
* The restored person is added to the end of the list, and the full list is shown.
* If a person with the same name (case-sensitive) already exists, e.g. because you added them again, `undo` fails
  and shows that person.
  To restore the deleted person, first `edit` the existing person's name, then run `undo` again.
* `undo` does not take any parameters. For example, `undo 2` shows an error.

Examples:
* `delete 2` followed by `undo` restores the person who was 2nd in the list.
* `delete 1`, `delete 1`, then `undo` twice restores both persons, the second one deleted first.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Exporting contacts as CSV: `export`

Creates a CSV copy of **all stored contacts**, including contacts hidden by a search.
Export does not change your contacts or the displayed list.

Format: `export -c|--csv [FILENAME]`

* Omit `FILENAME` to use `contacts.csv`. Use either `-c` or `--csv` before the filename.
* Use a filename ending in lowercase `.csv`, without a folder path. Surround a filename containing spaces
  with matching double or single quotes, for example `export --csv "my contacts.csv"`.
* Filenames cannot be empty or contain `< > : " / \ | ? *` or control characters. Leading or trailing spaces
  and reserved device names such as `CON.csv`, `NUL.csv` and `COM1.csv` are rejected.
* The file is created in the folder from which you launched the app. Following the Quick start instructions,
  this is the folder containing the JAR file.
* Existing files are never overwritten. Choose another filename or move the existing file before retrying.
* The UTF-8 file starts with `Name,Phone,Email,Department,Address,Tags`. Each stored contact gets one row,
  in stored order, even if several records have the same name. Missing details produce empty cells.
  Tags appear alphabetically in one cell, separated by `; `. Commas, quotes and line breaks within a value
  are preserved using CSV quoting. Phone numbers retain their original digits in the file; when importing
  into a spreadsheet, select a text column to keep leading zeroes.

Examples:

* `export --csv` creates `contacts.csv`.
* `export -c team.csv` creates `team.csv` using the short option.
* `find John` followed by `export --csv team.csv` still exports every stored contact.
* `export --csv "my contacts.csv"` creates `my contacts.csv`.

Success: `Exported 3 contacts to contacts.csv.` (the count reflects your stored contacts).
An empty address book reports `No contacts found to export.` and creates no file.
Invalid arguments report the command format or an invalid filename. If the folder is unavailable,
unwritable or out of space, the app reports that it cannot create the CSV; choose a writable launch folder
or free space and retry. Export is a copy for external use; it does not replace the app's JSON data file.

### Saving the data

AddressBook automatically saves data after successful commands except `sort` and `export`.
You do not need to save manually.
`sort` changes only the displayed order; it does not write the contact file or persist the sort order.
`export` writes a separate CSV copy; it does not modify or save the JSON contact file.
Departments and tags are contact data and are saved normally when changed.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until a successful command that saves data overwrites it. `sort` uses the current in-memory contacts without writing the file; failed commands also do not save. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**Export** | `export -c|--csv [FILENAME]`<br> e.g., `export -c team.csv`
**View** | `view CONTACT_ID`<br> e.g., `view 2`
**List** | `list` or `ls`
**Depart** | `depart CONTACT_ID --set DEPARTMENT`<br> e.g., `depart 1 --set Engineering`
**Sort** | `sort -b FIELD` or `sort --by FIELD`, where `FIELD` is `department`, `dept`, `tag`, or `tags`
**Undo** | `undo`
**Help** | `help`
