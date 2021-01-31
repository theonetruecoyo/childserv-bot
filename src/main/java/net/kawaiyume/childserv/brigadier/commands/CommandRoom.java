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
  13 november 2020
  ----------------------------------------------------------------------------
  CommandRoom.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.brigadier.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kawaiyume.childserv.Config;
import net.kawaiyume.childserv.brigadier.arguments.MatrixIdType;
import net.kawaiyume.childserv.brigadier.arguments.RoomModeType;
import net.kawaiyume.childserv.brigadier.helpers.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.kawaiyume.childserv.brigadier.arguments.RoomModeType.getRoomMode;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.argument;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.literal;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public final class CommandRoom
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandRoom.class);

    public static void register(final CommandDispatcher<SourceContext> dispatcher)
    {
        dispatcher.register(
            literal("room")
                .then(literal("add")
                    .then(argument("id", new MatrixIdType(true))
                        .executes(c -> roomAdd(c.getSource(), getString(c, "id")))
                    )
                )

                .then(literal("del")
                    .then(argument("id", new MatrixIdType(true))
                        .executes(c -> roomDel(c.getSource(), getString(c, "id")))
                    )
                )

                .then(argument("id", new MatrixIdType(false))
                    .then(literal("mode")
                        .then(literal("add")
                            .then(argument("mode", new RoomModeType())
                                .executes(c -> roomModeAdd(c.getSource(), getString(c, "id"), getRoomMode(c, "mode")))
                            )
                        )

                        .then(literal("del")
                            .then(argument("mode", new RoomModeType())
                                .executes(c -> roomModeDel(c.getSource(), getString(c, "id"), getRoomMode(c, "mode")))
                            )
                        )

                        .then(literal("list")
                            .executes(c -> roomModeList(c.getSource(), getString(c, "id")))
                        )
                    )

                    .then(literal("params")
                        .then(argument("mode", new RoomModeType())
                            .then(literal("set")
                                .then(argument("params", string())
                                    .executes(c -> roomModeSetParams(c.getSource(), getString(c, "id"), getRoomMode(c, "mode"), getString(c, "params")))
                                )
                            )
                        )
                    )
                )

                .then(literal("list")
                    .executes(c -> roomList(c.getSource()))
                )
        );
    }

    private static int roomAdd(final SourceContext source, final String roomId)
    {
        LOGGER.info("{} have add room {}", source.getMemberId(), roomId);

        source.getChildServ().getConfig().addRoom(roomId);
        source.getChildServ().post(source.getRoomId(), "Room " + roomId + " added.");
        source.getChildServ().joinRoom(roomId);

        return 1;
    }

    private static int roomDel(final SourceContext source, final String roomId)
    {
        LOGGER.info("{} have removed room {}", source.getMemberId(), roomId);

        source.getChildServ().getConfig().delRoom(roomId);
        source.getChildServ().post(source.getRoomId(), "Room " + roomId + " removed.");
        source.getChildServ().leaveRoom(roomId);

        return 1;
    }

    private static int roomList(final SourceContext source)
    {
        final StringBuilder str = new StringBuilder();
        str.append("# List of rooms :").append('\n');

        source.getChildServ().getConfig().getRooms().forEach(roomId ->
        {
            str.append("- ").append(roomId).append(" [").append(source.getChildServ().getRoomName(roomId)).append(']').append('\n');

            // add modes for each rooms
            source.getChildServ().getConfig().getRoomModes(roomId).forEach(mode ->
            {
                str.append("  - ").append(mode);
                final String param = source.getChildServ().getConfig().getParam(roomId, Config.RoomMode.find(mode));
                if(param != null && !param.isEmpty())
                {
                    str.append(" [ ").append(param).append(" ]");
                }
                str.append('\n');
            });
        });

        source.getChildServ().post(source.getRoomId(), str.toString(), true);

        return 1;
    }

    private static int roomModeAdd(final SourceContext source, final String roomId, final Config.RoomMode mode)
    {
        LOGGER.info("{} have added mode {} to room {}", source.getMemberId(), mode.name(), roomId);

        source.getChildServ().getConfig().addRoomMode(roomId, mode);
        source.getChildServ().post(source.getRoomId(), "Mode " + mode.name() + " added to room " + roomId);

        return 1;
    }

    private static int roomModeDel(final SourceContext source, final String roomId, final Config.RoomMode mode)
    {
        LOGGER.info("{} have removed mode {} from room {}", source.getMemberId(), mode.name(), roomId);

        source.getChildServ().getConfig().delRoomMode(roomId, mode);
        source.getChildServ().post(source.getRoomId(), "Mode " + mode.name() + " removed from room " + roomId);

        return 1;
    }

    private static int roomModeList(final SourceContext source, final String roomId)
    {
        final StringBuilder str = new StringBuilder();
        str.append("# Mode list for room ").append(roomId).append(" :").append('\n');

        source.getChildServ().getConfig().getRoomModes(roomId).forEach(mode ->
        {
            str.append("- ").append(mode);
            final String param = source.getChildServ().getConfig().getParam(roomId, Config.RoomMode.find(mode));
            if(param != null && !param.isEmpty())
            {
                str.append(" [ ").append(param).append(" ]");
            }
            str.append('\n');
        });

        source.getChildServ().post(source.getRoomId(), str.toString(), true);

        return 1;
    }

    private static int roomModeSetParams(final SourceContext source, final String roomId, final Config.RoomMode mode, final String params)
    {
        LOGGER.info("{} have set parameter(s) {} to mode {} for room {}", source.getMemberId(), params, mode.name(), roomId);

        source.getChildServ().getConfig().setRoomModeParams(roomId, mode, params);
        source.getChildServ().post(source.getRoomId(), "Params set to " + params + " to mode " + mode.name() + " for room " + roomId);

        return 1;
    }
}
