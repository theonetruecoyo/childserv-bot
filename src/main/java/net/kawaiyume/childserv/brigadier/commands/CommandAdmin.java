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
  CommandAdmin.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.brigadier.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kawaiyume.childserv.brigadier.helpers.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.argument;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.literal;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public final class CommandAdmin
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAdmin.class);

    public static void register(final CommandDispatcher<SourceContext> dispatcher)
    {
        dispatcher.register(
            literal("admin")
                .then(literal("grant")
                    .then(argument("id", greedyString())
                        .executes(c -> adminGrant(c.getSource(), getString(c, "id")))
                    )
                )

                .then(literal("revoke")
                    .then(argument("id", greedyString())
                        .executes(c -> adminRevoke(c.getSource(), getString(c, "id")))
                    )
                )

                .then(literal("list")
                    .executes(c -> adminList(c.getSource()))
                )
        );
    }

    private static int adminGrant(final SourceContext source, final String memberId)
    {
        LOGGER.info("{} have added new admin : {}", source.getMemberId(), memberId);

        source.getChildServ().getConfig().grant(memberId);
        source.getChildServ().post(source.getRoomId(), "Admin " + memberId + " added.");

        return 1;
    }

    private static int adminRevoke(final SourceContext source, final String memberId)
    {
        LOGGER.info("{} have revoked admin {}", source.getMemberId(), memberId);

        source.getChildServ().getConfig().revoke(memberId);
        source.getChildServ().post(source.getRoomId(), "Admin " + memberId + " revoked.");

        return 1;
    }

    private static int adminList(final SourceContext source)
    {
        final StringBuilder str = new StringBuilder();
        str.append("# List of admins :").append('\n');

        source.getChildServ().getConfig().getAdmins().forEach(room -> str.append("- ").append(room).append('\n'));

        source.getChildServ().post(source.getRoomId(), str.toString(), true);

        return 1;
    }
}
