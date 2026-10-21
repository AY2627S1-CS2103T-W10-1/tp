[![Java CI](https://github.com/AY2627S1-CS2103T-W10-1/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W10-1/tp/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-W10-1/tp/graph/badge.svg?token=PE1XWTQ0AX)](https://codecov.io/gh/AY2627S1-CS2103T-W10-1/tp)

![Ui](docs/images/Ui.png)

# sudoContact

sudoContact is a desktop contact-management application for secretaries supporting department heads in technology firms. It is designed for users who manage a large volume of professional contacts and prefer the speed and precision of a Unix-style command-line interface, while retaining a graphical view of the current contact list.

It helps users capture incomplete contact records, organise contacts by department and tags, retrieve relevant groups quickly, and export contact lists as CSV files for downstream communication workflows.

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

## Key capabilities

sudoContact's minimum viable product (MVP) comprises the following 11 capabilities:

1. Manage a shared department catalogue: list all registered departments (including unused ones), add departments, rename them across all associated contacts, and delete departments while clearing their contact assignments. Assign or change a contact's department using a registered name.
2. Search across all tags.
3. View one contact's full details.
4. Add a contact with only partial information.
5. Delete a contact.
6. Undo a deletion.
7. List all contacts.
8. Sort contacts by department or tags.
9. View available commands and their usage.
10. Use Unix-style command options, including short (`-`) and long (`--`) forms.
11. Generate a CSV export of the contact list.

## Intended users

sudoContact is for departmental secretaries and administrative staff who:

- maintain contact records for employees, clients, vendors, and other stakeholders;
- need to categorise contacts by department or working relationship;
- work comfortably from a keyboard and value command-driven workflows; and
- need a portable CSV export for email and reporting processes.

## Command overview

The following command formats describe sudoContact's product interface. Replace uppercase placeholders with values; items in square brackets are optional.

| Task | Command format | Example |
| --- | --- | --- |
| Add a contact | `add --name NAME [--phone PHONE] [--email EMAIL] [--department DEPARTMENT] [--tag TAG ...]` | `add --name "Alice Tan" --email "alice@example.com" --department Engineering --tag client` |
| List departments | `depart --list` | `depart --list` |
| Delete a department | `depart --delete DEPARTMENT` | `depart --delete Engineering` |
| Add a department | `depart --add DEPARTMENT` | `depart --add Engineering` |
| Rename a department | `depart --edit OLD_NAME NEW_NAME` | `depart --edit Engineering "Research & Development"` |
| Assign a department | `edit CONTACT_ID --department DEPARTMENT` | `edit 3 --department Engineering` |
| Find by tags | `find --tag TAG [--tag TAG ...]` | `find --tag client --tag important` |
| View a contact | `view INDEX` | `view 2` |
| List contacts | `list` or `ls` | `ls` |
| Sort contacts | `sort -b/--by <department\|dept\|tag\|tags>` | `sort --by department` |
| Delete a contact | `delete INDEX` | `delete 2` |
| Restore the latest deletion | `undo` | `undo` |
| Export contacts | `export --csv [FILENAME]` | `export --csv investors.csv` (`contacts.csv` when omitted) |
| Show help | `help [COMMAND]` or `? [COMMAND]` | `help depart` |

`INDEX` refers to the one-based position in the contact list currently displayed. This makes it possible to find or filter a group first and then act on a specific result.

Register a department with `depart --add` before assigning it through `add` or `edit`.
Use `undo` to reverse the latest contact or department deletion in the current session.
Quote department names containing spaces in `depart` commands. Renaming updates all associated contacts.

Use a single hyphen for short options and a double hyphen for long options. For example, `sort -b department` and `sort --by department` are equivalent. Command options and their values are shown above wherever applicable.

Use `help` or `?` for the command overview, and `help COMMAND` (for example, `help depart`)
for usage, supported options, and examples. Help also covers `edit`, `clear`, and `exit`.
Command names and help topics are case-insensitive; aliases such as `ls` are accepted.
The Help menu and `F1` show the overview. Press `q` in the Help window to close it.

## Data rules

- A contact name and at least one phone number or email address are required; other details may be supplied later.
- Phone numbers contain 7–15 digits, and email addresses must use a valid email format.
- Department names are 2–50 characters and may contain letters, numbers, spaces, hyphens, and ampersands.
- Tags are 1–30 characters and may contain letters, numbers, hyphens, and underscores. Tag matching is case-sensitive.
- Contacts with the same name are duplicates if either their provided phone numbers or email addresses match. Missing fields do not match.
- Previously valid saved contacts remain intact, including conflicts under the stricter rule; new add, edit and contact undo operations still reject duplicates.
- Sorting is ascending and case-insensitive; contacts without the selected sorting field appear last.
- CSV exports use UTF-8, include a header row, and preserve each stored contact as a separate row.

## Building from source

### Prerequisites

- Java Development Kit (JDK) 25 or later

### Build and run

From the repository root:

```powershell
.\gradlew.bat shadowJar
java -jar build\libs\addressbook.jar
```

On macOS or Linux:

```sh
./gradlew shadowJar
java -jar build/libs/addressbook.jar
```

Run the automated test suite with:

```powershell
.\gradlew.bat test
```

## Project documentation

- [User Guide](docs/UserGuide.md)
- [Developer documentation](docs/Documentation.md)
- [Testing guide](docs/Testing.md)

## License

This project is released under the [MIT License](LICENSE).
