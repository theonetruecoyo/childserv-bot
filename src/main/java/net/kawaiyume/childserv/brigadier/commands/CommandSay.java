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
  5 february 2021
  ----------------------------------------------------------------------------
  CommandSay.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.brigadier.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kawaiyume.childserv.brigadier.arguments.MatrixIdType;
import net.kawaiyume.childserv.brigadier.helpers.SourceContext;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.argument;
import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.literal;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public class CommandSay
{
    public static void register(final CommandDispatcher<SourceContext> dispatcher)
    {
        dispatcher.register(
            literal("say")
                .then(literal("room")
                    .then(argument("id", new MatrixIdType(false))
                        .then(argument("what", greedyString())
                            .executes(c -> sayRoom(c.getSource(), getString(c, "id"), getString(c, "what")))
                        )
                    )
                )
            );
    }

    private static int sayRoom(final SourceContext source, final String roomId, final String what)
    {
        source.getChildServ().post(roomId, what);

        return 1;
    }
}
