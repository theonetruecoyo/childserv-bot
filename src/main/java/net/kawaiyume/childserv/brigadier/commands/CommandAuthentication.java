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
  Created 01/04/2021
  ----------------------------------------------------------------------------
  CommandAuthentication.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.brigadier.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kawaiyume.childserv.brigadier.helpers.SourceContext;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.argument;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.literal;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public final class CommandAuthentication
{
    public static void register(final CommandDispatcher<SourceContext> dispatcher)
    {
        dispatcher.register(
            literal("login")
                .then(literal("get")
                    .executes(c -> loginGet(c.getSource()))
                )

                .then(literal("set")
                    .then(argument("login", greedyString())
                        .executes(c -> loginSet(c.getSource(), getString(c, "login")))
                    )
                )
        );

        dispatcher.register(
            literal("password")
                .then(literal("get")
                    .executes(c -> passwordGet(c.getSource()))
                )

                .then(literal("set")
                    .then(argument("password", greedyString())
                        .executes(c -> passwordSet(c.getSource(), getString(c, "password")))
                    )
                )
        );

        dispatcher.register(
            literal("host")
                .then(literal("get")
                    .executes(c -> hostGet(c.getSource()))
                )

                .then(literal("set")
                    .then(argument("host", greedyString())
                        .executes(c -> hostSet(c.getSource(), getString(c, "host")))
                    )
                )
        );
    }

    private static int loginSet(final SourceContext source, final String login)
    {
        final String old = source.getChildServ().getConfig().getUsername();
        source.getChildServ().getConfig().setUsername(login);
        source.getChildServ().getConfig().setAccessToken("");

        source.getChildServ().post(source.getRoomId(), "Login updated from " + old + " to " + source.getChildServ().getConfig().getUsername());

        return 1;
    }

    private static int loginGet(final SourceContext source)
    {
        source.getChildServ().post(source.getRoomId(), "Current login : " + source.getChildServ().getConfig().getUsername());

        return 1;
    }

    private static int hostSet(final SourceContext source, final String host)
    {
        final String old = source.getChildServ().getConfig().getHost();
        source.getChildServ().getConfig().setHost(host);
        source.getChildServ().getConfig().setAccessToken("");

        source.getChildServ().post(source.getRoomId(), "Host updated from " + old + " to " + source.getChildServ().getConfig().getHost());

        return 1;
    }

    private static int hostGet(final SourceContext source)
    {
        source.getChildServ().post(source.getRoomId(), "Current host : " + source.getChildServ().getConfig().getHost());

        return 1;
    }

    private static int passwordSet(final SourceContext source, final String password)
    {
        final String old = source.getChildServ().getConfig().getPassword();
        source.getChildServ().getConfig().setPassword(password);
        source.getChildServ().getConfig().setAccessToken("");

        source.getChildServ().post(source.getRoomId(), "Password updated from " + old + " to " + source.getChildServ().getConfig().getPassword());

        return 1;
    }

    private static int passwordGet(final SourceContext source)
    {
        source.getChildServ().post(source.getRoomId(), "Current password : " + source.getChildServ().getConfig().getPassword());

        return 1;
    }
}
