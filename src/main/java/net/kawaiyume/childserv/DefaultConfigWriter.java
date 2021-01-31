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
  DefaultConfigWriter.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public class DefaultConfigWriter
{
    public static void main(final String[] args)
    {
        final Config config = new Config();

        config.setHost("-redacted-");
        config.setUsername("childservant");

        // password authentication
        config.setPassword("-redacted-");

        // token authentication
        config.setAccessToken("");
        config.setDeviceId("");
        config.setUserId("");

        config.setPrompt("/");

        config.clearRooms();

        config.addRoom("!lgnoVWidzrWSxrQllD:matrix.kawaiyume.net");
        config.addRoom("!joxsyRkUcrElcVOMHt:matrix.org");

        config.setRoomModes("!joxsyRkUcrElcVOMHt:matrix.org", Config.RoomMode.SELF_MODERATOR);
        config.setRoomModes("!lgnoVWidzrWSxrQllD:matrix.kawaiyume.net", Config.RoomMode.SELF_ADMINISTRATOR, Config.RoomMode.ADMINISTRATION);

        config.grant("-redacted-");

        System.out.println(config.getRoot());
    }
}
