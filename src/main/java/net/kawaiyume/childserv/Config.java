/* vim: set filetype=java: ts=4: sw=4: */
/*
  Copyright (c) 2020, kawaiyume.net
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
  ----------------------------------------------------------------------------
  26 october 2020
  ----------------------------------------------------------------------------
  Config.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv;

import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public class Config extends NBTNavigator
{
    private enum Folder
    {
        CONNECTION,
        ROOMS,
        COMMANDS,
        ADMINS,
        VARS,
        SERVERS
    }

    private enum Key
    {
        HOST,
        USERNAME,
        PASSWORD,
        ACCESSTOKEN,
        DEVICEID,
        USERID,

        MODES,
        PARAMS,

        PROMPT
    }

    public enum RoomMode
    {
        // for test purpose, do nothing
        TEST,

        // we are admin of this room
        SELF_ADMINISTRATOR,
        // we are moderator of this room
        SELF_MODERATOR,

        // on this room, some members can send us commands
        ADMINISTRATION,
        // this room will be monitored for new users joining and we will send a welcome message
        WELCOME,
        // this room will have the banlist syncrhonisation enabled
        BANLIST_SYNC
        ;

        public static RoomMode find(final String mode)
        {
            if(mode == null || mode.isEmpty())
            {
                return null;
            }

            for(final RoomMode r : values())
            {
                if(r.name().equalsIgnoreCase(mode))
                {
                    return r;
                }
            }

            return null;
        }
    }

    public Config()
    {
        super("config.dat");
    }

    private CompoundTag folder(final String path)
    {
        CompoundTag ret = getCompound(path);
        if (ret == null)
        {
            ret = new CompoundTag(path);
            addCompound(path, ret);
        }

        return ret;
    }

    private ListTag list(final String path, final Class<? extends Tag<?>> type)
    {
        ListTag ret = (ListTag) findTag(path);
        if (ret == null)
        {
            ret = new ListTag(path, type, new ArrayList<>());

            final CompoundTag parent = (CompoundTag) findParent(path);
            parent.getValue().put(extractLeafPath(path), ret);
        }

        return ret;
    }

    private CompoundTag folder(final Folder path)
    {
        return folder(path.name().toLowerCase());
    }

    private ListTag list(final Folder path, final Class<? extends Tag<?>> type)
    {
        return list(path.name().toLowerCase(), type);
    }

    private void setString(final String folder, final String key, final String value)
    {
        final CompoundTag f = folder(folder);
        f.put(new StringTag(key, value));

        save();
    }

    private void setString(final Folder folder, final Key key, final String value)
    {
        setString(folder.name().toLowerCase(), key.name().toLowerCase(), value);
    }

    private String getString(final String folder, final String key)
    {
        final CompoundTag f = folder(folder);
        if (f.containsKey(key))
        {
            return ((StringTag) f.get(key)).getValue();
        }

        return "";
    }

    private String getString(final Folder folder, final Key key)
    {
        return getString(folder.name().toLowerCase(), key.name().toLowerCase());
    }

    public String getHost()
    {
        return getString(Folder.CONNECTION, Key.HOST);
    }

    public void setHost(final String host)
    {
        setString(Folder.CONNECTION, Key.HOST, host);
    }

    public void setPassword(final String password)
    {
        setString(Folder.CONNECTION, Key.PASSWORD, password);
    }

    public String getPassword()
    {
        return getString(Folder.CONNECTION, Key.PASSWORD);
    }

    public void setUsername(final String username)
    {
        setString(Folder.CONNECTION, Key.USERNAME, username);
    }

    public String getUsername()
    {
        return getString(Folder.CONNECTION, Key.USERNAME);
    }

    public void setAccessToken(final String token)
    {
        setString(Folder.CONNECTION, Key.ACCESSTOKEN, token);
    }

    public String getAccessToken()
    {
        return getString(Folder.CONNECTION, Key.ACCESSTOKEN);
    }

    public void setDeviceId(final String deviceId)
    {
        setString(Folder.CONNECTION, Key.DEVICEID, deviceId);
    }

    public String getDeviceId()
    {
        return getString(Folder.CONNECTION, Key.DEVICEID);
    }

    public void setUserId(final String userId)
    {
        setString(Folder.CONNECTION, Key.USERID, userId);
    }

    public String getUserId()
    {
        return getString(Folder.CONNECTION, Key.USERID);
    }

    public void setPrompt(final String prompt)
    {
        setString(Folder.COMMANDS, Key.PROMPT, prompt);
    }

    public String getPrompt()
    {
        return getString(Folder.COMMANDS, Key.PROMPT);
    }

    public void addRoom(final String room)
    {
        final CompoundTag f = folder(Folder.ROOMS);
        f.put(new CompoundTag(room));

        save();
    }

    public void delRoom(final String room)
    {
        final CompoundTag f = folder(Folder.ROOMS);
        f.getValue().remove(room);

        save();
    }

    public CompoundTag getRoom(final String room, final boolean addIfMissing)
    {
        final CompoundTag f = folder(Folder.ROOMS);
        if (!f.containsKey(room) && addIfMissing)
        {
            addRoom(room);
        }

        return (CompoundTag) f.get(room);
    }

    public void clearRooms()
    {
        final CompoundTag f = folder(Folder.ROOMS);
        f.getValue().clear();

        save();
    }

    public List<String> getRooms()
    {
        final CompoundTag f = folder(Folder.ROOMS);
        return f.getValue().values().stream().map(Tag::getName).collect(Collectors.toList());
    }

    public void setRoomModes(final String room, final RoomMode... modes)
    {
        final CompoundTag r = getRoom(room, true);

        final List<Tag<?>> mModes = new ArrayList<>();
        for (final RoomMode str : modes)
        {
            mModes.add(new StringTag(null, str.name()));
        }

        final ListTag lModes = new ListTag(Key.MODES.name().toLowerCase(), StringTag.class, mModes);
        r.put(lModes);

        save();
    }

    public void addRoomMode(final String room, final RoomMode mode)
    {
        final CompoundTag r = getRoom(room, false);
        if(r != null)
        {
            if(!r.containsKey(Key.MODES.name().toLowerCase()))
            {
                // add empty room list
                final ListTag lModes = new ListTag(Key.MODES.name().toLowerCase(), StringTag.class, new ArrayList<>());
                r.put(lModes);
            }

            final ListTag lModes = (ListTag) r.get(Key.MODES.name().toLowerCase());
            if(lModes.getValue().stream().map(t -> (StringTag)t).noneMatch(t -> t.getValue().equals(mode.name())))
            {
                lModes.getValue().add(new StringTag(null, mode.name()));
            }

            save();
        }
    }

    public void delRoomMode(final String room, final RoomMode mode)
    {
        final CompoundTag r = getRoom(room, false);
        if(r != null)
        {
            if(r.containsKey(Key.MODES.name().toLowerCase()))
            {
                final ListTag lModes = (ListTag) r.get(Key.MODES.name().toLowerCase());
                for(final Tag<?> t : lModes.getValue())
                {
                    final StringTag tt = (StringTag) t;
                    if(tt.getValue().equals(mode.name()))
                    {
                        lModes.getValue().remove(t);
                        break;
                    }
                }

                save();
            }
        }
    }

    public List<String> getRoomModes(final String room)
    {
        final CompoundTag r = getRoom(room, false);
        if(r != null)
        {
            final ListTag lModes = (ListTag) r.getValue().get(Key.MODES.name().toLowerCase());
            if(lModes != null)
            {
                return lModes.getValue().stream().map(t -> (StringTag) t).map(StringTag::getValue).collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    public boolean isRoomModeEnabled(final String room, final RoomMode mode)
    {
        final CompoundTag r = getRoom(room, false);
        if(r != null)
        {
            final ListTag lModes = (ListTag) r.getValue().get(Key.MODES.name().toLowerCase());
            if(lModes != null)
            {
                return lModes.getValue().stream().map(t -> (StringTag) t).anyMatch(t -> t.getValue().equals(mode.name()));
            }
        }

        return false;
    }

    public void grant(final String userId)
    {
        final ListTag l = list(Folder.ADMINS, StringTag.class);
        if(l.getValue().stream().map(t -> (StringTag)t).noneMatch(t -> t.getValue().equals(userId)))
        {
            l.getValue().add(new StringTag(null, userId));
        }

        save();
    }

    public void revoke(final String userId)
    {
        final ListTag l = list(Folder.ADMINS, StringTag.class);
        for(final Tag<?> t : l.getValue())
        {
            final StringTag tt = (StringTag) t;
            if(tt.getValue().equals(userId))
            {
                l.getValue().remove(t);
                break;
            }
        }

        save();
    }

    public List<String> getAdmins()
    {
        final ListTag l = list(Folder.ADMINS, StringTag.class);
        return l.getValue().stream().map(t -> (StringTag)t).map(StringTag::getValue).collect(Collectors.toList());
    }

    public boolean isAdmin(final String userId)
    {
        final ListTag l = list(Folder.ADMINS, StringTag.class);
        return l.getValue().stream().map(t -> (StringTag)t).anyMatch(t -> t.getValue().equals(userId));
    }

    public void addServer(final String server)
    {
        final ListTag l = list(Folder.SERVERS, StringTag.class);
        if(l.getValue().stream().map(t -> (StringTag)t).noneMatch(t -> t.getValue().equals(server)))
        {
            l.getValue().add(new StringTag(null, server));
        }

        save();
    }

    public void delServer(final String server)
    {
        final ListTag l = list(Folder.SERVERS, StringTag.class);
        for(final Tag<?> t : l.getValue())
        {
            final StringTag tt = (StringTag) t;
            if(tt.getValue().equals(server))
            {
                l.getValue().remove(t);
                break;
            }
        }

        save();
    }

    public List<String> getServers()
    {
        final ListTag l = list(Folder.SERVERS, StringTag.class);
        return l.getValue().stream().map(t -> (StringTag)t).map(StringTag::getValue).collect(Collectors.toList());
    }

    public void setVar(final String var, final String payload)
    {
        final CompoundTag f = folder(Folder.VARS);
        f.put(new StringTag(var, payload));

        save();
    }

    public void delVar(final String var)
    {
        final CompoundTag f = folder(Folder.VARS);
        f.getValue().remove(var);

        save();
    }

    public Map<String, String> getVars()
    {
        final CompoundTag f = folder(Folder.VARS);
        return f.getValue().values().stream().collect(Collectors.toMap(Tag::getName, e -> (String)e.getValue()));
    }

    public void setRoomModeParams(final String room, final RoomMode mode, final String params)
    {
        final CompoundTag r = getRoom(room, false);
        if(r != null)
        {
            if(!r.containsKey(Key.PARAMS.name().toLowerCase()))
            {
                // add empty params root
                final CompoundTag lParams = new CompoundTag(Key.PARAMS.name().toLowerCase());
                r.put(lParams);
            }

            final CompoundTag lParams = (CompoundTag) r.get(Key.PARAMS.name().toLowerCase());
            lParams.put(mode.name(), new StringTag(mode.name(), params));

            save();
        }
    }

    public String getParam(final String room, final RoomMode mode)
    {
        final CompoundTag r = getRoom(room, false);
        if(r != null)
        {
            if(r.containsKey(Key.PARAMS.name().toLowerCase()))
            {
                final CompoundTag lParams = (CompoundTag) r.get(Key.PARAMS.name().toLowerCase());
                if(lParams != null && !lParams.isEmpty())
                {
                    if(lParams.containsKey(mode.name()))
                    {
                        return ((StringTag)lParams.get(mode.name())).getValue();
                    }
                }
            }
        }

        return null;
    }

    public static void main(final String[] args)
    {
        final Config c = new Config();
        System.out.println(c.getRooms());

        System.out.println(c.isRoomModeEnabled("!lgnoVWidzrWSxrQllD:matrix.kawaiyume.net", RoomMode.SELF_MODERATOR));

        System.out.println(c.isAdmin("plop"));
        System.out.println(c.isAdmin("@omutsunobaka:matrix.kawaiyume.net"));
    }
}
