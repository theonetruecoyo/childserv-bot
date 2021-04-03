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
  CommandLife.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.brigadier.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.kawaiyume.childserv.brigadier.helpers.SourceContext;

import java.util.concurrent.TimeUnit;

import static net.kawaiyume.childserv.brigadier.helpers.CommandHelper.literal;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public final class CommandLife
{
    public static void register(final CommandDispatcher<SourceContext> dispatcher)
    {
        dispatcher.register(
            literal("reboot")
                .executes(c -> reboot(c.getSource())
            )
        );
    }

    private static int reboot(SourceContext source)
    {
        source.getChildServ().post(source.getRoomId(), "Rebooting in 5s");

        try
        {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        }
        catch (final InterruptedException ignored)
        {
            Thread.currentThread().interrupt();
        }

        System.exit(0);

        return 1;
    }
}
