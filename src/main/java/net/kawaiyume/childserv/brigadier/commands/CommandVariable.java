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
  28 january 2021
  ----------------------------------------------------------------------------
  CommandVariable.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.brigadier.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kawaiyume.childserv.brigadier.helpers.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.argument;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.literal;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public final class CommandVariable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandRoom.class);

    public static void register(final CommandDispatcher<SourceContext> dispatcher)
    {
        dispatcher.register(
            literal("vars")
                .then(literal("set")
                    .then(argument("var", string())
                        .then(argument("payload", greedyString())
                            .executes(c -> varSet(c.getSource(), getString(c, "var"), getString(c, "payload")))
                        )
                    )
                )

                .then(literal("get")
                    .then(argument("var", string())
                        .executes(c -> varGet(c.getSource(), getString(c, "var")))
                    )
                )

                .then(literal("del")
                    .then(argument("var", string())
                        .executes(c -> varDel(c.getSource(), getString(c, "var")))
                    )
                )

                .then(literal("list")
                    .executes(c -> varList(c.getSource()))
                )
        );
    }

    private static int varSet(final SourceContext source, final String var, final String payload)
    {
        LOGGER.info("{} have set the variable {} to {}", source.getMemberId(), var, payload);

        source.getChildServ().getConfig().setVar(var, payload);

        return 1;
    }

    private static int varGet(final SourceContext source, final String var)
    {
        final StringBuilder str = new StringBuilder();
        str.append("# Var ").append(var).append(" is set to :").append('\n');

        final Optional<Map.Entry<String, String>> vvar = source.getChildServ().getConfig().getVars().entrySet().stream().filter(e -> e.getKey().equals(var)).findFirst();
        vvar.ifPresent(stringStringEntry -> str.append(stringStringEntry.getValue()).append('\n'));

        source.getChildServ().post(source.getRoomId(), str.toString(), true);

        return 1;
    }

    private static int varDel(final SourceContext source, final String var)
    {
        LOGGER.info("{} have deleted the variable {}", source.getMemberId(), var);

        source.getChildServ().getConfig().delVar(var);
        source.getChildServ().post(source.getRoomId(), "Var " + var + " deleted", true);

        return 1;
    }

    private static int varList(final SourceContext source)
    {
        final StringBuilder str = new StringBuilder();
        str.append("# List of vars :").append('\n');

        source.getChildServ().getConfig().getVars().forEach((var, payload) ->
        {
            str.append("- ").append(var).append('\n');
        });

        source.getChildServ().post(source.getRoomId(), str.toString(), true);

        return 1;
    }
}
