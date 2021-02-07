/* vim: set filetype=java: ts=4: sw=4: */
/*
  Copyright 2021
  
  Licensed by Thales TCS
  ----------------------------------------------------------------------------
  Created 07/02/2021
  ----------------------------------------------------------------------------
  TestJoin.java
  ----------------------------------------------------------------------------
  <TAB> = 4 <space>
  UTF-8
  ----------------------------------------------------------------------------
 */

package net.kawaiyume.childserv.tests;

import io.github.ma1uta.matrix.client.StandaloneClient;
import io.github.ma1uta.matrix.client.model.auth.LoginResponse;
import io.github.ma1uta.matrix.client.model.room.RoomId;
import io.github.ma1uta.matrix.client.model.serverdiscovery.HomeserverInfo;
import io.github.ma1uta.matrix.client.model.serverdiscovery.ServerDiscoveryResponse;
import io.github.ma1uta.matrix.impl.exception.MatrixException;
import net.kawaiyume.childserv.ChildServ;
import net.kawaiyume.childserv.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas VEYSSIERE (T0047283 nicolas.veyssiere@thalesgroup.com)
 */
public class TestJoin
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestJoin.class);

    public static void main(final String[] args) throws InterruptedException
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

            //clientLoggedIn();
        }
        catch (final MatrixException e)
        {
            e.printStackTrace();
        }

        System.err.println(selfMemberId);

        botConfig.delServer("matrix.kawaiyume.net");

        botConfig.addServer("asra.gr");
        botConfig.addServer("midov.pl");
        botConfig.addServer("matrix.org");
        botConfig.addServer("nerdsin.space");
        botConfig.addServer("matrix.kiwifarms.net");
        botConfig.addServer("matrix.kawaiyume.net");

        final List<String> servers = botConfig.getServers();
        System.err.println(servers);
        final RoomId rId = mxClient.room().joinByIdOrAlias("!TFhoKWjAPgkcoCldKr:asra.gr", servers, null);
        //final RoomId rId = mxClient.room().joinById("!TFhoKWjAPgkcoCldKr:asra.gr", null);
        System.err.println(rId.getRoomId());

        Thread.currentThread().join();
    }
}
