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
  ChildServ.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ma1uta.matrix.client.StandaloneClient;
import io.github.ma1uta.matrix.client.model.auth.LoginResponse;
import io.github.ma1uta.matrix.client.model.event.JoinedMembersResponse;
import io.github.ma1uta.matrix.client.model.presence.PresenceStatus;
import io.github.ma1uta.matrix.client.model.room.CreateRoomRequest;
import io.github.ma1uta.matrix.client.model.room.RoomId;
import io.github.ma1uta.matrix.client.model.sync.AccountData;
import io.github.ma1uta.matrix.client.model.sync.DeviceLists;
import io.github.ma1uta.matrix.client.model.sync.InvitedRoom;
import io.github.ma1uta.matrix.client.model.sync.JoinedRoom;
import io.github.ma1uta.matrix.client.model.sync.LeftRoom;
import io.github.ma1uta.matrix.client.model.sync.Presence;
import io.github.ma1uta.matrix.client.model.sync.Rooms;
import io.github.ma1uta.matrix.client.model.sync.SyncResponse;
import io.github.ma1uta.matrix.client.model.sync.Timeline;
import io.github.ma1uta.matrix.client.sync.SyncLoop;
import io.github.ma1uta.matrix.client.sync.SyncParams;
import io.github.ma1uta.matrix.event.Event;
import io.github.ma1uta.matrix.event.RoomEncrypted;
import io.github.ma1uta.matrix.event.RoomEvent;
import io.github.ma1uta.matrix.event.RoomMessage;
import io.github.ma1uta.matrix.event.StateEvent;
import io.github.ma1uta.matrix.event.content.EventContent;
import io.github.ma1uta.matrix.event.content.RoomEncryptedContent;
import io.github.ma1uta.matrix.event.content.RoomMemberContent;
import io.github.ma1uta.matrix.event.content.RoomMessageContent;
import io.github.ma1uta.matrix.event.message.Text;
import io.github.ma1uta.matrix.impl.exception.MatrixException;
import net.kawaiyume.childserv.brigadier.commands.CommandAdmin;
import net.kawaiyume.childserv.brigadier.commands.CommandBanUnban;
import net.kawaiyume.childserv.brigadier.commands.CommandHelp;
import net.kawaiyume.childserv.brigadier.commands.CommandRoom;
import net.kawaiyume.childserv.brigadier.commands.CommandVersion;
import net.kawaiyume.childserv.brigadier.helpers.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public class ChildServ
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ChildServ.class);

    private static boolean DEBUG_MODE = false;
    private static final boolean DEBUG_SYNC_THREAD = false;
    private static final boolean BAN_UNBAN_DRY_RUN = true;

    private final Config botConfig;

    private final ExecutorService syncLoopExecutorService = Executors.newFixedThreadPool(2);

    private final ScheduledExecutorService backgroundTasksExecutorService = new ScheduledThreadPoolExecutor(2);

    private final CommandDispatcher<SourceContext> dispatcher = new CommandDispatcher<>();

    private final StandaloneClient mxClient;

    private String selfMemberId;

    private long readyTs = -1;

    private final Map<String, WelcomeRoom> welcomeRooms = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> roomsUsers = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> roomsBanned = new ConcurrentHashMap<>();

    public ChildServ(final boolean debug)
    {
        DEBUG_MODE = debug;

        botConfig = new Config();

        mxClient = new StandaloneClient.Builder().domain(botConfig.getHost()).build();

        // register commands
        CommandVersion.register(dispatcher);
        CommandRoom.register(dispatcher);
        CommandAdmin.register(dispatcher);
        CommandHelp.register(dispatcher);
        CommandBanUnban.register(dispatcher);

        try
        {
            final LoginResponse response = mxClient.auth().login(botConfig.getUsername(), botConfig.getPassword().toCharArray());
            selfMemberId = response.getUserId();

            clientLoggedIn();
        }
        catch (final MatrixException e)
        {
            e.printStackTrace();
        }
    }

    public void post(final String roomId, final String message)
    {
        post(roomId, message, false);
    }

    @SuppressWarnings("rawtypes")
    public void joinRoom(final String roomId)
    {
        LOGGER.info("{} :: joining room", roomId);
        mxClient.room().joinById(roomId, null);

        LOGGER.info("{} :: loading users for room", roomId);
        final JoinedMembersResponse joinedMembers = mxClient.event().joinedMembers(roomId);
        LOGGER.info("{} :: found {} users", roomId, joinedMembers.getJoined().size());
        roomsUsers.put(roomId, new HashSet<>(joinedMembers.getJoined().keySet()));

        LOGGER.info("{} :: loading banned users", roomId);
        final List<Event> events = mxClient.event().members(roomId).getChunk();
        final List<Event> banned = events.stream().filter(e -> ((RoomMemberContent) e.getContent()).getMembership().equals("ban")).collect(Collectors.toList());

        System.err.println("----- Status for room " + roomId);
        joinedMembers.getJoined().keySet().forEach(k ->
        {
            try
            {
                final PresenceStatus s = mxClient.presence().getPresenceStatus(k);
                if (s != null)
                {
                    if (s.getCurrentlyActive() == null)
                    {
                        System.err.println(k + " - N/A - " + s.getLastActiveAgo());
                    }
                    else
                    {
                        System.err.println(k + " - " + (s.getCurrentlyActive() ? "active" : "inactive") + " - " + s.getLastActiveAgo());
                    }
                }
                else
                {
                    System.err.println(k + " - no presence infos");
                }
            }
            catch(final Exception e)
            {
                System.err.println(k + " - exception");
            }
        });

        roomsBanned.put(roomId, new ConcurrentHashMap<>());

        if (!banned.isEmpty())
        {
            LOGGER.info("{} :: found {} banned users", roomId, banned.size());
            final Map<String, String> m = roomsBanned.get(roomId);
            for (final Event e : banned)
            {
                if (e instanceof StateEvent)
                {
                    final StateEvent s = (StateEvent) e;
                    if (s.getContent() instanceof RoomMemberContent)
                    {
                        final String reason = ((RoomMemberContent) s.getContent()).getReason();
                        if (reason == null || reason.isEmpty())
                        {
                            m.put(s.getStateKey(), "");
                        }
                        else
                        {
                            m.put(s.getStateKey(), reason);
                        }
                    }
                    else
                    {
                        m.put(s.getStateKey(), "");
                    }
                }
            }
        }
    }

    public void leaveRoom(final String roomId)
    {
        LOGGER.info("{} :: leaving room", roomId);
        mxClient.room().leave(roomId);

        roomsUsers.remove(roomId);
        roomsBanned.remove(roomId);
    }

    public void post(final String roomId, final String message, final boolean markown)
    {
        if (markown)
        {
            final MarkdownEngine markdownEngine = new MarkdownEngine();
            mxClient.event().sendFormattedMessage(roomId, message, markdownEngine.render(message));
        }
        else
        {
            mxClient.event().sendMessage(roomId, message);
        }
    }

    public String getRoomName(final String roomId)
    {
        return mxClient.room().name(roomId).getName();
    }

    public boolean banUser(final String roomId, final String memberId, final String reason)
    {
        if (!roomsBanned.get(roomId).containsKey(memberId))
        {
            LOGGER.info("Banning user {} from room {}", memberId, roomId);
            try
            {
                if(BAN_UNBAN_DRY_RUN)
                {
                    System.err.println("DRY RUN !!! ban(" + roomId + ", " + memberId + ", " + reason + ")");
                }
                else
                {
                    mxClient.room().ban(roomId, memberId, reason);
                }

                roomsBanned.get(roomId).put(memberId, reason);
                roomsUsers.get(roomId).remove(memberId);

                return true;
            }
            catch(final MatrixException e)
            {
                LOGGER.error("Can't ban {} !", memberId, e);
            }
        }

        return false;
    }

    public boolean unbanUser(final String roomId, final String memberId)
    {
        if(roomsBanned.get(roomId).containsKey(memberId))
        {
            LOGGER.info("Un-banning user {} from room {}", memberId, roomId);

            try
            {
                if(BAN_UNBAN_DRY_RUN)
                {
                    System.err.println("DRY RUN !!! unban(" + roomId + ", " + memberId + ")");
                }
                else
                {
                    mxClient.room().unban(roomId, memberId);
                }
                roomsBanned.get(roomId).remove(memberId);

                return true;
            }
            catch (final MatrixException e)
            {
                LOGGER.error("Can't unban {} !", memberId, e);
            }
        }

        return false;
    }

    public Config getConfig()
    {
        return botConfig;
    }

    private void clientLoggedIn()
    {
        if (!DEBUG_MODE)
        {
            // leave all rooms without leaving rooms we must be present
            final List<String> previousRooms = mxClient.room().joinedRooms().getJoinedRooms();
            LOGGER.info("We must leave {} rooms", previousRooms.size() - botConfig.getRooms().size());
            previousRooms.forEach(room ->
            {
                if (!botConfig.getRooms().contains(room))
                {
                    leaveRoom(room);

                    try
                    {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // join configured rooms
        botConfig.getRooms().forEach(room ->
        {
            try
            {
                joinRoom(room);

                try
                {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
            catch (final MatrixException e)
            {
                LOGGER.error("Unable to join room {}", room, e);
            }
        });

        // merge all banned user from room where we the flag is set
        if(!DEBUG_MODE)
        {
            final Map<String, String> allBan = new HashMap<>();
            botConfig.getRooms().forEach(room ->
            {
                if (botConfig.isRoomModeEnabled(room, Config.RoomMode.BANLIST_SYNC))
                {
                    allBan.putAll(roomsBanned.get(room));
                }
            });

            // now perform ban
            if (!allBan.isEmpty())
            {
                botConfig.getRooms().forEach(room ->
                {
                    if (botConfig.isRoomModeEnabled(room, Config.RoomMode.BANLIST_SYNC) && (botConfig.isRoomModeEnabled(room, Config.RoomMode.SELF_MODERATOR) || botConfig.isRoomModeEnabled(room, Config.RoomMode.SELF_ADMINISTRATOR)))
                    {
                        allBan.forEach((m, r) ->
                        {
                            if (banUser(room, m, r))
                            {
                                try
                                {
                                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                                }
                                catch (InterruptedException e)
                                {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        });
                    }
                });
            }
        }

        readyTs = new Date().getTime();

        LOGGER.info("Ready !");
        final SyncLoop syncLoop = new SyncLoop(mxClient.sync(), this::processIncomingEvents);
        final SyncParams params = SyncParams.builder().fullState(true).presence("online").timeout(10L * 1000L).build();
        syncLoop.setInit(params);

        // sync loop for matrix events
        syncLoopExecutorService.submit(syncLoop);

        // background task for leaving welcome rooms
        backgroundTasksExecutorService.scheduleWithFixedDelay(this::checkForObsoleteWelcomeRooms, 1, 1, TimeUnit.MINUTES);
        backgroundTasksExecutorService.scheduleWithFixedDelay(this::sendPresenceStatus, 1, 30, TimeUnit.MINUTES);
    }

    @SuppressWarnings("rawtypes")
    private void processIncomingEvents(final SyncResponse syncResponse, final SyncParams syncParams)
    {
        if (syncParams.isFullState())
        {
            syncParams.setFullState(false);
        }

        final Rooms rooms = syncResponse.getRooms();
        if (rooms != null)
        {
            if (!DEBUG_MODE)
            {
                processLeave(rooms);
                processJoin(rooms);
                processPosts(rooms);
            }
            else
            {
                processPosts(rooms);
            }
        }

        if (DEBUG_MODE || DEBUG_SYNC_THREAD)
        {
            AccountData accountData = syncResponse.getAccountData();
            if (accountData != null)
            {
                System.out.println("=== Account data ===");
                accountData.getEvents().forEach(ChildServ::printEvent);
            }

            DeviceLists deviceLists = syncResponse.getDeviceLists();
            if (deviceLists != null)
            {
                System.out.println("=== Changed devices ===");
                deviceLists.getChanged().forEach(System.out::println);

                System.out.println("=== Left devices ===");
                deviceLists.getLeft().forEach(System.out::println);

                System.out.println("=== Changed devices ===");
                deviceLists.getChanged().forEach(System.out::println);
            }

            Presence presence = syncResponse.getPresence();
            if (presence != null)
            {
                presence.getEvents().forEach(ChildServ::printEvent);
            }

            //Rooms rooms = syncResponse.getRooms();
            if (rooms != null)
            {
                Map<String, InvitedRoom> invite = rooms.getInvite();
                if (invite != null)
                {
                    System.out.println("=== Invites ===");
                    for (Map.Entry<String, InvitedRoom> inviteEntry : invite.entrySet())
                    {
                        System.out.println("Invite into the room: " + inviteEntry.getKey());
                        InvitedRoom invitedRoom = inviteEntry.getValue();
                        if (invitedRoom != null && invitedRoom.getInviteState() != null)
                        {
                            List<Event> inviteEvents = invitedRoom.getInviteState().getEvents();
                            if (inviteEvents != null)
                            {
                                inviteEvents.forEach(ChildServ::printEvent);
                            }
                        }
                    }
                }

                Map<String, LeftRoom> leave = rooms.getLeave();
                if (leave != null)
                {
                    System.out.println("=== Left rooms ===");
                    for (Map.Entry<String, LeftRoom> leftEntry : leave.entrySet())
                    {
                        System.out.println("Left from the room: " + leftEntry.getKey());
                    }
                }

                Map<String, JoinedRoom> join = rooms.getJoin();
                if (join != null)
                {
                    System.out.println("=== Joined rooms ===");
                    for (Map.Entry<String, JoinedRoom> joinEntry : join.entrySet())
                    {
                        System.out.println("Joined room: " + joinEntry.getKey());
                        JoinedRoom joinedRoom = joinEntry.getValue();

                        System.out.println("= State =");
                        if (joinedRoom.getState() != null && joinedRoom.getState().getEvents() != null)
                        {
                            joinedRoom.getState().getEvents().forEach(ChildServ::printEvent);
                        }

                        Timeline timeline = joinedRoom.getTimeline();
                        if (timeline != null && timeline.getEvents() != null)
                        {
                            System.out.println("= Timeline =");
                            timeline.getEvents().forEach(ChildServ::printEvent);
                        }
                    }
                }
            }
        }
    }

    private void processLeave(final Rooms rooms)
    {
        final Map<String, LeftRoom> leave = rooms.getLeave();
        if (leave != null)
        {
            for (final Map.Entry<String, LeftRoom> leftEntry : leave.entrySet())
            {
                if (botConfig.getRooms().contains(leftEntry.getKey()))
                {
                    syncLoopExecutorService.submit(() -> comeBack(leftEntry.getKey()));
                }
            }
        }
    }

    private void processJoin(final Rooms rooms)
    {
        final Map<String, JoinedRoom> join = rooms.getJoin();
        if (join != null)
        {
            for (final Map.Entry<String, JoinedRoom> joinEntry : join.entrySet())
            {
                if (botConfig.getRooms().contains(joinEntry.getKey()))
                {
                    final JoinedRoom joinedRoom = joinEntry.getValue();
                    final Timeline timeline = joinedRoom.getTimeline();
                    if (timeline != null && timeline.getEvents() != null && !timeline.getEvents().isEmpty())
                    {
                        timeline.getEvents().forEach(event ->
                        {
                            if (event.getType().equals("m.room.member") && event instanceof RoomEvent)
                            {
                                final long serverTs = ((RoomEvent<?>) event).getOriginServerTs();
                                if (serverTs > readyTs)
                                {
                                    if (event.getContent() instanceof RoomMemberContent)
                                    {
                                        switch (((RoomMemberContent) event.getContent()).getMembership())
                                        {
                                            case RoomMemberContent.JOIN:
                                            {
                                                final String senderId = ((RoomEvent<?>) event).getSender();
                                                if (!roomsUsers.get(joinEntry.getKey()).contains(senderId))
                                                {
                                                    LOGGER.info("{} joined the room {}", senderId, joinEntry.getKey());
                                                    roomsUsers.get(joinEntry.getKey()).add(senderId);

                                                    if (botConfig.isRoomModeEnabled(joinEntry.getKey(), Config.RoomMode.WELCOME))
                                                    {
                                                        sendWelcomeMessage(senderId);
                                                    }
                                                }
                                                else
                                                {
                                                    LOGGER.info("{} changed it's display name", senderId);
                                                }
                                                break;
                                            }

                                            case RoomMemberContent.LEAVE:
                                            {
                                                // check if this is a unban (api bug : the unban action can be seen as a leave action)
                                                if (event instanceof StateEvent)
                                                {
                                                    final String unbanned = ((StateEvent<?>) event).getStateKey();
                                                    if (unbanned != null && !unbanned.isEmpty())
                                                    {
                                                        if (roomsBanned.get(joinEntry.getKey()).containsKey(unbanned))
                                                        {
                                                            roomsBanned.get(joinEntry.getKey()).remove(unbanned);
                                                            LOGGER.info("{} is unbanned from the room {}", unbanned, joinEntry.getKey());
                                                            break;
                                                        }
                                                    }
                                                }

                                                final String senderId = ((RoomEvent<?>) event).getSender();
                                                roomsUsers.get(joinEntry.getKey()).remove(senderId);

                                                break;
                                            }

                                            case RoomMemberContent.BAN:
                                            {
                                                final String bannedId = ((StateEvent<?>) event).getStateKey();
                                                final String moderatorId = ((RoomEvent<?>) event).getSender();
                                                if (!moderatorId.equals(selfMemberId))
                                                {
                                                    final String reason = ((RoomMemberContent) event.getContent()).getReason();
                                                    roomsBanned.get(joinEntry.getKey()).put(bannedId, reason);
                                                    roomsUsers.get(joinEntry.getKey()).remove(bannedId);
                                                    LOGGER.info("{} have been banned from the room {} by {} (reason : {})", bannedId, joinEntry.getKey(), moderatorId, reason);

                                                    // propagate ban for all the other rooms
                                                    botConfig.getRooms().forEach(room ->
                                                    {
                                                        if (!room.equals(joinEntry.getKey()))
                                                        {
                                                            if (botConfig.isRoomModeEnabled(room, Config.RoomMode.BANLIST_SYNC) && (botConfig.isRoomModeEnabled(room, Config.RoomMode.SELF_MODERATOR) || botConfig.isRoomModeEnabled(room, Config.RoomMode.SELF_ADMINISTRATOR)))
                                                            {
                                                                banUser(room, bannedId, reason);
                                                            }
                                                        }
                                                    });
                                                }

                                                break;
                                            }

                                            default:
                                                LOGGER.warn("Not implemented room membership event : {}", ((RoomMemberContent) event.getContent()).getMembership());
                                                break;
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
                else if (welcomeRooms.containsKey(joinEntry.getKey()))
                {
                    final JoinedRoom joinedRoom = joinEntry.getValue();
                    final Timeline timeline = joinedRoom.getTimeline();
                    if (timeline != null && timeline.getEvents() != null && !timeline.getEvents().isEmpty())
                    {
                        timeline.getEvents().forEach(event ->
                        {
                            if (event.getType().equals("m.room.member") && event instanceof RoomEvent)
                            {
                                final long serverTs = ((RoomEvent<?>) event).getOriginServerTs();
                                if (serverTs > readyTs)
                                {
                                    if (event.getContent() instanceof RoomMemberContent)
                                    {
                                        if (((RoomMemberContent) event.getContent()).getMembership().equals("leave"))
                                        {
                                            leaveWelcomeRoom("user left", joinEntry.getKey());
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private void leaveWelcomeRoom(final String reason, final String roomId)
    {
        LOGGER.info("Leaving welcome room {} (reason : {})", roomId, reason);
        welcomeRooms.remove(roomId);
        mxClient.room().leave(roomId);
    }

    private void processPosts(final Rooms rooms)
    {
        final Map<String, JoinedRoom> join = rooms.getJoin();
        if (join != null)
        {
            for (final Map.Entry<String, JoinedRoom> joinEntry : join.entrySet())
            {
                if (botConfig.getRooms().contains(joinEntry.getKey()))
                {
                    final JoinedRoom joinedRoom = joinEntry.getValue();
                    final Timeline timeline = joinedRoom.getTimeline();
                    if (timeline != null && timeline.getEvents() != null && !timeline.getEvents().isEmpty())
                    {
                        timeline.getEvents().forEach(event ->
                        {
                            if (event.getType().equals("m.room.encrypted") && event instanceof RoomEvent)
                            {
                                final long serverTs = ((RoomEvent<?>) event).getOriginServerTs();
                                if (serverTs > readyTs)
                                {
                                    if (event instanceof RoomEncrypted)
                                    {
                                        final RoomEncryptedContent encryptedContent = (RoomEncryptedContent) event.getContent();
                                        System.err.println(encryptedContent + " - " + encryptedContent.getAlgorithm());
                                    }
                                }
                            }
                            else if (event.getType().equals("m.room.message") && event instanceof RoomEvent)
                            {
                                final long serverTs = ((RoomEvent<?>) event).getOriginServerTs();
                                if (serverTs > readyTs)
                                {
                                    if (event instanceof RoomMessage)
                                    {
                                        if (event.getContent() instanceof Text)
                                        {
                                            final Text textContent = (Text) event.getContent();
                                            final String text = textContent.getBody();

                                            // check if this is a command
                                            if (text.startsWith(botConfig.getPrompt()))
                                            {
                                                processCommand(text, joinEntry.getKey(), ((RoomMessage<?>) event).getSender());
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private void processCommand(final String text, final String roomId, final String senderId)
    {
        // check if this is a room where the commands are enabled
        if (botConfig.isRoomModeEnabled(roomId, Config.RoomMode.ADMINISTRATION))
        {
            // check if the command have been issued by an admin
            if (botConfig.isAdmin(senderId))
            {
                try
                {
                    dispatcher.execute(text.substring(botConfig.getPrompt().length()), new SourceContext(ChildServ.this, roomId, senderId));
                }
                catch (final CommandSyntaxException e)
                {
                    mxClient.event().sendMessage(roomId, e.getMessage());
                }
            }
        }
    }

    private void sendWelcomeMessage(final String who)
    {
        LOGGER.info("Sending welcome message to {}", who);

        final CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        createRoomRequest.setDirect(true);
        createRoomRequest.setName("Welcome to Degeneracy");
        createRoomRequest.setRoomAliasName(UUID.randomUUID().toString());
        createRoomRequest.setTopic("Information about #degeneracy:midov.pl");

        final List<String> invitations = new ArrayList<>();
        invitations.add(who);
        createRoomRequest.setInvite(invitations);

        final RoomId newRoomId = mxClient.room().create(createRoomRequest);

        final MarkdownEngine markdownEngine = new MarkdownEngine();
        final String message = "**You just joined #degeneracy:midov.pl, a tabooless fetish & shitposting room for true deviants. Visit our community page for more information: +degeneracy:midov.pl**\n" + "\n" + "This room uses end-to-end encryption. This means only users inside the room can read the messages and new users won't be able to see messages sent before they joined the room. When you join, you will either see an empty room or messages that fail to load. This is normal and you don't have to do anything to receive messages normally from this point on.\n" + "\n" + "If messages keep failing to decrypt, or your messages take a long time to send, this is probably an issue of your homeserver. Especially matrix.org handles encrypted rooms poorly and it is recommended you switch to a faster server. To find out how to do this and to ask other questions related to Matrix or Element, join our Matrix / Element discussion room: #unabletodecrypt:midov.pl\n" + "\n" + "**Rules**\n" + "\n" + "Almost everything is allowed here and moderation is kept to the absolute minimum, but please follow our 3 rules:\n" + "\n" + "- 1  Don't be narrow-minded. There are no rules except the ones listed here, which exist for practical and legal reasons. Don't let your morals or beliefs get in the way of other peoples fun.\n" + "- 2  Don't confess or plan violent or sexual crimes in a serious way.\n" + "- 3  Don't post, request or discuss pornographic or sexualized content involving real children. This means: any images or videos naked, in underwear or in a sexual context.\n" + "\n" + "Everything that is not on this list is allowed. Join the room at your own discretion if you are easily shocked or upset. Also keep in mind that \"tabooless\" does not mean \"lawless\", and that it is your own responsibility to follow additional laws of your country or state that could get you in trouble.\n" + "\n" + "Have fun.";

        mxClient.event().sendFormattedMessage(newRoomId.getRoomId(), message, markdownEngine.render(message));

        welcomeRooms.put(newRoomId.getRoomId(), new WelcomeRoom(newRoomId.getRoomId()));
    }

    private void comeBack(final String roomId)
    {
        try
        {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        LOGGER.debug("Joining room {} after having kicked", roomId);
        mxClient.room().joinById(roomId, null);
    }

    private void checkForObsoleteWelcomeRooms()
    {
        final List<String> mustLeave = welcomeRooms.values().stream().filter(WelcomeRoom::isOld).map(WelcomeRoom::getId).collect(Collectors.toList());
        if (!mustLeave.isEmpty())
        {
            mustLeave.stream().forEach(room -> leaveWelcomeRoom("timeout", room));
        }
    }

    private void sendPresenceStatus()
    {
        mxClient.presence().setPresenceStatus("online", new Date().toLocaleString());
    }

    public static void printEvent(final Event<?> event)
    {
        System.out.println("Type: " + event.getType());

        if (event instanceof RoomEvent)
        {
            RoomEvent<?> roomEvent = (RoomEvent<?>) event;

            System.out.println("Event ID: " + roomEvent.getEventId());
            System.out.println("Room ID: " + roomEvent.getRoomId());
            System.out.println("Sender: " + roomEvent.getSender());
            System.out.println("Origin server TS: " + roomEvent.getOriginServerTs());

            if (roomEvent instanceof StateEvent)
            {
                StateEvent<?> stateEvent = (StateEvent<?>) roomEvent;

                System.out.println("State key: " + stateEvent.getStateKey());
            }
        }

        EventContent content = event.getContent();
        if (content instanceof RoomMessageContent)
        {
            RoomMessageContent roomMessageContent = (RoomMessageContent) content;

            System.out.println("MSG type: " + roomMessageContent.getMsgtype());
            System.out.println("Body: " + roomMessageContent.getBody());
        }
    }
}
