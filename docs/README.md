# Plana User Guide

Plana manages tasks and client records for small businesses such as home bakeries.
Tasks and clients are kept in separate lists.

## Managing clients

### Adding a client

Use `client add` with a name, a unique email, and any optional contact details.

```text
client add Alice Tan /email alice@example.com /phone 91234567 /address 12 Baker Street /preferences no nuts, less sweet /notes Birthday cake customer
```

The name and email are required. Phone, address, baking-product preferences, and notes are optional.
Email addresses are case-insensitively unique and are stored in lowercase.

### Listing clients

```text
client list
```

Clients are shown in creation order. Each client receives a temporary list-position reference such as `C1`.
The reference is used by the view, edit, and delete commands and can change after a deletion.

### Viewing client details

```text
client view C1
```

This displays all fields. Missing optional fields are shown as `Not provided`.

### Finding clients

```text
client find alice
```

Search is case-insensitive and checks the name, email, phone, address, preferences, and notes.

### Editing a client

```text
client edit C1 /phone 98765432 /preferences less sweet
```

Use one or more field markers: `/name`, `/email`, `/phone`, `/address`, `/preferences`, and `/notes`.
To clear an optional field, use an empty quoted value, such as `/notes ""`.

### Deleting a client

```text
client delete C1
```

Deletion is permanent. Since orders are not currently supported, there are no dependent records.

## Client command reference

```text
client add <name> /email <email> [/phone <phone>] [/address <address>] [/preferences <preferences>] [/notes <notes>]
client list
client view <position>
client find <keyword>
client edit <position> /field <value>
client delete <position>
```

Client records are stored separately from tasks in `data/clients.txt`.
