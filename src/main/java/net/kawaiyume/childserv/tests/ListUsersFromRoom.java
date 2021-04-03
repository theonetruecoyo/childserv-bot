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
  ListUsersFromRoom.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.tests;

import io.github.ma1uta.matrix.client.StandaloneClient;
import io.github.ma1uta.matrix.client.model.auth.LoginResponse;
import io.github.ma1uta.matrix.client.model.event.JoinedMembersResponse;
import io.github.ma1uta.matrix.client.model.serverdiscovery.HomeserverInfo;
import io.github.ma1uta.matrix.client.model.serverdiscovery.ServerDiscoveryResponse;
import io.github.ma1uta.matrix.impl.exception.MatrixException;
import net.kawaiyume.childserv.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author iXô (ixo@kawaiyume.net)
 */
public class ListUsersFromRoom
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ListUsersFromRoom.class);

    public static void main(final String[] args)
    {
        final Config botConfig = new Config();

        String selfMemberId = null;

        final StandaloneClient mxClient = new StandaloneClient.Builder().domain(botConfig.getHost()).build();
        try
        {
            final String accessToken = botConfig.getAccessToken();
            if(accessToken != null && !accessToken.isEmpty())
            {
                LOGGER.info("Authentication with auth token ...");

                // login using access token
                final LoginResponse fakeLoginFromToken = new LoginResponse();
                fakeLoginFromToken.setUserId(botConfig.getUserId());
                fakeLoginFromToken.setAccessToken(botConfig.getAccessToken());
                fakeLoginFromToken.setDeviceId(botConfig.getDeviceId());
                fakeLoginFromToken.setHomeServer(botConfig.getHost());

                final ServerDiscoveryResponse serverDiscoveryResponse = new ServerDiscoveryResponse();
                final HomeserverInfo homeserverInfo = new HomeserverInfo();
                homeserverInfo.setBaseUrl("https://" + botConfig.getHost());
                serverDiscoveryResponse.setHomeserver(homeserverInfo);
                fakeLoginFromToken.setWellKnown(serverDiscoveryResponse);

                mxClient.afterLogin(fakeLoginFromToken);
                selfMemberId = botConfig.getUserId();
            }
            else
            {
                LOGGER.info("Authentication with password ...");

                // login using password, the reply will set the access token for the next time
                final LoginResponse response = mxClient.auth().login(botConfig.getUsername(), botConfig.getPassword().toCharArray());

                // update the config
                botConfig.setAccessToken(response.getAccessToken());
                botConfig.setUserId(response.getUserId());
                botConfig.setDeviceId(response.getDeviceId());

                selfMemberId = response.getUserId();
            }

            joinRoom(botConfig, mxClient, "!eqbPcPNQyRqBrpFmxh:matrix.org");
        }
        catch (final MatrixException e)
        {
            e.printStackTrace();
        }
    }

    private static void iJoinRoom(final Config botConfig, final StandaloneClient mxClient, final String roomId)
    {
        final List<String> servers = botConfig.getServers();
        if(servers == null || servers.isEmpty())
        {
            mxClient.room().joinById(roomId, null);
        }
        else
        {
            mxClient.room().joinByIdOrAlias(roomId, servers, null);
        }
    }

    public static void joinRoom(final Config botConfig, final StandaloneClient mxClient, final String roomId)
    {
        LOGGER.info("{} :: joining room", roomId);
        iJoinRoom(botConfig, mxClient, roomId);

        LOGGER.info("{} :: loading users for room", roomId);
        final JoinedMembersResponse joinedMembers = mxClient.event().joinedMembers(roomId);
        LOGGER.info("{} :: found {} users", roomId, joinedMembers.getJoined().size());
        //roomsUsers.put(roomId, new HashSet<>(joinedMembers.getJoined().keySet()));

        //LOGGER.info("{} :: loading banned users", roomId);
        //final List<Event> events = mxClient.event().members(roomId).getChunk();
        //final List<Event> banned = events.stream().filter(e -> ((RoomMemberContent) e.getContent()).getMembership().equals("ban")).collect(Collectors.toList());

        joinedMembers.getJoined().keySet().forEach(k ->
        {
            System.err.println(k);
        });
    }
}
