# ChildServ admin commands help :

ChildServ have a limited admin command list, they are parsed by the Brigadier library from Mojang

All commands must be prefixed by the command prompt, by default (if not changed) this is the `/` caracter

_Little tip_ : if using Element, the `/` must be doubled, so each command must be something like `//command <arguments>`

Here is the list of the commands :

**Note** : <XXX> is an argument, the `<` and `>` symbols should not be entered in the final command

## Version

- `/version` : return the version of ChildServ

## Room

- `/room add <id>` : add the `id` room to the managed room list
- `/room del <id>` : del the `id` room from the managed room list
- `/room list` : list all rooms from managed room list
- `/room <id> mode add <mode>` : add a `mode` to the `id` room
- `/room <id> mode add <mode>` : del the `mode` from the `id` room
- `/room <id> mode list` : list all modes for the room `id`
- `/room <id> params <mode> set <params>` : set the params for the mode `mode` of room `id`

## Variables

- `/vars set <var> <payload>` : set the `var` to `payload`
- `/vars get <var>` : get the payload for the `var`
- `/vars del <var>` : del the `var`
- `/vars list` : list the vars

### Valid modes

- `TEST` : for test purpose, do nothing


- `SELF_ADMINISTRATOR` : we are admin of this room
- `SELF_MODERATOR` : we are moderator of this room


- `ADMINISTRATION` : on this room, some members (ie = admin) can send us commands
- `WELCOME` : this room will be monitored for new users joining and we will send a welcome message
- `BANLIST_SYNC` : this room will have the banlist syncrhonisation enabled

**Note** : room mode can be provided case insensitive 

## Admin

- `/admin grant <id>` : set admin rights for the `id` member
- `/admin revoke <id>` : removed the admin rights for the `id` member
- `/admin list` : list all admins

## Help

- `/help` : ?????? profit !